package com.aiawareness.diary.data.model

data class UserSettings(
    val nickname: String = "",
    val avatarPath: String = "",
    val apiEndpoint: String = "",
    val apiKey: String = "",
    val diaryGenerationHour: Int = 22,
    val diaryGenerationMinute: Int = 0,
    val s3Endpoint: String = "",
    val s3Bucket: String = "",
    val s3AccessKey: String = "",
    val s3SecretKey: String = "",
    val s3AutoSync: Boolean = false
)