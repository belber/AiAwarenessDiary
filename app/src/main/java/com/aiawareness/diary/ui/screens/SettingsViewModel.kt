package com.aiawareness.diary.ui.screens

import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.aiawareness.diary.data.backup.ImportConflictStrategy
import com.aiawareness.diary.data.backup.ImportPreview
import com.aiawareness.diary.data.backup.LocalBackupService
import com.aiawareness.diary.data.local.AvatarStorage
import com.aiawareness.diary.data.repository.RecordRepository
import com.aiawareness.diary.data.remote.OpenAIRequest
import com.aiawareness.diary.data.remote.OpenAIApiServiceFactory
import com.aiawareness.diary.data.remote.S3ConnectionTester
import com.aiawareness.diary.data.remote.buildConnectionFailureMessage
import com.aiawareness.diary.data.remote.buildExceptionMessage
import com.aiawareness.diary.data.remote.safeLogError
import com.aiawareness.diary.data.model.UserSettings
import com.aiawareness.diary.data.repository.DiaryRepository
import com.aiawareness.diary.data.repository.SettingsRepository
import com.aiawareness.diary.domain.AiDiaryGenerationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val avatarRefreshVersion: Int = 0,
    val stats: SettingsOverviewStats = SettingsOverviewStats(),
    val isSaving: Boolean = false,
    val isExportingBackup: Boolean = false,
    val isImportingBackup: Boolean = false,
    val isTestingConnection: Boolean = false,
    val isTestingS3Connection: Boolean = false,
    val testedAiConfigFingerprint: String? = null,
    val pendingImportUri: Uri? = null,
    val importPreview: ImportPreview? = null,
    val message: String? = null
)

data class SettingsOverviewStats(
    val totalDays: Int = 0,
    val totalRecords: Int = 0,
    val firstRecordDate: String? = null,
    val latestRecordDate: String? = null
)

fun hasCompleteS3Config(
    endpoint: String,
    bucket: String,
    accessKey: String,
    secretKey: String
): Boolean =
    endpoint.isNotBlank() &&
        bucket.isNotBlank() &&
        accessKey.isNotBlank() &&
        secretKey.isNotBlank()

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val recordRepository: RecordRepository,
    private val diaryRepository: DiaryRepository,
    private val apiServiceFactory: OpenAIApiServiceFactory,
    private val s3ConnectionTester: S3ConnectionTester,
    private val localBackupService: LocalBackupService,
    private val avatarStorage: AvatarStorage,
    private val aiDiaryGenerationScheduler: AiDiaryGenerationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.userSettings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
        loadOverviewStats()
    }

    private fun loadOverviewStats() {
        viewModelScope.launch {
            val dates = recordRepository.getDatesWithRecords().sorted()
            val totalRecords = dates.sumOf { date -> recordRepository.getRecordCountByDate(date) }
            _uiState.update {
                it.copy(
                    stats = SettingsOverviewStats(
                        totalDays = dates.size,
                        totalRecords = totalRecords,
                        firstRecordDate = dates.firstOrNull(),
                        latestRecordDate = dates.lastOrNull()
                    )
                )
            }
        }
    }

    fun exportBackup(destination: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExportingBackup = true, message = null) }
            try {
                val message = localBackupService.exportToZip(destination)
                _uiState.update { it.copy(isExportingBackup = false, message = message) }
            } catch (e: Exception) {
                safeLogError("SettingsViewModel", "exportBackup exception", e)
                _uiState.update {
                    it.copy(
                        isExportingBackup = false,
                        message = buildExceptionMessage("导出失败", e)
                    )
                }
            }
        }
    }

    fun previewImport(source: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImportingBackup = true, message = null) }
            try {
                val preview = localBackupService.previewImport(source)
                _uiState.update {
                    it.copy(
                        isImportingBackup = false,
                        pendingImportUri = source,
                        importPreview = preview
                    )
                }
            } catch (e: Exception) {
                safeLogError("SettingsViewModel", "previewImport exception", e)
                _uiState.update {
                    it.copy(
                        isImportingBackup = false,
                        message = buildExceptionMessage("导入预检失败", e)
                    )
                }
            }
        }
    }

    fun confirmImport(strategy: ImportConflictStrategy) {
        val source = uiState.value.pendingImportUri ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isImportingBackup = true, message = null) }
            try {
                val message = localBackupService.importFromZip(source, strategy)
                loadOverviewStats()
                _uiState.update {
                    it.copy(
                        isImportingBackup = false,
                        pendingImportUri = null,
                        importPreview = null,
                        message = message
                    )
                }
            } catch (e: Exception) {
                safeLogError("SettingsViewModel", "confirmImport exception", e)
                _uiState.update {
                    it.copy(
                        isImportingBackup = false,
                        message = buildExceptionMessage("导入失败", e)
                    )
                }
            }
        }
    }

    fun dismissImportPreview() {
        _uiState.update { it.copy(pendingImportUri = null, importPreview = null) }
    }

    fun currentSettings(): UserSettings = uiState.value.settings

    fun savePersonalInfo(nickname: String, avatarUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            val avatarPath = try {
                avatarUri?.let { avatarStorage.importAvatar(it) }
            } catch (e: Exception) {
                safeLogError("SettingsViewModel", "savePersonalInfo avatar import exception", e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = buildExceptionMessage("头像保存失败", e)
                    )
                }
                return@launch
            }
            _uiState.update { state ->
                state.copy(
                    settings = state.settings.copy(
                        nickname = nickname,
                        avatarPath = avatarPath ?: state.settings.avatarPath
                    ),
                    avatarRefreshVersion = if (avatarPath.isNullOrBlank()) {
                        state.avatarRefreshVersion
                    } else {
                        state.avatarRefreshVersion + 1
                    },
                    message = null
                )
            }
            settingsRepository.updateNickname(nickname)
            if (!avatarPath.isNullOrBlank()) {
                settingsRepository.updateAvatarPath(avatarPath)
            }
            _uiState.update { it.copy(isSaving = false, message = "个人信息已保存") }
        }
    }

    fun saveProfileQuote(value: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            settingsRepository.updateProfileQuote(value)
            _uiState.update { it.copy(isSaving = false, message = "状态语已保存") }
        }
    }

    fun saveApiConfig(endpoint: String, apiKey: String, modelName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            settingsRepository.updateApiConfig(endpoint, apiKey, modelName)
            _uiState.update { it.copy(isSaving = false, message = "AI 配置已保存") }
        }
    }

    fun saveAiConfigAndGenerationTime(
        endpoint: String,
        apiKey: String,
        modelName: String,
        hour: Int,
        minute: Int
    ) {
        viewModelScope.launch {
            if (uiState.value.testedAiConfigFingerprint != aiConfigFingerprint(endpoint, apiKey, modelName)) {
                _uiState.update { it.copy(message = "请先测试连接，确认 AI 配置可用后再保存") }
                return@launch
            }
            _uiState.update { it.copy(isSaving = true, message = null) }
            settingsRepository.updateApiConfig(endpoint, apiKey, modelName)
            settingsRepository.updateDiaryGenerationTime(hour, minute)
            aiDiaryGenerationScheduler.scheduleDaily(hour, minute)
            _uiState.update { it.copy(isSaving = false, message = "AI 配置已保存") }
        }
    }

    fun testAiConnection(endpoint: String, apiKey: String, modelName: String) {
        viewModelScope.launch {
            if (endpoint.isBlank() || apiKey.isBlank() || modelName.isBlank()) {
                _uiState.update { it.copy(message = "请先填写完整的 AI 配置") }
                return@launch
            }

            _uiState.update { it.copy(isTestingConnection = true, message = null) }
            try {
                val apiService = apiServiceFactory.create(endpoint)
                val response = apiService.generateCompletion(
                    authorization = "Bearer $apiKey",
                    request = OpenAIRequest(
                        model = modelName,
                        messages = listOf(
                            OpenAIRequest.Message("system", "You are a connectivity test assistant."),
                            OpenAIRequest.Message("user", "Reply with ok.")
                        ),
                        temperature = 0.0,
                        maxTokens = 8
                    )
                )

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isTestingConnection = false,
                            testedAiConfigFingerprint = aiConfigFingerprint(endpoint, apiKey, modelName),
                            message = "AI 连接测试成功"
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    safeLogError("SettingsViewModel", "testAiConnection failed: code=${response.code()} body=$errorBody")
                    _uiState.update {
                        it.copy(
                            isTestingConnection = false,
                            testedAiConfigFingerprint = null,
                            message = buildConnectionFailureMessage(response.code(), errorBody)
                        )
                    }
                }
            } catch (e: Exception) {
                safeLogError("SettingsViewModel", "testAiConnection exception", e)
                _uiState.update {
                    it.copy(
                        isTestingConnection = false,
                        testedAiConfigFingerprint = null,
                        message = buildExceptionMessage("连接测试失败", e)
                    )
                }
            }
        }
    }

    fun saveS3Config(
        endpoint: String,
        bucket: String,
        accessKey: String,
        secretKey: String,
        autoSync: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            val autoSyncEnabled = autoSync && hasCompleteS3Config(endpoint, bucket, accessKey, secretKey)
            settingsRepository.updateS3Config(endpoint, bucket, accessKey, secretKey)
            settingsRepository.updateS3AutoSync(autoSyncEnabled)
            _uiState.update { it.copy(isSaving = false, message = "数据管理配置已保存") }
        }
    }

    fun testS3Connection(
        endpoint: String,
        bucket: String,
        accessKey: String,
        secretKey: String
    ) {
        viewModelScope.launch {
            if (endpoint.isBlank() || bucket.isBlank() || accessKey.isBlank() || secretKey.isBlank()) {
                _uiState.update { it.copy(message = "请先填写完整的 S3 配置") }
                return@launch
            }

            _uiState.update { it.copy(isTestingS3Connection = true, message = null) }
            try {
                val message = s3ConnectionTester.testConnection(endpoint, bucket, accessKey, secretKey)
                _uiState.update { it.copy(isTestingS3Connection = false, message = message) }
            } catch (e: Exception) {
                safeLogError("SettingsViewModel", "testS3Connection exception", e)
                _uiState.update {
                    it.copy(
                        isTestingS3Connection = false,
                        message = buildExceptionMessage("S3 连接测试失败", e)
                    )
                }
            }
        }
    }

    fun saveDiaryGenerationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, message = null) }
            settingsRepository.updateDiaryGenerationTime(hour, minute)
            aiDiaryGenerationScheduler.scheduleDaily(hour, minute)
            _uiState.update { it.copy(isSaving = false, message = "AI日记生成时间已保存") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun aiConfigFingerprint(endpoint: String, apiKey: String, modelName: String): String =
        listOf(endpoint.trim(), apiKey.trim(), modelName.trim()).joinToString("::")
}
