package com.aiawareness.diary.ui.screens

import android.net.Uri
import com.aiawareness.diary.MainDispatcherRule
import com.aiawareness.diary.data.local.RecordPhotoStorage
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.data.model.UserSettings
import com.aiawareness.diary.data.remote.OpenAIRequest
import com.aiawareness.diary.data.remote.OpenAIResponse
import com.aiawareness.diary.data.remote.OpenAIApiService
import com.aiawareness.diary.data.remote.OpenAIApiServiceFactory
import com.aiawareness.diary.data.repository.DiaryRepository
import com.aiawareness.diary.data.repository.RecordRepository
import com.aiawareness.diary.data.repository.SettingsRepository
import com.aiawareness.diary.domain.AiDiaryAutoGenerationCoordinator
import com.aiawareness.diary.domain.AiDiaryGenerationScheduler
import com.aiawareness.diary.domain.AutoGenerationOutcome
import com.aiawareness.diary.domain.CatchUpGenerationOutcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val recordRepository: RecordRepository = mock()
    private val diaryRepository: DiaryRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val apiServiceFactory: OpenAIApiServiceFactory = mock()
    private val apiService: OpenAIApiService = mock()
    private val photoStorage: RecordPhotoStorage = mock()
    private val autoGenerationCoordinator: AiDiaryAutoGenerationCoordinator = mock()
    private val aiDiaryGenerationScheduler: AiDiaryGenerationScheduler = mock()

    @Test
    fun updateMonth_updatesCurrentYearAndMonth() = runTest {
        stubInitialLoads()
        val viewModel = createViewModel()

        viewModel.updateMonth(year = 2027, month = 2)

        assertEquals(2027, viewModel.uiState.value.currentYear)
        assertEquals(2, viewModel.uiState.value.currentMonth)
    }

    @Test
    fun generateDiary_withoutApiConfig_setsErrorAndDoesNotPersistDiary() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(flowOf(UserSettings()))
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(false))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(emptyList())
        whenever(recordRepository.getDatesWithRecords()).thenReturn(emptyList())
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(0)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(diaryRepository.hasDiaryForDate(any())).thenReturn(false)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = false)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.generateDiary()
        advanceUntilIdle()

        assertEquals("请先配置 API", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isGeneratingDiary)
        verify(diaryRepository, never()).saveDiary(any())
    }

    @Test
    fun generateDiary_withApiConfigButNoRecords_setsEmptyStateError() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(
            flowOf(
                UserSettings(
                    apiEndpoint = "https://api.example.com/",
                    apiKey = "secret",
                    modelName = "qwen-plus"
                )
            )
        )
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(true))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(emptyList())
        whenever(recordRepository.getDatesWithRecords()).thenReturn(emptyList())
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(0)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(diaryRepository.hasDiaryForDate(any())).thenReturn(false)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = false)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.generateDiary()
        advanceUntilIdle()

        assertEquals("没有记录可生成日记", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isGeneratingDiary)
        verify(diaryRepository, never()).saveDiary(any())
    }

    @Test
    fun saveRecord_delegatesInsertAndRefreshesLists() = runTest {
        stubInitialLoads()
        whenever(recordRepository.insertRecord(any())).thenReturn(1L)

        val viewModel = createViewModel()
        advanceUntilIdle()
        org.mockito.Mockito.clearInvocations(recordRepository)

        viewModel.saveRecord("记录一条新的觉察")
        advanceUntilIdle()

        verify(recordRepository, atLeastOnce()).insertRecord(any())
        verify(recordRepository, atLeastOnce()).getRecordsByDate(any())
        verify(recordRepository, atLeastOnce()).getDatesWithRecords()
    }

    @Test
    fun saveRecord_withBlankContent_setsErrorAndDoesNotInsert() = runTest {
        stubInitialLoads()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.saveRecord("   ")
        advanceUntilIdle()

        assertEquals("记录内容不能为空", viewModel.uiState.value.error)
        verify(recordRepository, never()).insertRecord(any())
    }

    @Test
    fun saveRecord_withPendingPhoto_savesImportedPathAndClearsPendingPhoto() = runTest {
        stubInitialLoads()
        whenever(recordRepository.insertRecord(any())).thenReturn(1L)
        val photoUri: Uri = mock()
        whenever(photoStorage.persistImportedPhoto(photoUri)).thenReturn("/files/record_photos/p1.jpg")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingPhoto(photoUri)
        viewModel.saveRecord("记录一条新的觉察")
        advanceUntilIdle()

        verify(photoStorage).persistImportedPhoto(photoUri)
        verify(recordRepository).insertRecord(
            check { record ->
                assertEquals("/files/record_photos/p1.jpg", record.photoPath)
            }
        )
        assertNull(viewModel.uiState.value.pendingPhotoUri)
        assertNull(viewModel.uiState.value.pendingCapturedPhotoFile)
    }

    @Test
    fun importPendingPhoto_stagesImportedPhotoLocallyForPreviewAndSending() = runTest {
        stubInitialLoads()
        val photoUri: Uri = mock()
        val tempFile = File("/tmp/imported-preview.jpg")
        whenever(photoStorage.stageImportedPhoto(photoUri)).thenReturn(tempFile)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.importPendingPhoto(photoUri)
        advanceUntilIdle()

        verify(photoStorage).stageImportedPhoto(photoUri)
        assertNull(viewModel.uiState.value.pendingPhotoUri)
        assertEquals(tempFile, viewModel.uiState.value.pendingCapturedPhotoFile)
    }

    @Test
    fun staleImportedPhotoResult_doesNotOverrideNewerPendingSelection_andDeletesStaleTempFile() = runTest {
        stubInitialLoads()
        val staleTempFile = File.createTempFile("stale_import_", ".jpg").apply { writeText("stale") }
        val freshTempFile = File.createTempFile("fresh_import_", ".jpg").apply { writeText("fresh") }
        val viewModel = createViewModel()
        advanceUntilIdle()

        val staleRequestId = viewModel.beginPendingPhotoImportForTest()
        val freshRequestId = viewModel.beginPendingPhotoImportForTest()

        viewModel.completePendingPhotoImportForTest(freshRequestId, Result.success(freshTempFile))
        viewModel.completePendingPhotoImportForTest(staleRequestId, Result.success(staleTempFile))

        assertEquals(freshTempFile, viewModel.uiState.value.pendingCapturedPhotoFile)
        assertEquals(PendingPhotoSource.Imported, viewModel.uiState.value.pendingPhotoSource)
        assertFalse(staleTempFile.exists())

        freshTempFile.delete()
    }

    @Test
    fun staleImportedPhotoFailure_doesNotClearNewerPendingSelection() = runTest {
        stubInitialLoads()
        val freshTempFile = File.createTempFile("fresh_import_", ".jpg").apply { writeText("fresh") }
        val viewModel = createViewModel()
        advanceUntilIdle()

        val staleRequestId = viewModel.beginPendingPhotoImportForTest()
        val freshRequestId = viewModel.beginPendingPhotoImportForTest()

        viewModel.completePendingPhotoImportForTest(freshRequestId, Result.success(freshTempFile))
        viewModel.completePendingPhotoImportForTest(
            staleRequestId,
            Result.failure(RuntimeException("stale import failed"))
        )

        assertEquals(freshTempFile, viewModel.uiState.value.pendingCapturedPhotoFile)
        assertEquals(PendingPhotoSource.Imported, viewModel.uiState.value.pendingPhotoSource)
        assertNull(viewModel.uiState.value.error)

        freshTempFile.delete()
    }

    @Test
    fun saveRecord_withStagedImportedPhoto_usesImportedPersistencePath() = runTest {
        stubInitialLoads()
        whenever(recordRepository.insertRecord(any())).thenReturn(1L)
        val tempFile = File("/tmp/imported-preview.jpg")
        whenever(photoStorage.persistStagedPhoto(tempFile)).thenReturn("/files/record_photos/p1.jpg")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingCapturedPhoto(tempFile, PendingPhotoSource.Imported)
        viewModel.saveRecord("记录一条新的觉察")
        advanceUntilIdle()

        verify(photoStorage).persistStagedPhoto(tempFile)
        verify(photoStorage, never()).persistCapturedPhoto(tempFile)
        verify(recordRepository).insertRecord(
            check { record ->
                assertEquals("/files/record_photos/p1.jpg", record.photoPath)
            }
        )
    }

    @Test
    fun saveRecord_withPendingPhotoAndBlankContent_stillSavesRecord() = runTest {
        stubInitialLoads()
        whenever(recordRepository.insertRecord(any())).thenReturn(1L)
        val photoUri: Uri = mock()
        whenever(photoStorage.persistImportedPhoto(photoUri)).thenReturn("/files/record_photos/p2.jpg")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingPhoto(photoUri)
        viewModel.saveRecord("   ")
        advanceUntilIdle()

        verify(recordRepository).insertRecord(
            check { record ->
                assertEquals("", record.content)
                assertEquals("/files/record_photos/p2.jpg", record.photoPath)
            }
        )
        assertNull(viewModel.uiState.value.pendingPhotoUri)
    }

    @Test
    fun saveRecord_withPendingCapturedPhoto_usesFallbackCapablePersistencePathAndClearsPendingState() = runTest {
        stubInitialLoads()
        whenever(recordRepository.insertRecord(any())).thenReturn(1L)
        val tempFile = File("/tmp/capture.jpg")
        whenever(photoStorage.persistStagedPhoto(tempFile)).thenReturn("/files/record_photos/captured.jpg")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingCapturedPhoto(tempFile)
        viewModel.saveRecord("记录一条拍照觉察")
        advanceUntilIdle()

        verify(photoStorage).persistStagedPhoto(tempFile)
        verify(photoStorage, never()).persistCapturedPhoto(tempFile)
        verify(recordRepository).insertRecord(
            check { record ->
                assertEquals("/files/record_photos/captured.jpg", record.photoPath)
            }
        )
        assertNull(viewModel.uiState.value.pendingPhotoUri)
        assertNull(viewModel.uiState.value.pendingCapturedPhotoFile)
    }

    @Test
    fun saveRecord_whenInsertFailsAfterImportedPhotoPersistence_deletesNewPhotoAndKeepsPendingPhoto() = runTest {
        stubInitialLoads()
        val photoUri: Uri = mock()
        whenever(photoStorage.persistImportedPhoto(photoUri)).thenReturn("/files/record_photos/p1.jpg")
        whenever(recordRepository.insertRecord(any())).thenThrow(RuntimeException("db write failed"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingPhoto(photoUri)
        viewModel.saveRecord("记录一条新的觉察")
        advanceUntilIdle()

        verify(photoStorage).deletePhoto("/files/record_photos/p1.jpg")
        assertEquals("保存记录失败: db write failed", viewModel.uiState.value.error)
        assertEquals(photoUri, viewModel.uiState.value.pendingPhotoUri)
    }

    @Test
    fun replaceRecordPhoto_whenUpdateFailsAfterPersistence_deletesNewPhotoAndKeepsOldPhoto() = runTest {
        stubInitialLoads()
        val record = Record(
            id = 9L,
            date = "2026-04-15",
            time = "10:00",
            content = "午前记录",
            photoPath = "/files/record_photos/p1.jpg"
        )
        val newUri: Uri = mock()
        whenever(photoStorage.persistImportedPhoto(newUri)).thenReturn("/files/record_photos/p2.jpg")
        whenever(recordRepository.updateRecord(any())).thenThrow(RuntimeException("db write failed"))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.replaceRecordPhoto(record, newUri)
        advanceUntilIdle()

        verify(photoStorage).deletePhoto("/files/record_photos/p2.jpg")
        verify(photoStorage, never()).deletePhoto("/files/record_photos/p1.jpg")
        assertEquals("更新记录照片失败: db write failed", viewModel.uiState.value.error)
    }

    @Test
    fun deleteRecord_withAttachedPhoto_deletesPersistedPhoto() = runTest {
        stubInitialLoads()
        whenever(recordRepository.deleteRecordAndReturn(7L)).thenReturn(
            Record(
                id = 7L,
                date = "2026-04-15",
                time = "09:20",
                content = "晨间散步",
                photoPath = "/files/record_photos/p1.jpg"
            )
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteRecord(7L)
        advanceUntilIdle()

        verify(recordRepository).deleteRecordAndReturn(7L)
        verify(photoStorage).deletePhoto("/files/record_photos/p1.jpg")
    }

    @Test
    fun replaceRecordPhoto_persistsNewPhoto_updatesRecord_andDeletesOldPhoto() = runTest {
        stubInitialLoads()
        val record = Record(
            id = 9L,
            date = "2026-04-15",
            time = "10:00",
            content = "午前记录",
            photoPath = "/files/record_photos/p1.jpg"
        )
        val newUri: Uri = mock()
        whenever(photoStorage.persistImportedPhoto(newUri)).thenReturn("/files/record_photos/p2.jpg")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.replaceRecordPhoto(record, newUri)
        advanceUntilIdle()

        verify(photoStorage).persistImportedPhoto(newUri)
        verify(recordRepository).updateRecord(
            check { updated ->
                assertEquals("/files/record_photos/p2.jpg", updated.photoPath)
                assertEquals(record.id, updated.id)
            }
        )
        verify(photoStorage).deletePhoto("/files/record_photos/p1.jpg")
    }

    @Test
    fun setPendingCapturedPhoto_clearsPendingImportedPhoto() = runTest {
        stubInitialLoads()
        val photoUri: Uri = mock()
        val tempFile = File("/tmp/capture.jpg")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingPhoto(photoUri)
        viewModel.setPendingCapturedPhoto(tempFile)

        assertNull(viewModel.uiState.value.pendingPhotoUri)
        assertEquals(tempFile, viewModel.uiState.value.pendingCapturedPhotoFile)
    }

    @Test
    fun setPendingCapturedPhoto_setsCapturedPendingState() = runTest {
        stubInitialLoads()
        val tempFile = File("/tmp/capture.jpg")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingCapturedPhoto(tempFile)

        val state = viewModel.uiState.value
        assertNull(state.pendingPhotoUri)
        assertEquals(tempFile, state.pendingCapturedPhotoFile)
        assertEquals(PendingPhotoSource.Captured, state.pendingPhotoSource)
    }

    @Test
    fun setPendingCapturedPhoto_replacesOldTempFile() = runTest {
        stubInitialLoads()
        val first = File.createTempFile("capture_old_", ".jpg").apply { writeText("old") }
        val second = File.createTempFile("capture_new_", ".jpg").apply { writeText("new") }
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingCapturedPhoto(first)
        viewModel.setPendingCapturedPhoto(second)

        assertEquals(second, viewModel.uiState.value.pendingCapturedPhotoFile)
        assertFalse(first.exists())

        second.delete()
    }

    @Test
    fun setPendingPhoto_deletesExistingPendingCapturedPhotoFile() = runTest {
        stubInitialLoads()
        val existingTempFile: File = mock()
        whenever(existingTempFile.exists()).thenReturn(true)
        val photoUri: Uri = mock()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingCapturedPhoto(existingTempFile)
        viewModel.setPendingPhoto(photoUri)

        verify(existingTempFile).delete()
        assertEquals(photoUri, viewModel.uiState.value.pendingPhotoUri)
        assertNull(viewModel.uiState.value.pendingCapturedPhotoFile)
    }

    @Test
    fun setPendingCapturedPhoto_withNull_deletesExistingPendingCapturedPhotoFile() = runTest {
        stubInitialLoads()
        val existingTempFile: File = mock()
        whenever(existingTempFile.exists()).thenReturn(true)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingCapturedPhoto(existingTempFile)
        viewModel.setPendingCapturedPhoto(null)

        verify(existingTempFile).delete()
        assertNull(viewModel.uiState.value.pendingPhotoUri)
        assertNull(viewModel.uiState.value.pendingCapturedPhotoFile)
    }

    @Test
    fun onCleared_deletesPendingCapturedPhotoFile() = runTest {
        stubInitialLoads()
        val pendingTempFile = File.createTempFile("pending_capture_", ".jpg").apply { writeText("capture") }
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setPendingCapturedPhoto(pendingTempFile)

        viewModel.javaClass.getDeclaredMethod("onCleared").apply {
            isAccessible = true
            invoke(viewModel)
        }

        assertFalse(pendingTempFile.exists())
        assertNull(viewModel.uiState.value.pendingCapturedPhotoFile)
        assertTrue(viewModel.uiState.value.pendingPhotoUri == null)
    }

    @Test
    fun generateDiary_apiFailure_surfacesStatusAndBodyMessage() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(
            flowOf(
                UserSettings(
                    apiEndpoint = "https://dashscope.aliyuncs.com/compatible-mode/v1",
                    apiKey = "secret",
                    modelName = "qwen-plus",
                    nickname = "林间"
                )
            )
        )
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(true))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(
            listOf(Record(id = 1, date = "2026-04-13", time = "18:30", content = "工作结束后的疲惫感。"))
        )
        whenever(recordRepository.getDatesWithRecords()).thenReturn(listOf("2026-04-13"))
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(1)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(diaryRepository.hasDiaryForDate(any())).thenReturn(true)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = false)
        )
        whenever(apiServiceFactory.create("https://dashscope.aliyuncs.com/compatible-mode/v1"))
            .thenReturn(apiService)
        whenever(
            apiService.generateCompletion(
                authorization = eq("Bearer secret"),
                request = any<OpenAIRequest>()
            )
        ).thenReturn(
            Response.error(
                404,
                "{\"message\":\"Invalid URL\"}".toResponseBody()
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.generateDiary()
        advanceUntilIdle()

        assertEquals("API 调用失败(404): Invalid URL", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isGeneratingDiary)
        verify(diaryRepository, never()).saveDiary(any())
    }

    @Test
    fun generateDiary_success_marksHomeReviewNavigationHint() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(
            flowOf(
                UserSettings(
                    apiEndpoint = "https://api.example.com/",
                    apiKey = "secret",
                    modelName = "qwen-plus",
                    nickname = "林间"
                )
            )
        )
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(true))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(
            listOf(Record(id = 1, date = "2026-04-13", time = "18:30", content = "nihao"))
        )
        whenever(recordRepository.getDatesWithRecords()).thenReturn(listOf("2026-04-13"))
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(1)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(diaryRepository.hasDiaryForDate(any())).thenReturn(false)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = false)
        )
        whenever(apiServiceFactory.create("https://api.example.com/")).thenReturn(apiService)
        whenever(
            apiService.generateCompletion(
                authorization = eq("Bearer secret"),
                request = any<OpenAIRequest>()
            )
        ).thenReturn(
            Response.success(
                OpenAIResponse(
                    id = "resp-2",
                    choices = listOf(
                        OpenAIResponse.Choice(
                            index = 0,
                            message = OpenAIResponse.Choice.Message(
                                role = "assistant",
                                content = "【日记】\n2026年4月13日 周一 天气未知\nnihao\n\n【启发】\n先继续记录。"
                            ),
                            finishReason = "stop"
                        )
                    ),
                    usage = null
                )
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.generateDiary()
        advanceUntilIdle()

        assertEquals(viewModel.uiState.value.currentDate, viewModel.uiState.value.generatedDiaryReadyDate)
    }

    @Test
    fun generateDiary_success_keepsGeneratedDiaryReadyDateForInlineCardHint() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(
            flowOf(
                UserSettings(
                    apiEndpoint = "https://api.example.com/",
                    apiKey = "secret",
                    modelName = "qwen-plus",
                    nickname = "林间"
                )
            )
        )
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(true))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(
            listOf(Record(id = 1, date = "2026-04-13", time = "18:30", content = "nihao"))
        )
        whenever(recordRepository.getDatesWithRecords()).thenReturn(listOf("2026-04-13"))
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(1)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(diaryRepository.hasDiaryForDate(any())).thenReturn(false)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = false)
        )
        whenever(apiServiceFactory.create("https://api.example.com/")).thenReturn(apiService)
        whenever(
            apiService.generateCompletion(
                authorization = eq("Bearer secret"),
                request = any<OpenAIRequest>()
            )
        ).thenReturn(
            Response.success(
                OpenAIResponse(
                    id = "resp-4",
                    choices = listOf(
                        OpenAIResponse.Choice(
                            index = 0,
                            message = OpenAIResponse.Choice.Message(
                                role = "assistant",
                                content = "【日记】\n2026年4月13日 周一 天气未知\nnihao\n\n【启发】\n先继续记录。"
                            ),
                            finishReason = "stop"
                        )
                    ),
                    usage = null
                )
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.generateDiary()
        advanceUntilIdle()

        assertEquals(viewModel.uiState.value.currentDate, viewModel.uiState.value.generatedDiaryReadyDate)
    }

    @Test
    fun generateDiary_usesLowTemperatureRequestToReduceFabrication() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(
            flowOf(
                UserSettings(
                    apiEndpoint = "https://api.example.com/",
                    apiKey = "secret",
                    modelName = "qwen-plus",
                    nickname = "林间"
                )
            )
        )
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(true))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(
            listOf(Record(id = 1, date = "2026-04-13", time = "18:30", content = "nihao"))
        )
        whenever(recordRepository.getDatesWithRecords()).thenReturn(listOf("2026-04-13"))
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(1)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(diaryRepository.hasDiaryForDate(any())).thenReturn(false)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = false)
        )
        whenever(apiServiceFactory.create("https://api.example.com/")).thenReturn(apiService)
        whenever(
            apiService.generateCompletion(
                authorization = eq("Bearer secret"),
                request = any<OpenAIRequest>()
            )
        ).thenReturn(
            Response.success(
                OpenAIResponse(
                    id = "resp-3",
                    choices = listOf(
                        OpenAIResponse.Choice(
                            index = 0,
                            message = OpenAIResponse.Choice.Message(
                                role = "assistant",
                                content = "【日记】\n2026年4月13日 周一 天气未知\nnihao\n\n【启发】\n先继续记录。"
                            ),
                            finishReason = "stop"
                        )
                    ),
                    usage = null
                )
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.generateDiary()
        advanceUntilIdle()

        val requestCaptor = argumentCaptor<OpenAIRequest>()
        verify(apiService).generateCompletion(
            authorization = eq("Bearer secret"),
            request = requestCaptor.capture()
        )
        assertEquals(0.2, requestCaptor.firstValue.temperature, 0.0)
    }

    @Test
    fun startupCatchUp_success_setsSessionHintWithGeneratedCount() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(
            flowOf(
                UserSettings(
                    apiEndpoint = "https://api.example.com/",
                    apiKey = "secret",
                    modelName = "qwen-plus"
                )
            )
        )
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(true))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(emptyList())
        whenever(recordRepository.getDatesWithRecords()).thenReturn(emptyList())
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(0)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 3, failed = false)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("已补生成 3 篇 AI 日记，可前往回顾查看", viewModel.uiState.value.autoDiaryGenerationHint)
    }

    @Test
    fun startupCatchUp_withoutGeneratedOrFailedResult_keepsSnackbarHintEmpty() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(flowOf(UserSettings()))
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(false))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(emptyList())
        whenever(recordRepository.getDatesWithRecords()).thenReturn(emptyList())
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(0)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = false)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.autoDiaryGenerationHint)
    }

    @Test
    fun startupCatchUp_generatesMostRecentFiveMissingDiaries() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(flowOf(UserSettings()))
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(true))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(emptyList())
        whenever(recordRepository.getDatesWithRecords()).thenReturn(emptyList())
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(0)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 5, failed = false)
        )

        createViewModel()
        advanceUntilIdle()

        verify(autoGenerationCoordinator).generateRecentMissingDiaries()
    }

    @Test
    fun startupCatchUp_whenAutoGenerationFails_marksHomeHintWithoutSnackbarError() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(
            flowOf(
                UserSettings(
                    apiEndpoint = "https://api.example.com/",
                    apiKey = "secret",
                    modelName = "qwen-plus",
                    nickname = "林间"
                )
            )
        )
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(true))
        whenever(recordRepository.getRecordsByDate(any())).thenAnswer { invocation ->
            val date = invocation.getArgument<String>(0)
            listOf(Record(id = 1, date = date, time = "09:00", content = "记录 $date"))
        }
        whenever(recordRepository.getDatesWithRecords()).thenReturn(listOf("2026-04-16"))
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(1)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = true)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(
            "AI 日记生成失败，请检查账号、网络状态、连接配置等",
            viewModel.uiState.value.autoDiaryGenerationHint
        )
        assertNull(viewModel.uiState.value.error)
        verify(diaryRepository, never()).saveDiary(any())
    }

    @Test
    fun manualGenerate_success_clearsPreviousAutoGenerationFailureHint() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(
            flowOf(
                UserSettings(
                    apiEndpoint = "https://api.example.com/",
                    apiKey = "secret",
                    modelName = "qwen-plus",
                    nickname = "林间"
                )
            )
        )
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(true))
        whenever(recordRepository.getRecordsByDate(any())).thenAnswer { invocation ->
            val date = invocation.getArgument<String>(0)
            listOf(Record(id = 1, date = date, time = "09:00", content = "记录 $date"))
        }
        whenever(recordRepository.getDatesWithRecords()).thenReturn(listOf("2026-04-16"))
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(1)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = true)
        )
        whenever(apiServiceFactory.create("https://api.example.com/")).thenReturn(apiService)
        whenever(
            apiService.generateCompletion(
                authorization = eq("Bearer secret"),
                request = any<OpenAIRequest>()
            )
        ).thenReturn(
            Response.success(
                OpenAIResponse(
                    id = "resp-3",
                    choices = listOf(
                        OpenAIResponse.Choice(
                            index = 0,
                            message = OpenAIResponse.Choice.Message(
                                role = "assistant",
                                content = "【日记】\n2026年4月16日 周四 天气未知\n记录 2026-04-16\n\n【启发】\n继续记录。"
                            ),
                            finishReason = "stop"
                        )
                    ),
                    usage = null
                )
            )
        )

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(
            "AI 日记生成失败，请检查账号、网络状态、连接配置等",
            viewModel.uiState.value.autoDiaryGenerationHint
        )

        viewModel.generateDiary()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.autoDiaryGenerationHint)
    }

    private fun createViewModel(): MainViewModel =
        MainViewModel(
            recordRepository = recordRepository,
            diaryRepository = diaryRepository,
            settingsRepository = settingsRepository,
            apiServiceFactory = apiServiceFactory,
            photoStorage = photoStorage,
            autoGenerationCoordinator = autoGenerationCoordinator,
            aiDiaryGenerationScheduler = aiDiaryGenerationScheduler
        )

    private suspend fun stubInitialLoads() {
        whenever(settingsRepository.userSettings).thenReturn(flowOf(UserSettings()))
        whenever(settingsRepository.hasApiKey).thenReturn(flowOf(false))
        whenever(recordRepository.getRecordsByDate(any())).thenReturn(emptyList())
        whenever(recordRepository.getDatesWithRecords()).thenReturn(emptyList())
        whenever(recordRepository.getRecordCountByDate(any())).thenReturn(0)
        whenever(diaryRepository.getDiaryByDate(any())).thenReturn(null)
        whenever(diaryRepository.hasDiaryForDate(any())).thenReturn(false)
        whenever(autoGenerationCoordinator.generateRecentMissingDiaries()).thenReturn(
            CatchUpGenerationOutcome(generatedCount = 0, failed = false)
        )
        whenever(autoGenerationCoordinator.generateTodayIfNeeded()).thenReturn(
            AutoGenerationOutcome(generated = false, failed = false)
        )
    }
}
