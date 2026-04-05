package com.blockick.app.di

import android.content.Context
import androidx.room.Room
import com.blockick.app.data.db.AppDatabase
import com.blockick.app.data.db.dao.*
import com.blockick.app.data.parsers.AdblockParser
import com.blockick.app.data.parsers.DomainListParser
import com.blockick.app.data.parsers.HostsFileParser
import com.blockick.app.data.parsers.JsonParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "Blockick.db"
        )
            // Manual migrations are required from now on to prevent data loss.
            .build()
    }

    @Provides
    fun provideBlocklistDao(db: AppDatabase): BlocklistDao = db.blocklistDao()

    @Provides
    fun provideDomainDao(db: AppDatabase): DomainDao = db.domainDao()

    @Provides
    fun provideAllowlistDao(db: AppDatabase): AllowlistDao = db.allowlistDao()

    @Provides
    fun provideQueryLogDao(db: AppDatabase): QueryLogDao = db.queryLogDao()

    @Provides
    fun provideStatsDao(db: AppDatabase): StatsDao = db.statsDao()

    @Provides
    fun provideUserBlocklistDao(db: AppDatabase): UserBlocklistDao = db.userBlocklistDao()

    @Provides
    fun provideExcludedAppDao(db: AppDatabase): ExcludedAppDao = db.excludedAppDao()

    @Provides
    fun provideCustomListDao(db: AppDatabase): CustomListDao = db.customListDao()

    @Provides
    @Singleton
    fun provideHostsParser() = HostsFileParser()

    @Provides
    @Singleton
    fun provideDomainParser() = DomainListParser()

    @Provides
    @Singleton
    fun provideAdblockParser() = AdblockParser()

    @Provides
    @Singleton
    fun provideJsonParser() = JsonParser()
}

