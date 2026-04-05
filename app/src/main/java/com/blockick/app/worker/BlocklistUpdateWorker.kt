package com.blockick.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blockick.app.data.repository.BlocklistRepository
import com.blockick.app.domain.engine.BlocklistEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BlocklistUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: BlocklistRepository,
    private val engine: BlocklistEngine
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.refreshAllWithUrls()
            engine.loadRules() // Reload rules in engine after DB update
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
