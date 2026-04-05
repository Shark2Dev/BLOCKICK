package com.blockick.app

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.blockick.app.data.repository.BlocklistRepository
import com.blockick.app.worker.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BlockickApp : Application(), Configuration.Provider {

    @Inject
    lateinit var blocklistRepository: BlocklistRepository

    @Inject
    lateinit var workManagerScheduler: WorkManagerScheduler

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.i("BlockickApp", "Application started")
        
        applicationScope.launch {
            if (blocklistRepository.isDatabaseEmpty()) {
                blocklistRepository.initializeDefaults()
            }
            
            workManagerScheduler.scheduleOrCancel()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}

