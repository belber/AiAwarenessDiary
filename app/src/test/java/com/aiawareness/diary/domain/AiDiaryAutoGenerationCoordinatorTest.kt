package com.aiawareness.diary.domain

import com.aiawareness.diary.MainDispatcherRule
import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.data.model.UserSettings
import com.aiawareness.diary.data.remote.OpenAIResponse
import com.aiawareness.diary.data.remote.OpenAIApiService
import com.aiawareness.diary.data.remote.OpenAIApiServiceFactory
import com.aiawareness.diary.data.repository.DiaryRepository
import com.aiawareness.diary.data.repository.RecordRepository
import com.aiawareness.diary.data.repository.SettingsRepository
import com.aiawareness.diary.util.DateUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AiDiaryAutoGenerationCoordinatorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val recordRepository: RecordRepository = mock()
    private val diaryRepository: DiaryRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val apiServiceFactory: OpenAIApiServiceFactory = mock()
    private val apiService: OpenAIApiService = mock()

    @Test
    fun generateRecentMissingDiaries_withSixRecentRecordDates_generatesMostRecentFiveDates() = runTest {
        val settings = completeSettings()
        val recentDates = (0..5).map { LocalDate.now().minusDays(it.toLong()).toString() }
        stubSettings(settings)
        whenever(recordRepository.getDatesWithRecords()).thenReturn(recentDates)
        recentDates.forEach { date ->
            whenever(diaryRepository.hasDiaryForDate(date)).thenReturn(false)
            whenever(recordRepository.getRecordsByDate(date)).thenReturn(recordsFor(date))
        }
        whenever(apiServiceFactory.create(settings.apiEndpoint)).thenReturn(apiService)
        whenever(apiService.generateCompletion(any(), any())).thenReturn(successfulResponse())

        val coordinator = createCoordinator()

        val outcome = coordinator.generateRecentMissingDiaries()

        assertEquals(5, outcome.generatedCount)
        assertFalse(outcome.failed)
        verify(apiServiceFactory, org.mockito.kotlin.times(5)).create(settings.apiEndpoint)
        verify(apiService, org.mockito.kotlin.times(5)).generateCompletion(any(), any())
        val diaryCaptor = argumentCaptor<Diary>()
        verify(diaryRepository, org.mockito.kotlin.times(5)).saveDiary(diaryCaptor.capture())
        assertEquals(recentDates.take(5), diaryCaptor.allValues.map { it.date })
    }

    @Test
    fun generateRecentMissingDiaries_withIncompleteConfig_skipsGeneration() = runTest {
        stubSettings(UserSettings(apiEndpoint = "https://api.example.com/", apiKey = "", modelName = "gpt-4o-mini"))
        whenever(recordRepository.getDatesWithRecords()).thenReturn(
            listOf(LocalDate.now().toString())
        )

        val coordinator = createCoordinator()

        val outcome = coordinator.generateRecentMissingDiaries()

        assertEquals(0, outcome.generatedCount)
        assertFalse(outcome.failed)
        verify(apiServiceFactory, never()).create(any())
        verify(diaryRepository, never()).saveDiary(any())
    }

    @Test
    fun generateTodayIfNeeded_withoutRecordsForToday_skipsGeneration() = runTest {
        val settings = completeSettings()
        val today = DateUtil.getCurrentDate()
        stubSettings(settings)
        whenever(recordRepository.getRecordsByDate(today)).thenReturn(emptyList())
        whenever(diaryRepository.hasDiaryForDate(today)).thenReturn(false)

        val coordinator = createCoordinator()

        val outcome = coordinator.generateTodayIfNeeded()

        assertFalse(outcome.generated)
        assertFalse(outcome.failed)
        verify(apiServiceFactory, never()).create(any())
        verify(diaryRepository, never()).saveDiary(any())
    }

    @Test
    fun generateTodayIfNeeded_withExistingUpToDateDiary_skipsGeneration() = runTest {
        val settings = completeSettings()
        val today = DateUtil.getCurrentDate()
        val existingDiary = Diary(
            id = 1,
            date = today,
            aiDiary = "已生成日记",
            aiInsight = "已生成启发",
            generatedAt = 2_000L,
            updatedAt = 2_000L
        )
        stubSettings(settings)
        whenever(diaryRepository.getDiaryByDate(today)).thenReturn(existingDiary)
        whenever(recordRepository.getRecordsByDate(today)).thenReturn(
            listOf(
                Record(
                    id = 1,
                    date = today,
                    time = "18:00",
                    content = "已有记录",
                    createdAt = 1_000L,
                    updatedAt = 1_000L
                )
            )
        )

        val coordinator = createCoordinator()

        val outcome = coordinator.generateTodayIfNeeded()

        assertFalse(outcome.generated)
        assertFalse(outcome.failed)
        verify(apiServiceFactory, never()).create(any())
    }

    @Test
    fun generateTodayIfNeeded_withNewerRecordThanExistingDiary_regeneratesDiary() = runTest {
        val settings = completeSettings()
        val today = DateUtil.getCurrentDate()
        val existingDiary = Diary(
            id = 1,
            date = today,
            aiDiary = "旧日记",
            aiInsight = "旧启发",
            generatedAt = 1_000L,
            updatedAt = 1_000L
        )
        stubSettings(settings)
        whenever(diaryRepository.hasDiaryForDate(today)).thenReturn(true)
        whenever(diaryRepository.getDiaryByDate(today)).thenReturn(existingDiary)
        whenever(recordRepository.getRecordsByDate(today)).thenReturn(
            listOf(
                Record(
                    id = 1,
                    date = today,
                    time = "22:05",
                    content = "晚些时候补充的新记录",
                    createdAt = 2_000L,
                    updatedAt = 2_000L
                )
            )
        )
        whenever(apiServiceFactory.create(settings.apiEndpoint)).thenReturn(apiService)
        whenever(apiService.generateCompletion(any(), any())).thenReturn(successfulResponse())

        val coordinator = createCoordinator()

        val outcome = coordinator.generateTodayIfNeeded()

        assertEquals(true, outcome.generated)
        assertFalse(outcome.failed)
        verify(apiServiceFactory).create(settings.apiEndpoint)
        verify(apiService).generateCompletion(any(), any())
        verify(diaryRepository).saveDiary(any())
    }

    @Test
    fun generateRecentMissingDiaries_regeneratesExistingDiaryWhenRecordsAreNewer() = runTest {
        val settings = completeSettings()
        val recentDates = (0..4).map { LocalDate.now().minusDays(it.toLong()).toString() }
        stubSettings(settings)
        whenever(recordRepository.getDatesWithRecords()).thenReturn(recentDates)
        val staleDate = recentDates.first()
        recentDates.forEachIndexed { index, date ->
            whenever(recordRepository.getRecordsByDate(date)).thenReturn(
                listOf(
                    Record(
                        id = index.toLong() + 1,
                        date = date,
                        time = "09:30",
                        content = "记录 $date",
                        createdAt = 5_000L + index,
                        updatedAt = 5_000L + index
                    )
                )
            )
            whenever(diaryRepository.hasDiaryForDate(date)).thenReturn(date == staleDate)
            whenever(diaryRepository.getDiaryByDate(date)).thenReturn(
                if (date == staleDate) {
                    Diary(
                        id = 9,
                        date = date,
                        aiDiary = "旧日记",
                        aiInsight = "旧启发",
                        generatedAt = 1_000L,
                        updatedAt = 1_000L
                    )
                } else {
                    null
                }
            )
        }
        whenever(apiServiceFactory.create(settings.apiEndpoint)).thenReturn(apiService)
        whenever(apiService.generateCompletion(any(), any())).thenReturn(successfulResponse())

        val coordinator = createCoordinator()

        val outcome = coordinator.generateRecentMissingDiaries()

        assertEquals(5, outcome.generatedCount)
        assertFalse(outcome.failed)
        verify(apiServiceFactory, org.mockito.kotlin.times(5)).create(settings.apiEndpoint)
        verify(apiService, org.mockito.kotlin.times(5)).generateCompletion(any(), any())
        verify(diaryRepository, org.mockito.kotlin.times(5)).saveDiary(any())
    }

    private fun createCoordinator(): AiDiaryAutoGenerationCoordinator =
        AiDiaryAutoGenerationCoordinator(
            recordRepository = recordRepository,
            diaryRepository = diaryRepository,
            settingsRepository = settingsRepository,
            apiServiceFactory = apiServiceFactory
        )

    private fun stubSettings(settings: UserSettings) {
        whenever(settingsRepository.userSettings).thenReturn(flowOf(settings))
    }

    private fun completeSettings(): UserSettings =
        UserSettings(
            nickname = "蜉蝣",
            apiEndpoint = "https://api.example.com/",
            apiKey = "secret",
            modelName = "gpt-4o-mini"
        )

    private fun recordsFor(date: String): List<Record> =
        listOf(
            Record(
                id = date.hashCode().toLong(),
                date = date,
                time = "09:30",
                content = "记录 $date"
            )
        )

    private fun successfulResponse(): Response<OpenAIResponse> =
        Response.success(
            OpenAIResponse(
                id = "resp_1",
                choices = listOf(
                    OpenAIResponse.Choice(
                        index = 0,
                        message = OpenAIResponse.Choice.Message(
                            role = "assistant",
                            content = "【日记】\n今天整理了一条记录。\n\n【启发】\n保持观察。"
                        ),
                        finishReason = "stop"
                    )
                ),
                usage = null
            )
        )
}
