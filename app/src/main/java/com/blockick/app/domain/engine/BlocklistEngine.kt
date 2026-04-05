package com.blockick.app.domain.engine

import com.blockick.app.data.db.dao.AllowlistDao
import com.blockick.app.data.db.dao.DomainDao
import com.blockick.app.data.db.dao.UserBlocklistDao
import kotlinx.coroutines.flow.first
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistEngine @Inject constructor(
    private val domainDao: DomainDao,
    private val allowlistDao: AllowlistDao,
    private val userBlocklistDao: UserBlocklistDao
) {
    private val loadMutex = Mutex()
    
    // Use HashSets for O(1) lookups. Volatile ensures visibility across threads.
    @Volatile
    private var blockedDomains = HashSet<String>()
    @Volatile
    private var allowedDomains = HashSet<String>()
    @Volatile
    private var userBlockedDomains = HashSet<String>()

    val totalRules: Int get() = blockedDomains.size + userBlockedDomains.size

    suspend fun loadRules() {
        // Prevent multiple simultaneous loads
        loadMutex.withLock {
            Log.i("BlocklistEngine", "Starting to load rules from database...")
            
            // Fetch from DB
            val blocked = domainDao.getAllEnabledDomains()
            val allowed = allowlistDao.getAll().first().map { it.domain.lowercase() }
            val userBlocked = userBlocklistDao.getAll().first().map { it.domain.lowercase() }
            
            Log.i("BlocklistEngine", "Fetched ${blocked.size} domains. Updating memory sets...")
            
            // Create new sets and swap them to avoid inconsistent states during loading
            val newBlocked = HashSet<String>(blocked.size)
            newBlocked.addAll(blocked)
            
            val newAllowed = HashSet<String>(allowed)
            val newUserBlocked = HashSet<String>(userBlocked)

            // Atomic swap
            blockedDomains = newBlocked
            allowedDomains = newAllowed
            userBlockedDomains = newUserBlocked
            
            Log.i("BlocklistEngine", "Rules updated: ${blockedDomains.size} blocked")
        }
    }

    fun shouldBlock(domain: String): Boolean {
        val normalized = domain.lowercase().trimEnd('.')
        
        // 1. Allowlist higher precedence
        if (allowedDomains.contains(normalized)) return false

        // 2. User Blocklist
        if (userBlockedDomains.contains(normalized)) return true
        
        // 3. Check exact match
        if (blockedDomains.contains(normalized)) return true
        
        // 4. Check parent domains (subdomain matching)
        var parent = normalized
        while (parent.contains(".")) {
            parent = parent.substringAfter(".", "")
            if (parent.isEmpty()) break
            if (userBlockedDomains.contains(parent)) return true
            if (blockedDomains.contains(parent)) return true
        }
        
        return false
    }
}
