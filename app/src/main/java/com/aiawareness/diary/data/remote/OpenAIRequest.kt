package com.aiawareness.diary.data.remote

import com.google.gson.annotations.SerializedName

data class OpenAIRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 500
) {
    data class Message(
        val role: String,
        val content: String
    )
}
