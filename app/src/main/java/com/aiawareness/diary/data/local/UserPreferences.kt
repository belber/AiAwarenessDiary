package com.aiawareness.diary.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aiawareness.diary.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val NICKNAME = stringPreferencesKey("nickname")
        private val AVATAR_PATH = stringPreferencesKey("avatar_path")
        private val API_ENDPOINT = stringPreferencesKey("api_endpoint")
        private val API_KEY = stringPreferencesKey("api_key")
        private val DIARY_GEN_HOUR = intPreferencesKey("diary_gen_hour")
        private val DIARY_GEN_MINUTE = intPreferencesKey("diary_gen_minute")
        private val S3_ENDPOINT = stringPreferencesKey("s3_endpoint")
        private val S3_BUCKET = stringPreferencesKey("s3_bucket")
        private val S3_ACCESS_KEY = stringPreferencesKey("s3_access_key")
        private val S3_SECRET_KEY = stringPreferencesKey("s3_secret_key")
        private val S3_AUTO_SYNC = booleanPreferencesKey("s3_auto_sync")
    }

    val userSettings: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            nickname = prefs[NICKNAME] ?: "",
            avatarPath = prefs[AVATAR_PATH] ?: "",
            apiEndpoint = prefs[API_ENDPOINT] ?: "",
            apiKey = prefs[API_KEY] ?: "",
            diaryGenerationHour = prefs[DIARY_GEN_HOUR] ?: 22,
            diaryGenerationMinute = prefs[DIARY_GEN_MINUTE] ?: 0,
            s3Endpoint = prefs[S3_ENDPOINT] ?: "",
            s3Bucket = prefs[S3_BUCKET] ?: "",
            s3AccessKey = prefs[S3_ACCESS_KEY] ?: "",
            s3SecretKey = prefs[S3_SECRET_KEY] ?: "",
            s3AutoSync = prefs[S3_AUTO_SYNC] ?: false
        )
    }

    suspend fun updateNickname(nickname: String) {
        dataStore.edit { prefs -> prefs[NICKNAME] = nickname }
    }

    suspend fun updateAvatarPath(path: String) {
        dataStore.edit { prefs -> prefs[AVATAR_PATH] = path }
    }

    suspend fun updateApiConfig(endpoint: String, apiKey: String) {
        dataStore.edit { prefs ->
            prefs[API_ENDPOINT] = endpoint
            prefs[API_KEY] = apiKey
        }
    }

    suspend fun updateDiaryGenerationTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[DIARY_GEN_HOUR] = hour
            prefs[DIARY_GEN_MINUTE] = minute
        }
    }

    suspend fun updateS3Config(
        endpoint: String,
        bucket: String,
        accessKey: String,
        secretKey: String
    ) {
        dataStore.edit { prefs ->
            prefs[S3_ENDPOINT] = endpoint
            prefs[S3_BUCKET] = bucket
            prefs[S3_ACCESS_KEY] = accessKey
            prefs[S3_SECRET_KEY] = secretKey
        }
    }

    suspend fun updateS3AutoSync(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[S3_AUTO_SYNC] = enabled }
    }

    fun hasApiKey(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[API_KEY]?.isNotBlank() == true && prefs[API_ENDPOINT]?.isNotBlank() == true
    }
}