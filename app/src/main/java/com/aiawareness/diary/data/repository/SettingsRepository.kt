package com.aiawareness.diary.data.repository

import com.aiawareness.diary.data.local.UserPreferences
import com.aiawareness.diary.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val userPreferences: UserPreferences
) {

    val userSettings: Flow<UserSettings> = userPreferences.userSettings
    val hasApiKey: Flow<Boolean> = userPreferences.hasApiKey()

    suspend fun updateNickname(nickname: String) {
        userPreferences.updateNickname(nickname)
    }

    suspend fun updateAvatarPath(path: String) {
        userPreferences.updateAvatarPath(path)
    }

    suspend fun updateProfileQuote(value: String) {
        userPreferences.updateProfileQuote(value)
    }

    suspend fun updateApiConfig(endpoint: String, apiKey: String, modelName: String) {
        userPreferences.updateApiConfig(endpoint, apiKey, modelName)
    }

    suspend fun updateDiaryGenerationTime(hour: Int, minute: Int) {
        userPreferences.updateDiaryGenerationTime(hour, minute)
    }

    suspend fun updateS3Config(
        endpoint: String,
        bucket: String,
        accessKey: String,
        secretKey: String
    ) {
        userPreferences.updateS3Config(endpoint, bucket, accessKey, secretKey)
    }

    suspend fun updateS3AutoSync(enabled: Boolean) {
        userPreferences.updateS3AutoSync(enabled)
    }

    suspend fun replaceAll(settings: UserSettings) {
        userPreferences.replaceAll(settings)
    }
}
