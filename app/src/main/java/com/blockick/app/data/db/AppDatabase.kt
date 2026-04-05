package com.blockick.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blockick.app.data.db.dao.*
import com.blockick.app.data.db.entities.*

@Database(
    entities = [
        BlocklistEntity::class,
        DomainEntity::class,
        AllowlistEntity::class,
        QueryLogEntity::class,
        StatsEntity::class,
        UserBlocklistEntity::class,
        ExcludedAppEntity::class,
        CustomListEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blocklistDao(): BlocklistDao
    abstract fun domainDao(): DomainDao
    abstract fun allowlistDao(): AllowlistDao
    abstract fun queryLogDao(): QueryLogDao
    abstract fun statsDao(): StatsDao
    abstract fun userBlocklistDao(): UserBlocklistDao
    abstract fun excludedAppDao(): ExcludedAppDao
    abstract fun customListDao(): CustomListDao
}

