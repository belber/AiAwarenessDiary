package com.aiawareness.diary.ui.screens

import android.net.Uri
import com.aiawareness.diary.MainDispatcherRule
import com.aiawareness.diary.data.backup.ImportConflictStrategy
import com.aiawareness.diary.data.backup.ImportPreview
import com.aiawareness.diary.data.backup.LocalBackupService
import com.aiawareness.diary.data.local.AvatarStorage
import com.aiawareness.diary.data.model.UserSettings
import com.aiawareness.diary.data.remote.OpenAIRequest
import com.aiawareness.diary.data.remote.OpenAIResponse
import com.aiawareness.diary.data.remote.OpenAIApiService
import com.aiawareness.diary.data.remote.OpenAIApiServiceFactory
import com.aiawareness.diary.data.remote.S3ConnectionTester
import com.aiawareness.diary.data.repository.DiaryRepository
import com.aiawareness.diary.data.repository.RecordRepository
import com.aiawareness.diary.data.repository.SettingsRepository
import com.aiawareness.diary.domain.AiDiaryGenerationScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.mockito.kotlin.never
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository: SettingsRepository = mock()
    private val recordRepository: RecordRepository = mock()
    private val diaryRepository: DiaryRepository = mock()
    private val apiServiceFactory: OpenAIApiServiceFactory = mock()
    private val apiService: OpenAIApiService = mock()
    private val s3ConnectionTester: S3ConnectionTester = mock()
    private val localBackupService: LocalBackupService = mock()
    private val avatarStorage: AvatarStorage = mock()
    private val aiDiaryGenerationScheduler: AiDiaryGenerationScheduler = mock()

    @Test
    fun saveApiConfig_persistsEndpointAndKeyAndPublishesMessage() = runTest {
        stubBase()
        val viewModel = buildViewModel()

        viewModel.saveApiConfig("https://api.example.com/", "secret", "gpt-4o-mini")
        advanceUntilIdle()

        verify(settingsRepository).updateApiConfig("https://api.example.com/", "secret", "gpt-4o-mini")
        assertEquals("AI 配置已保存", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun saveAiConfigAndGenerationTime_requiresSuccessfulConnectionTestFirst() = runTest {
        stubBase()
        val viewModel = buildViewModel()

        viewModel.saveAiConfigAndGenerationTime(
            endpoint = "https://api.example.com/",
            apiKey = "secret",
            modelName = "gpt-4.1-mini",
            hour = 22,
            minute = 0
        )
        advanceUntilIdle()

        verify(settingsRepository, never()).updateApiConfig(any(), any(), any())
        assertEquals("请先测试连接，确认 AI 配置可用后再保存", viewModel.uiState.value.message)
    }

    @Test
    fun saveAiConfigAndGenerationTime_persistsOnlyAfterMatchingSuccessfulTest() = runTest {
        stubBase()
        whenever(apiServiceFactory.create("https://api.example.com/")).thenReturn(apiService)
        whenever(
            apiService.generateCompletion(
                authorization = eq("Bearer secret"),
                request = any<OpenAIRequest>()
            )
        ).thenReturn(
            Response.success(
                OpenAIResponse(
                    id = "test",
                    choices = listOf(
                        OpenAIResponse.Choice(
                            index = 0,
                            message = OpenAIResponse.Choice.Message("assistant", "ok"),
                            finishReason = "stop"
                        )
                    ),
                    usage = null
                )
            )
        )
        val viewModel = buildViewModel()

        viewModel.testAiConnection(
            endpoint = "https://api.example.com/",
            apiKey = "secret",
            modelName = "gpt-4.1-mini"
        )
        advanceUntilIdle()
        viewModel.saveAiConfigAndGenerationTime(
            endpoint = "https://api.example.com/",
            apiKey = "secret",
            modelName = "gpt-4.1-mini",
            hour = 22,
            minute = 0
        )
        advanceUntilIdle()

        verify(settingsRepository).updateApiConfig("https://api.example.com/", "secret", "gpt-4.1-mini")
        verify(settingsRepository).updateDiaryGenerationTime(22, 0)
        verify(aiDiaryGenerationScheduler).scheduleDaily(22, 0)
        assertEquals("AI 配置已保存", viewModel.uiState.value.message)
    }

    @Test
    fun saveDiaryGenerationTime_reschedulesDailyWorker() = runTest {
        stubBase()
        val viewModel = buildViewModel()

        viewModel.saveDiaryGenerationTime(hour = 7, minute = 45)
        advanceUntilIdle()

        verify(settingsRepository).updateDiaryGenerationTime(7, 45)
        verify(aiDiaryGenerationScheduler).scheduleDaily(7, 45)
        assertEquals("AI日记生成时间已保存", viewModel.uiState.value.message)
    }

    @Test
    fun savePersonalInfo_importsAvatarAndUpdatesPreviewState() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(
            flowOf(UserSettings(nickname = "用户", avatarPath = "/avatars/current.jpg"))
        )
        whenever(recordRepository.getDatesWithRecords()).thenReturn(emptyList())
        val avatarUri: Uri = mock()
        whenever(avatarStorage.importAvatar(avatarUri)).thenReturn("/avatars/imported.jpg")
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val initialVersion = viewModel.uiState.value.avatarRefreshVersion

        viewModel.savePersonalInfo(
            nickname = "用户",
            avatarUri = avatarUri
        )
        advanceUntilIdle()

        verify(settingsRepository).updateAvatarPath("/avatars/imported.jpg")
        assertEquals("/avatars/imported.jpg", viewModel.uiState.value.settings.avatarPath)
        assertEquals(initialVersion + 1, viewModel.uiState.value.avatarRefreshVersion)
        assertEquals("个人信息已保存", viewModel.uiState.value.message)
    }

    @Test
    fun saveS3Config_persistsAllFieldsAndAutoSync() = runTest {
        stubBase()
        val viewModel = buildViewModel()

        viewModel.saveS3Config("https://s3.example.com", "diary", "ak", "sk", true)
        advanceUntilIdle()

        verify(settingsRepository).updateS3Config("https://s3.example.com", "diary", "ak", "sk")
        verify(settingsRepository).updateS3AutoSync(true)
        assertEquals("数据管理配置已保存", viewModel.uiState.value.message)
    }

    @Test
    fun saveS3Config_disablesAutoSyncWhenConfigIncomplete() = runTest {
        stubBase()
        val viewModel = buildViewModel()

        viewModel.saveS3Config(
            endpoint = "",
            bucket = "diary",
            accessKey = "ak",
            secretKey = "sk",
            autoSync = true
        )
        advanceUntilIdle()

        verify(settingsRepository).updateS3AutoSync(false)
    }

    @Test
    fun previewImport_publishesPreviewState() = runTest {
        stubBase()
        val source: Uri = mock()
        whenever(localBackupService.previewImport(source)).thenReturn(
            ImportPreview(1, 2, 3, 4, true)
        )
        val viewModel = buildViewModel()

        viewModel.previewImport(source)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.importPreview?.recordAdds)
        assertEquals(source, viewModel.uiState.value.pendingImportUri)
        assertFalse(viewModel.uiState.value.isImportingBackup)
    }

    @Test
    fun confirmImport_mergeUsesBackupServiceAndClearsPreview() = runTest {
        stubBase()
        val source: Uri = mock()
        whenever(localBackupService.previewImport(source)).thenReturn(ImportPreview(1, 0, 1, 0, false))
        whenever(localBackupService.importFromZip(source, ImportConflictStrategy.Merge))
            .thenReturn("导入完成，已合并不冲突的数据")
        val viewModel = buildViewModel()

        viewModel.previewImport(source)
        advanceUntilIdle()
        viewModel.confirmImport(ImportConflictStrategy.Merge)
        advanceUntilIdle()

        verify(localBackupService).importFromZip(source, ImportConflictStrategy.Merge)
        assertEquals("导入完成，已合并不冲突的数据", viewModel.uiState.value.message)
        assertEquals(null, viewModel.uiState.value.importPreview)
        assertEquals(null, viewModel.uiState.value.pendingImportUri)
    }

    @Test
    fun testAiConnection_successPublishesMessage() = runTest {
        stubBase()
        whenever(apiServiceFactory.create("https://dashscope.aliyuncs.com/compatible-mode/v1")).thenReturn(apiService)
        whenever(
            apiService.generateCompletion(
                authorization = eq("Bearer secret"),
                request = any<OpenAIRequest>()
            )
        ).thenReturn(
            Response.success(
                OpenAIResponse(
                    id = "test",
                    choices = listOf(
                        OpenAIResponse.Choice(
                            index = 0,
                            message = OpenAIResponse.Choice.Message("assistant", "ok"),
                            finishReason = "stop"
                        )
                    ),
                    usage = null
                )
            )
        )
        val viewModel = buildViewModel()

        viewModel.testAiConnection(
            endpoint = "https://dashscope.aliyuncs.com/compatible-mode/v1",
            apiKey = "secret",
            modelName = "qwen-plus"
        )
        advanceUntilIdle()

        assertEquals("AI 连接测试成功", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isTestingConnection)
    }

    @Test
    fun testAiConnection_failurePublishesDetailedMessage() = runTest {
        stubBase()
        whenever(apiServiceFactory.create("https://dashscope.aliyuncs.com/compatible-mode/v1")).thenReturn(apiService)
        whenever(
            apiService.generateCompletion(
                authorization = eq("Bearer secret"),
                request = any<OpenAIRequest>()
            )
        ).thenReturn(Response.error(401, "{\"message\":\"Invalid API key\"}".toResponseBody()))
        val viewModel = buildViewModel()

        viewModel.testAiConnection(
            endpoint = "https://dashscope.aliyuncs.com/compatible-mode/v1",
            apiKey = "secret",
            modelName = "qwen-plus"
        )
        advanceUntilIdle()

        assertEquals("连接测试失败(401): Invalid API key", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isTestingConnection)
    }

    @Test
    fun init_loadsOverviewStats() = runTest {
        whenever(settingsRepository.userSettings).thenReturn(flowOf(UserSettings()))
        whenever(recordRepository.getDatesWithRecords()).thenReturn(listOf("2026-04-11", "2026-04-13"))
        whenever(recordRepository.getRecordCountByDate("2026-04-11")).thenReturn(2)
        whenever(recordRepository.getRecordCountByDate("2026-04-13")).thenReturn(3)

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.stats.totalDays)
        assertEquals(5, viewModel.uiState.value.stats.totalRecords)
        assertEquals("2026-04-11", viewModel.uiState.value.stats.firstRecordDate)
        assertEquals("2026-04-13", viewModel.uiState.value.stats.latestRecordDate)
    }

    private fun buildViewModel(): SettingsViewModel =
        SettingsViewModel(
            settingsRepository = settingsRepository,
            recordRepository = recordRepository,
            diaryRepository = diaryRepository,
            apiServiceFactory = apiServiceFactory,
            s3ConnectionTester = s3ConnectionTester,
            localBackupService = localBackupService,
            avatarStorage = avatarStorage,
            aiDiaryGenerationScheduler = aiDiaryGenerationScheduler
        )

    private suspend fun stubBase() {
        whenever(settingsRepository.userSettings).thenReturn(flowOf(UserSettings()))
        whenever(recordRepository.getDatesWithRecords()).thenReturn(emptyList())
    }
}
