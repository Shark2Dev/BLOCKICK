package com.blockick.app.domain.engine

import android.util.Log
import com.blockick.app.data.db.dao.QueryLogDao
import com.blockick.app.data.db.dao.StatsDao
import com.blockick.app.data.db.entities.QueryLogEntity
import com.blockick.app.data.db.entities.StatsEntity
import com.blockick.app.data.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.isActive
import org.xbill.DNS.*
import java.net.InetAddress
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsPacketProcessor @Inject constructor(
    private val blocklistEngine: BlocklistEngine,
    private val upstreamResolver: UpstreamResolver,
    private val statsDao: StatsDao,
    private val queryLogDao: QueryLogDao,
    private val appPreferences: AppPreferences
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val logChannel = kotlinx.coroutines.channels.Channel<LogEvent>(capacity = 1000)

    // Safe Search VIPs
    private val safeSearchMap = mapOf(
        "google.com" to "216.239.38.120", // forcesafesearch.google.com
        "www.google.com" to "216.239.38.120",
        "bing.com" to "204.79.197.220", // strict.bing.com
        "www.bing.com" to "204.79.197.220",
        "duckduckgo.com" to "safe.duckduckgo.com",
        "www.duckduckgo.com" to "safe.duckduckgo.com",
        "yandex.com" to "213.180.193.56", // family.yandex.com
        "www.yandex.com" to "213.180.193.56"
    )

    init {
        startLogWorker()
    }

    private fun startLogWorker() {
        scope.launch {
            val logBatch = mutableListOf<LogEvent>()
            var lastFlushTime = System.currentTimeMillis()

            while (isActive) {
                try {
                    val event = withTimeoutOrNull(1000) { logChannel.receive() }
                    if (event != null) {
                        logBatch.add(event)
                    }

                    val currentTime = System.currentTimeMillis()
                    if (logBatch.size >= 100 || (currentTime - lastFlushTime >= 5000 && logBatch.isNotEmpty())) {
                        flushLogs(logBatch)
                        logBatch.clear()
                        lastFlushTime = currentTime
                    }
                } catch (e: Exception) {
                    Log.e("DnsProcessor", "Error in log worker", e)
                }
            }
        }
    }

    private suspend fun flushLogs(batch: List<LogEvent>) {
        val today = LocalDate.now().toString()
        val blockedCount = batch.count { it.isBlocked }
        val totalCount = batch.size

        try {
            if (statsDao.getStatsForDate(today) == null) {
                statsDao.insertOrUpdate(StatsEntity(date = today, blockedCount = 0, totalCount = 0))
            }
            
            repeat(blockedCount) { statsDao.incrementBlocked(today) }
            repeat(totalCount - blockedCount) { statsDao.incrementTotal(today) }

            val entities = batch.map { 
                QueryLogEntity(
                    domain = it.domain, 
                    isBlocked = it.isBlocked, 
                    queryType = it.queryType,
                    timestamp = it.timestamp
                ) 
            }
            queryLogDao.insertAll(entities)
        } catch (e: Exception) {
            Log.e("DnsProcessor", "Failed to flush logs to database", e)
        }
    }

    private data class LogEvent(
        val domain: String,
        val isBlocked: Boolean,
        val queryType: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private suspend fun isBypassActive(): Boolean {
        val bypassEnabled = appPreferences.bypassEnabled.first()
        if (!bypassEnabled) return false
        
        val now = java.time.LocalDateTime.now()
        val currentDay = now.dayOfWeek.value // 1 = Monday, 7 = Sunday
        val currentHour = now.hour
        val currentMinute = now.minute
        
        val bypassDays = appPreferences.bypassDays.first()
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
        
        if (!bypassDays.contains(currentDay)) return false
        
        val startHour = appPreferences.bypassStartHour.first()
        val startMinute = appPreferences.bypassStartMinute.first()
        val endHour = appPreferences.bypassEndHour.first()
        val endMinute = appPreferences.bypassEndMinute.first()
        
        val currentTimeMinutes = currentHour * 60 + currentMinute
        val startTimeMinutes = startHour * 60 + startMinute
        val endTimeMinutes = endHour * 60 + endMinute
        
        return if (startTimeMinutes <= endTimeMinutes) {
            // Same day bypass (e.g., 02:00 - 03:00)
            currentTimeMinutes in startTimeMinutes..endTimeMinutes
        } else {
            // Overnight bypass (e.g., 22:00 - 06:00)
            currentTimeMinutes >= startTimeMinutes || currentTimeMinutes <= endTimeMinutes
        }
    }

    suspend fun processPacket(queryData: ByteArray): ByteArray? {
        val query = try {
            Message(queryData)
        } catch (e: Exception) {
            Log.e("DnsProcessor", "Malformed query", e)
            return null
        }

        val question = query.question
        if (question == null) return createErrorResponse(query, Rcode.FORMERR)
        
        val domain = question.name.toString(true).lowercase().removeSuffix(".")
        val queryType = Type.string(question.type)

        // Check if bypass is active
        val bypassActive = isBypassActive()
        if (bypassActive) {
            Log.i("DnsProcessor", "BYPASS ACTIVE - Forwarding: $domain")
            logAndIncrement(domain, false, queryType)
            val response = upstreamResolver.resolve(queryData)
            return response ?: createErrorResponse(query, Rcode.SERVFAIL)
        }

        // 1. Check Safe Search
        if (appPreferences.safeSearchEnabled.first()) {
            val safeSearchTarget = safeSearchMap[domain] ?: safeSearchMap.entries.find { (key, _) -> 
                domain.endsWith(".$key") 
            }?.value
            
            if (safeSearchTarget != null) {
                Log.i("DnsProcessor", "Enforcing Safe Search for: $domain -> $safeSearchTarget")
                logAndIncrement(domain, false, queryType)
                return createSafeSearchResponse(query, safeSearchTarget)
            }
        }

        // 2. Check Blocklists
        return try {
            if (blocklistEngine.shouldBlock(domain)) {
                Log.i("DnsProcessor", "BLOCKING: $domain")
                logAndIncrement(domain, true, queryType)
                createNxDomainResponse(query)
            } else {
                logAndIncrement(domain, false, queryType)
                val response = upstreamResolver.resolve(queryData)
                if (response == null) {
                    Log.e("DnsProcessor", "Upstream Error: $domain")
                    createErrorResponse(query, Rcode.SERVFAIL)
                } else {
                    response
                }
            }
        } catch (e: Exception) {
            Log.e("DnsProcessor", "Exception processing $domain", e)
            createErrorResponse(query, Rcode.SERVFAIL)
        }
    }

    private fun createSafeSearchResponse(query: Message, target: String): ByteArray {
        val response = Message(query.header.id)
        response.header.setFlag(Flags.QR.toInt())
        response.header.setFlag(Flags.AA.toInt())
        response.header.setFlag(Flags.RA.toInt())
        response.addRecord(query.question, Section.QUESTION)

        try {
            if (target.contains(Regex("[a-zA-Z]"))) {
                // CNAME response
                val cname = Record.newRecord(query.question.name, Type.CNAME, DClass.IN, 3600, Name.fromString(target + ".").toWire())
                response.addRecord(cname, Section.ANSWER)
            } else {
                // A Record response
                val address = InetAddress.getByName(target)
                val aRecord = Record.newRecord(query.question.name, Type.A, DClass.IN, 3600, address.address)
                response.addRecord(aRecord, Section.ANSWER)
            }
        } catch (e: Exception) {
            Log.e("DnsProcessor", "Error creating Safe Search record", e)
        }

        return response.toWire()
    }

    private fun createNxDomainResponse(query: Message): ByteArray {
        val response = Message(query.header.id)
        response.header.setFlag(Flags.QR.toInt())
        response.header.setFlag(Flags.AA.toInt())
        response.header.setFlag(Flags.RA.toInt())
        response.header.rcode = Rcode.NXDOMAIN
        response.addRecord(query.question, Section.QUESTION)
        return response.toWire()
    }

    private fun createErrorResponse(query: Message, rcode: Int): ByteArray {
        val response = Message(query.header.id)
        response.header.setFlag(Flags.QR.toInt())
        response.header.rcode = rcode
        if (query.question != null) {
            response.addRecord(query.question, Section.QUESTION)
        }
        return response.toWire()
    }

    private fun logAndIncrement(domain: String, isBlocked: Boolean, queryType: String) {
        logChannel.trySend(LogEvent(domain, isBlocked, queryType))
    }
}

