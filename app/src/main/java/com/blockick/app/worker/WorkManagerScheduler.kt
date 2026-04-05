package com.blockick.app.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: com.blockick.app.data.preferences.AppPreferences
) {
    companion object {
        const val WORK_NAME = "blocklist_update_work"
    }

    suspend fun scheduleOrCancel() {
        val workManager = WorkManager.getInstance(context)
        val autoUpdate = appPreferences.autoUpdate.first()
        
        if (autoUpdate) {
            val frequency = appPreferences.updateFrequency.first()
            schedulePeriodicWork(frequency)
        } else {
            cancelPeriodicWork()
        }
    }

    private fun schedulePeriodicWork(frequencyDays: Int) {
        val workManager = WorkManager.getInstance(context)
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val repeatInterval = frequencyDays.toLong()
        
        val periodicWorkRequest = PeriodicWorkRequestBuilder<BlocklistUpdateWorker>(
            repeatInterval, java.util.concurrent.TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(repeatInterval, java.util.concurrent.TimeUnit.DAYS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    private fun cancelPeriodicWork() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME)
    }

    fun runOnceNow() {
        val workManager = WorkManager.getInstance(context)
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<BlocklistUpdateWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(oneTimeWorkRequest)
    }
}
