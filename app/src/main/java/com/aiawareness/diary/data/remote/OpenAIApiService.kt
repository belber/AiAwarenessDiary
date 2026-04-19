package com.aiawareness.diary.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApiService {

    @POST("chat/completions")
    suspend fun generateCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): Response<OpenAIResponse>
}
