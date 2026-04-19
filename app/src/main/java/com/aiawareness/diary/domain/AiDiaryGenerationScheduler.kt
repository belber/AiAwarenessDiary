package com.aiawareness.diary.domain

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aiawareness.diary.work.AiDiaryAutoGenerationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiDiaryGenerationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleDaily(hour: Int, minute: Int) {
        val initialDelay = computeInitialDelay(hour = hour, minute = minute)
        val request = PeriodicWorkRequestBuilder<AiDiaryAutoGenerationWorker>(1, java.util.concurrent.TimeUnit.DAYS)
            .setInitialDelay(initialDelay)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelDaily() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private fun computeInitialDelay(hour: Int, minute: Int): Duration {
        val now = LocalDateTime.now()
        var nextRun = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(now, nextRun)
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "ai_diary_daily_generation"
    }
}
