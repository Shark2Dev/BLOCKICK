package com.blockick.app.data.db.dao

import androidx.room.*
import com.blockick.app.data.db.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BlocklistDao {
    @Query("SELECT * FROM blocklists")
    fun getAll(): Flow<List<BlocklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(blocklists: List<BlocklistEntity>)


    @Update
    suspend fun update(blocklist: BlocklistEntity)

    @Update
    suspend fun updateAll(blocklists: List<BlocklistEntity>)

    @Delete
    suspend fun delete(blocklist: BlocklistEntity)

    @Query("DELETE FROM blocklists")
    suspend fun deleteAll()
}

@Dao
interface DomainDao {
    @Query("""
        SELECT d.domain 
        FROM domains d
        INNER JOIN blocklists b ON d.sourceListId = b.id
        WHERE b.isEnabled = 1
        UNION
        SELECT d.domain
        FROM domains d
        INNER JOIN custom_blocklists c ON d.sourceListId = 'custom-' || c.id
        WHERE c.isEnabled = 1
    """)
    suspend fun getAllEnabledDomains(): List<String>

    @Query("SELECT domain FROM domains")
    suspend fun getAllDomains(): List<String>

    @Query("DELETE FROM domains WHERE sourceListId = :listId")
    suspend fun deleteBySource(listId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(domains: List<DomainEntity>)

    @Query("DELETE FROM domains")
    suspend fun deleteAll()
}

@Dao
interface AllowlistDao {
    @Query("SELECT * FROM allowlist")
    fun getAll(): Flow<List<AllowlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(allowlist: AllowlistEntity)

    @Delete
    suspend fun delete(allowlist: AllowlistEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM allowlist WHERE domain = :domain)")
    suspend fun isAllowed(domain: String): Boolean
}

@Dao
interface QueryLogDao {
    @Query("SELECT * FROM query_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<QueryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: QueryLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<QueryLogEntity>)

    @Query("DELETE FROM query_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM query_logs WHERE domain = :domain")
    suspend fun getOccurrenceCount(domain: String): Int

    @Query("SELECT isBlocked FROM query_logs WHERE domain = :domain ORDER BY timestamp DESC LIMIT 1")
    suspend fun isDomainBlocked(domain: String): Boolean?

    @Query("SELECT queryType FROM query_logs WHERE domain = :domain ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastQueryType(domain: String): String?

    @Query("SELECT domain, COUNT(*) as count FROM query_logs WHERE isBlocked = 1 GROUP BY domain ORDER BY count DESC LIMIT :limit")
    fun getTopBlockedDomains(limit: Int = 5): Flow<List<TopDomain>>

    @Query("SELECT (timestamp / 3600000) * 3600000 as hour, COUNT(*) as count FROM query_logs WHERE isBlocked = 1 AND timestamp > :since GROUP BY hour ORDER BY hour ASC")
    fun getHourlyStats(since: Long): Flow<List<HourlyStat>>

    data class TopDomain(val domain: String, val count: Int)
    data class HourlyStat(val hour: Long, val count: Int)
}

@Dao
interface UserBlocklistDao {
    @Query("SELECT * FROM user_blocklist")
    fun getAll(): Flow<List<UserBlocklistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blocklist: UserBlocklistEntity)

    @Delete
    suspend fun delete(blocklist: UserBlocklistEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM user_blocklist WHERE domain = :domain)")
    suspend fun isBlocked(domain: String): Boolean
}

@Dao
interface StatsDao {
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatsForDate(date: String): StatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: StatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<StatsEntity>)

    @Query("UPDATE daily_stats SET blockedCount = blockedCount + 1, totalCount = totalCount + 1 WHERE date = :date")
    suspend fun incrementBlocked(date: String)

    @Query("UPDATE daily_stats SET totalCount = totalCount + 1 WHERE date = :date")
    suspend fun incrementTotal(date: String)

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun getLast7Days(): Flow<List<StatsEntity>>

    @Query("SELECT SUM(blockedCount) FROM daily_stats")
    fun getTotalBlocked(): Flow<Int?>
}

@Dao
interface ExcludedAppDao {
    @Query("SELECT * FROM excluded_apps")
    fun getAll(): Flow<List<ExcludedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: ExcludedAppEntity)

    @Delete
    suspend fun delete(app: ExcludedAppEntity)

    @Query("SELECT packageName FROM excluded_apps")
    suspend fun getExcludedPackageNames(): List<String>
}

@Dao
interface CustomListDao {
    @Query("SELECT * FROM custom_blocklists")
    fun getAll(): Flow<List<CustomListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: CustomListEntity)

    @Update
    suspend fun update(list: CustomListEntity)

    @Delete
    suspend fun delete(list: CustomListEntity)
}

