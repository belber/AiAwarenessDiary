package com.aiawareness.diary.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aiawareness.diary.domain.AiDiaryAutoGenerationCoordinator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AiDiaryAutoGenerationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val coordinator: AiDiaryAutoGenerationCoordinator
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        coordinator.generateTodayIfNeeded()
        return Result.success()
    }
}
