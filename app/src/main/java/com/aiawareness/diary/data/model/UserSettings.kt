package com.aiawareness.diary.data.model

fun defaultDisplayNickname(): String = "蜉蝣"

const val DEFAULT_AI_API_ENDPOINT: String = "https://dashscope.aliyuncs.com/compatible-mode/v1"
const val DEFAULT_AI_MODEL_NAME: String = "qwen-turbo-latest"

data class UserSettings(
    val nickname: String = "",
    val avatarPath: String = "",
    val profileQuote: String = "",
    val apiEndpoint: String = DEFAULT_AI_API_ENDPOINT,
    val apiKey: String = "",
    val modelName: String = DEFAULT_AI_MODEL_NAME,
    val diaryGenerationHour: Int = 22,
    val diaryGenerationMinute: Int = 0,
    val s3Endpoint: String = "",
    val s3Bucket: String = "",
    val s3AccessKey: String = "",
    val s3SecretKey: String = "",
    val s3AutoSync: Boolean = false
)
