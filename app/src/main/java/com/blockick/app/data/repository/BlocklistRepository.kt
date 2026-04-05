package com.blockick.app.data.repository

import com.blockick.app.data.db.dao.BlocklistDao
import com.blockick.app.data.db.dao.CustomListDao
import com.blockick.app.data.db.dao.DomainDao
import com.blockick.app.data.db.dao.UserBlocklistDao
import com.blockick.app.data.db.entities.BlocklistEntity
import com.blockick.app.data.db.entities.CustomListEntity
import com.blockick.app.data.db.entities.DomainEntity
import com.blockick.app.data.parsers.AdblockParser
import com.blockick.app.data.parsers.DomainListParser
import com.blockick.app.data.parsers.HostsFileParser
import com.blockick.app.data.parsers.JsonParser
import com.blockick.app.domain.engine.BlocklistEngine
import com.blockick.app.util.NotificationHelper
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

@Singleton
class BlocklistRepository @Inject constructor(
    private val blocklistDao: BlocklistDao,
    private val customListDao: CustomListDao,
    private val domainDao: DomainDao,
    private val userBlocklistDao: UserBlocklistDao,
    private val hostsParser: HostsFileParser,
    private val domainParser: DomainListParser,
    private val adblockParser: AdblockParser,
    private val jsonParser: JsonParser,
    private val okHttpClient: OkHttpClient,
    private val blocklistEngine: BlocklistEngine,
    private val notificationHelper: NotificationHelper
) {
    val blocklists = blocklistDao.getAll()
    val customLists = customListDao.getAll()

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        blocklistDao.getAll().first().isEmpty()
    }

    fun getTotalRulesCount(): Flow<Int> {
        return combine(blocklists, customLists, userBlocklistDao.getAll()) { lists, custom, user ->
            val fromLists = lists.filter { it.isEnabled }.sumOf { it.entryCount }
            val fromCustom = custom.filter { it.isEnabled }.sumOf { it.entryCount }
            val fromUser = user.size
            fromLists + fromCustom + fromUser
        }
    }

    suspend fun initializeDefaults() = withContext(Dispatchers.IO) {
        blocklistDao.deleteAll()
        val defaults = listOf(
            // 1. Dan Pollock
            BlocklistEntity(
                id = "pollock", name = "Dan Pollock", description = "Hosts file containing a list of domains to block.",
                url = "", format = "hosts", isEnabled = false, authorUrl = "https://someonewhocares.org/hosts/", author = "Dan Pollock"
            ),
            BlocklistEntity(
                id = "pollock-hosts", name = "Hosts", description = "The standard Dan Pollock hosts file.",
                url = "https://someonewhocares.org/hosts/hosts", format = "hosts", isEnabled = false, parentId = "pollock"
            ),

            // 2. 1Hosts (3 Profiles)
            BlocklistEntity(
                id = "1hosts", name = "1Hosts", description = "Advanced DNS filter blocklists for privacy and security.",
                url = "", format = "hosts", isEnabled = false, authorUrl = "https://github.com/badmojr/1Hosts", author = "badmojr"
            ),
            BlocklistEntity(
                id = "1hosts-lite", name = "Lite", description = "Balanced blocklist for general users.",
                url = "https://raw.githubusercontent.com/badmojr/1Hosts/master/Lite/hosts.txt", format = "hosts", isEnabled = false, parentId = "1hosts"
            ),
            BlocklistEntity(
                id = "1hosts-xtra", name = "Xtra", description = "Aggressive version for maximum threat mitigation.",
                url = "https://raw.githubusercontent.com/badmojr/1Hosts/master/Xtra/hosts.txt", format = "hosts", isEnabled = false, parentId = "1hosts"
            ),
            BlocklistEntity(
                id = "1hosts-pro", name = "Pro", description = "Aggressive version for maximum privacy.",
                url = "https://raw.githubusercontent.com/badmojr/1Hosts/master/Pro/hosts.txt", format = "hosts", isEnabled = false, parentId = "1hosts"
            ),

            // 3. EasyList
            BlocklistEntity(
                id = "easylist", name = "EasyList", description = "The primary filter list for international web pages.",
                url = "", format = "adblock", isEnabled = false, authorUrl = "https://easylist.to", author = "EasyList Team"
            ),
            BlocklistEntity(
                id = "easylist-ads", name = "Ads", description = "Primary ad-blocking filter rules.",
                url = "https://easylist.to/easylist/easylist.txt", format = "adblock", isEnabled = false, parentId = "easylist"
            ),

            // 4. EasyPrivacy
            BlocklistEntity(
                id = "easyprivacy", name = "EasyPrivacy", description = "Supplementary list that removes tracking.",
                url = "", format = "adblock", isEnabled = false, authorUrl = "https://easylist.to", author = "EasyList Team"
            ),
            BlocklistEntity(
                id = "easyprivacy-privacy", name = "Privacy", description = "Primary privacy-protection filter rules.",
                url = "https://easylist.to/easylist/easyprivacy.txt", format = "adblock", isEnabled = false, parentId = "easyprivacy"
            ),

            // 5. Goodbye Ads (2 Profiles)
            BlocklistEntity(
                id = "goodbyeads", name = "Goodbye Ads", description = "A powerful blocklist to eliminate ads, malware, and trackers.",
                url = "", format = "hosts", isEnabled = false, authorUrl = "https://github.com/jerryn70/GoodbyeAds", author = "jerryn70"
            ),
            BlocklistEntity(
                id = "goodbyeads-standard", name = "Standard", description = "Standard Goodbye Ads hosts file.",
                url = "https://raw.githubusercontent.com/jerryn70/GoodbyeAds/master/Hosts/GoodbyeAds.txt", format = "hosts", isEnabled = false, parentId = "goodbyeads"
            ),
            BlocklistEntity(
                id = "goodbyeads-extended", name = "Extended", description = "Extended Goodbye Ads hosts file with more aggressive blocking.",
                url = "https://raw.githubusercontent.com/jerryn70/GoodbyeAds/master/Extension/GoodbyeAds-YouTube-AdBlock.txt", format = "hosts", isEnabled = false, parentId = "goodbyeads"
            ),

            // 6. Phishing Army (2 Profiles)
            BlocklistEntity(
                id = "phishingarmy", name = "Phishing Army", description = "Extended coverage against phishing attacks.",
                url = "", format = "hosts", isEnabled = false, authorUrl = "https://phishing.army", author = "Phishing Army"
            ),
            BlocklistEntity(
                id = "phishingarmy-standard", name = "Standard", description = "Standard phishing blocklist.",
                url = "https://phishing.army/download/phishing_army_blocklist.txt", format = "hosts", isEnabled = false, parentId = "phishingarmy"
            ),
            BlocklistEntity(
                id = "phishingarmy-extended", name = "Extended", description = "Aggregates domains from multiple phishing feeds.",
                url = "https://phishing.army/download/phishing_army_blocklist_extended.txt", format = "hosts", isEnabled = false, parentId = "phishingarmy"
            ),

            // 7. hBlock
            BlocklistEntity(
                id = "hblock", name = "hBlock", description = "Improves security and privacy by blocking ads and tracking.",
                url = "", format = "hosts", isEnabled = false, authorUrl = "https://hblock.molinero.dev", author = "hectorm"
            ),
            BlocklistEntity(
                id = "hblock-main", name = "hBlock", description = "Main hBlock hosts ruleset.",
                url = "https://hblock.molinero.dev/hosts", format = "hosts", isEnabled = false, parentId = "hblock"
            ),

            // 8. URLHaus
            BlocklistEntity(
                id = "urlhaus", name = "URLHaus", description = "Malicious URL blocklist operated by abuse.ch.",
                url = "", format = "hosts", isEnabled = false, authorUrl = "https://urlhaus.abuse.ch", author = "abuse.ch"
            ),
            BlocklistEntity(
                id = "urlhaus-main", name = "Malicious URLs", description = "Direct text list of actively malicious URLs.",
                url = "https://urlhaus.abuse.ch/downloads/text/", format = "hosts", isEnabled = false, parentId = "urlhaus"
            ),

            // 9. Malicious Domain Blocklist
            BlocklistEntity(
                id = "maliciousdomains", name = "Malicious Domain Blocklist", description = "Aggregated blocklist protecting against malicious hosts.",
                url = "", format = "hosts", isEnabled = false, authorUrl = "https://github.com/romainmarcoux/malicious-domains", author = "romainmarcoux"
            ),
            BlocklistEntity(
                id = "malicious-main", name = "Malicious Domains", description = "Aggregated list of malicious domains.",
                url = "https://raw.githubusercontent.com/romainmarcoux/malicious-domains/main/full-domains-aa.txt", format = "hosts", isEnabled = false, parentId = "maliciousdomains"
            ),

            // 10. OISD (5 Profiles)
            BlocklistEntity(
                id = "oisd", name = "OISD", description = "Highly curated to block ads, trackers, and malware with zero false positives.", 
                url = "", format = "domain", isEnabled = false, authorUrl = "https://oisd.nl", author = "sjhgvr"
            ),
            BlocklistEntity(
                id = "oisd-big", name = "Big", description = "Full-strength ad-blocking, tracking, and malware protection.", 
                url = "https://big.oisd.nl", format = "domain", isEnabled = true, parentId = "oisd"
            ),
            BlocklistEntity(
                id = "oisd-small", name = "Small", description = "Essential protection for devices or slower connections.", 
                url = "https://small.oisd.nl", format = "domain", isEnabled = false, parentId = "oisd"
            ),
            BlocklistEntity(
                id = "oisd-basic", name = "Basic", description = "Essential protection with zero false positives.", 
                url = "https://basic.oisd.nl", format = "domain", isEnabled = false, parentId = "oisd"
            ),
            BlocklistEntity(
                id = "oisd-nsfw", name = "NSFW", description = "Adult content blocking (porn, shock, gore).", 
                url = "https://nsfw.oisd.nl", format = "domain", isEnabled = false, parentId = "oisd"
            ),
            BlocklistEntity(
                id = "oisd-nsfw-small", name = "NSFW Small", description = "Light adult content protection.", 
                url = "https://nsfw-small.oisd.nl", format = "domain", isEnabled = false, parentId = "oisd"
            ),
            
            // 11. Steven Black (5 Profiles)
            BlocklistEntity(
                id = "stevenblack", name = "Steven Black", description = "Consolidated hosts file merging multiple curated lists.", 
                url = "", format = "hosts", isEnabled = true, authorUrl = "https://github.com/StevenBlack/hosts", author = "Steven Black"
            ),
            BlocklistEntity(
                id = "stevenblack-unified", name = "Unified", description = "Unified protection against ads, trackers, and phishing.", 
                url = "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts", format = "hosts", isEnabled = true, parentId = "stevenblack"
            ),
            BlocklistEntity(
                id = "stevenblack-social", name = "Social", description = "Standard rules plus social media trackers.", 
                url = "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/social/hosts", format = "hosts", isEnabled = false, parentId = "stevenblack"
            ),
            BlocklistEntity(
                id = "stevenblack-gambling", name = "Gambling", description = "Standard rules plus gambling protection.", 
                url = "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/gambling/hosts", format = "hosts", isEnabled = false, parentId = "stevenblack"
            ),
            BlocklistEntity(
                id = "stevenblack-fakenews", name = "Fakenews", description = "Standard rules plus fake news protection.", 
                url = "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/fakenews/hosts", format = "hosts", isEnabled = false, parentId = "stevenblack"
            ),
            BlocklistEntity(
                id = "stevenblack-porn", name = "Porn", description = "Standard rules plus adult content protection.", 
                url = "https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/porn/hosts", format = "hosts", isEnabled = false, parentId = "stevenblack"
            ),
            
            // 12. AdAway
            BlocklistEntity(
                id = "adaway", name = "AdAway", description = "The official filter list from the AdAway project.", 
                url = "", format = "hosts", isEnabled = true, authorUrl = "https://adaway.org", author = "AdAway Team"
            ),
            BlocklistEntity(
                id = "adaway-main", name = "AdAway", description = "Primary mobile-first filtering rules.", 
                url = "https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt", format = "hosts", isEnabled = true, parentId = "adaway"
            ),
            
            // 13. Hagezi DNS Blocklists (replaces discontinued Energized Protection)
            BlocklistEntity(
                id = "energized", name = "Hagezi", description = "High-quality, actively maintained DNS blocklists with multiple protection tiers.", 
                url = "", format = "hosts", isEnabled = false, authorUrl = "https://github.com/hagezi/dns-blocklists", author = "HaGeZi"
            ),
            BlocklistEntity(
                id = "energized-spark", name = "Light", description = "Lightweight, low false-positive list. Ideal for most users.", 
                url = "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/hosts/light.txt", format = "hosts", isEnabled = false, parentId = "energized"
            ),
            BlocklistEntity(
                id = "energized-blu", name = "Normal", description = "Balanced protection that blocks ads, trackers, and more.", 
                url = "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/hosts/multi.txt", format = "hosts", isEnabled = false, parentId = "energized"
            ),
            BlocklistEntity(
                id = "energized-ultimate", name = "Pro", description = "Comprehensive blocking for maximum privacy.", 
                url = "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/hosts/pro.txt", format = "hosts", isEnabled = false, parentId = "energized"
            ),

            // 14. DuckDuckGo
            BlocklistEntity(
                id = "ddg", name = "DuckDuckGo", description = "Data-driven tracker protection from DuckDuckGo.", 
                url = "", format = "json", isEnabled = false, authorUrl = "https://github.com/duckduckgo/tracker-blocklists", author = "DuckDuckGo"
            ),
            BlocklistEntity(
                id = "ddg-main", name = "Tracker Radar", description = "The official Tracker Data Set.", 
                url = "https://raw.githubusercontent.com/duckduckgo/tracker-blocklists/main/web/tds.json", format = "json", isEnabled = false, parentId = "ddg"
            ),
            
            // 15. Exodus Privacy
            BlocklistEntity(
                id = "exodus", name = "Exodus Privacy", description = "Identifies trackers embedded within Android applications.", 
                url = "", format = "json", isEnabled = false, authorUrl = "https://exodus-privacy.eu.org", author = "Exodus Privacy"
            ),
            BlocklistEntity(
                id = "exodus-main", name = "Exodus Privacy", description = "Protection against analytics and tracking SDKs.", 
                url = "https://reports.exodus-privacy.eu.org/api/trackers", format = "json", isEnabled = false, parentId = "exodus"
            ),
            
            // 16. The Block List Project (8 Profiles)
            BlocklistEntity(
                id = "blp", name = "The Block List Project", description = "Specialized filtering lists for specific categories.", 
                url = "", format = "hosts", isEnabled = false, authorUrl = "https://blocklistproject.github.io", author = "The Block List Project"
            ),
            BlocklistEntity(
                id = "blp-ads", name = "Ads", description = "Blocking advertising domains.", 
                url = "https://blocklistproject.github.io/Lists/ads.txt", format = "hosts", isEnabled = false, parentId = "blp"
            ),
            BlocklistEntity(
                id = "blp-tracking", name = "Tracking", description = "Blocking tracking and telemetry services.", 
                url = "https://blocklistproject.github.io/Lists/tracking.txt", format = "hosts", isEnabled = false, parentId = "blp"
            ),
            BlocklistEntity(
                id = "blp-malware", name = "Malware", description = "Protection against domains distributing malicious software.", 
                url = "https://blocklistproject.github.io/Lists/malware.txt", format = "hosts", isEnabled = false, parentId = "blp"
            ),
            BlocklistEntity(
                id = "blp-phishing", name = "Phishing", description = "Targeted blocking for phishing attacks.", 
                url = "https://blocklistproject.github.io/Lists/phishing.txt", format = "hosts", isEnabled = false, parentId = "blp"
            ),
            BlocklistEntity(
                id = "blp-gambling", name = "Gambling", description = "Blocking gambling and betting sites.", 
                url = "https://blocklistproject.github.io/Lists/gambling.txt", format = "hosts", isEnabled = false, parentId = "blp"
            ),
            BlocklistEntity(
                id = "blp-porn", name = "Porn", description = "Blocking adult content.", 
                url = "https://blocklistproject.github.io/Lists/porn.txt", format = "hosts", isEnabled = false, parentId = "blp"
            ),
            BlocklistEntity(
                id = "blp-social", name = "Social", description = "Blocking social media sites.", 
                url = "https://blocklistproject.github.io/Lists/social.txt", format = "hosts", isEnabled = false, parentId = "blp"
            ),
            BlocklistEntity(
                id = "blp-scam", name = "Scam", description = "Blocking scam and fraud sites.", 
                url = "https://blocklistproject.github.io/Lists/scam.txt", format = "hosts", isEnabled = false, parentId = "blp"
            )
        )
        Log.i("BlocklistRepo", "Initializing ${defaults.size} default lists")
        blocklistDao.insertAll(defaults)
        
        // Apply Balanced profile by default on first launch
        applyProfile("Balanced")
    }

    suspend fun refreshAllWithUrls() = withContext(Dispatchers.IO) {
        notificationHelper.showUpdateStarted()
        try {
            Log.i("BlocklistRepo", "Refreshing all lists with URLs")
            val allLists = blocklistDao.getAll().first()
            
            // Force re-initialization if new profiles are missing to clear duplicates
            if (allLists.isEmpty() || allLists.none { it.id == "oisd-basic" }) {
                initializeDefaults()
            }
            
            // Only download lists that are actually enabled to save time and bandwidth
            blocklistDao.getAll().first().filter { it.url.isNotEmpty() && it.isEnabled }.forEach { list ->
                updateList(list.url, list.format, list.id) { size ->
                    blocklistDao.update(list.copy(lastUpdated = System.currentTimeMillis(), entryCount = size))
                }
            }

            customListDao.getAll().first().forEach { list ->
                updateList(list.url, list.format, "custom-${list.id}") { size ->
                    customListDao.update(list.copy(lastUpdated = System.currentTimeMillis(), entryCount = size))
                }
            }
            
            blocklistEngine.loadRules()
            notificationHelper.showUpdateComplete()
        } catch (e: Exception) {
            Log.e("BlocklistRepo", "Error refreshing lists", e)
            notificationHelper.cancelUpdateNotification()
            throw e
        }
    }

    private suspend fun updateList(url: String, format: String, sourceId: String, onComplete: suspend (Int) -> Unit) {
        try {
            val domains = downloadAndParse(url, format)
            if (domains.isNotEmpty()) {
                syncDomainsToDatabase(sourceId, domains)
                onComplete(domains.size)
            }
        } catch (e: Exception) {
            Log.e("BlocklistRepo", "Failed to update $sourceId", e)
        }
    }

    suspend fun addCustomList(name: String, url: String, format: String) {
        val newList = CustomListEntity(name = name, url = url, format = format)
        customListDao.insert(newList)
        refreshAllWithUrls() 
    }

    suspend fun removeCustomList(list: CustomListEntity) {
        customListDao.delete(list)
        domainDao.deleteBySource("custom-${list.id}")
        blocklistEngine.loadRules()
    }

    suspend fun toggleCustomList(list: CustomListEntity) {
        val newState = !list.isEnabled
        customListDao.update(list.copy(isEnabled = newState))
        blocklistEngine.loadRules()
    }

    suspend fun downloadAndParse(url: String, format: String): List<String> {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()
        
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val content = response.body?.string() ?: return emptyList()
                
                return when (format) {
                    "hosts" -> hostsParser.parse(content)
                    "domain" -> domainParser.parse(content)
                    "adblock" -> adblockParser.parse(content)
                    "json" -> jsonParser.parse(content)
                    else -> emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("BlocklistRepo", "Network error downloading $url", e)
            return emptyList()
        }
    }
    
    suspend fun syncDomainsToDatabase(listId: String, domains: List<String>) {
        domainDao.deleteBySource(listId)
        val entities = domains.map { DomainEntity(domain = it, sourceListId = listId) }
        domainDao.insertAll(entities)
    }

    suspend fun applyProfile(profileName: String) = withContext(Dispatchers.IO) {
        val allLists = blocklistDao.getAll().first()
        val toEnable = when (profileName) {
            "Ultra" -> listOf(
                "oisd", "oisd-big", 
                "stevenblack", "stevenblack-unified",
                "adaway", "adaway-main",
                "1hosts", "1hosts-pro",
                "goodbyeads", "goodbyeads-extended",
                "energized", "energized-ultimate"
            )
            "Minimal" -> listOf(
                "oisd", "oisd-small", 
                "stevenblack", "stevenblack-unified",
                "adaway", "adaway-main",
                "energized", "energized-spark"
            )
            else -> listOf( // Balanced
                "oisd", "oisd-big", 
                "stevenblack", "stevenblack-unified", 
                "adaway", "adaway-main",
                "1hosts", "1hosts-lite",
                "energized", "energized-blu"
            )
        }

        val updatedLists = allLists.map { list ->
            list.copy(isEnabled = toEnable.contains(list.id))
        }
        
        blocklistDao.updateAll(updatedLists)
        blocklistEngine.loadRules()
    }
}
