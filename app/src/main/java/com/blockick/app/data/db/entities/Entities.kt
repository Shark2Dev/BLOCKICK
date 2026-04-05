package com.blockick.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocklists")
data class BlocklistEntity(
    @PrimaryKey val id: String, // e.g., "oisd-basic"
    val name: String,
    val description: String = "",
    val url: String,
    val format: String, // "hosts", "domain", "adblock", "json"
    val isEnabled: Boolean = true,
    val lastUpdated: Long = 0,
    val entryCount: Int = 0,
    val parentId: String? = null, // For sub-configurations like "OISD" -> "OISD Basic"
    val authorUrl: String = "",
    val author: String = ""
)

@Entity(tableName = "custom_blocklists")
data class CustomListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val format: String, // "hosts", "domain", "adblock"
    val isEnabled: Boolean = true,
    val lastUpdated: Long = 0,
    val entryCount: Int = 0
)

@Entity(
    tableName = "domains",
    indices = [androidx.room.Index(value = ["sourceListId"])]
)
data class DomainEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val sourceListId: String
)

@Entity(tableName = "allowlist")
data class AllowlistEntity(
    @PrimaryKey val domain: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_blocklist")
data class UserBlocklistEntity(
    @PrimaryKey val domain: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "query_logs")
data class QueryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val isBlocked: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val appPackage: String? = null,
    val queryType: String = "A"
)

@Entity(tableName = "daily_stats")
data class StatsEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val blockedCount: Int = 0,
    val totalCount: Int = 0
)

@Entity(tableName = "excluded_apps")
data class ExcludedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val addedAt: Long = System.currentTimeMillis()
)

