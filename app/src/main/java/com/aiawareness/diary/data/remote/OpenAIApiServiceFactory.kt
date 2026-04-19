package com.aiawareness.diary.data.remote

import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Retrofit

@Singleton
class OpenAIApiServiceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {
    fun create(endpoint: String): OpenAIApiService =
        retrofitBuilder
            .baseUrl(normalizeApiBaseUrl(endpoint))
            .build()
            .create(OpenAIApiService::class.java)
}
