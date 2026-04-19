package com.aiawareness.diary.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.aiawareness.diary.MainDispatcherRule
import com.aiawareness.diary.domain.AiDiaryAutoGenerationCoordinator
import com.aiawareness.diary.domain.AutoGenerationOutcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AiDiaryAutoGenerationWorkerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appContext: Context = mock()
    private val coordinator: AiDiaryAutoGenerationCoordinator = mock()

    @Test
    fun doWork_withCompleteConfigAndRecords_generatesTodayDiary() = runTest {
        whenever(appContext.applicationContext).thenReturn(appContext)
        whenever(coordinator.generateTodayIfNeeded()).thenReturn(
            AutoGenerationOutcome(generated = true, failed = false)
        )
        val worker = TestListenableWorkerBuilder<AiDiaryAutoGenerationWorker>(appContext)
            .setWorkerFactory(
                object : androidx.work.WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters
                    ): ListenableWorker =
                        AiDiaryAutoGenerationWorker(appContext, workerParameters, coordinator)
                }
            )
            .build()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(coordinator).generateTodayIfNeeded()
    }
}
