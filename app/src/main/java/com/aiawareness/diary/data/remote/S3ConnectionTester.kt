package com.aiawareness.diary.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class S3ConnectionTester @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    fun testConnection(
        endpoint: String,
        bucket: String,
        accessKey: String,
        secretKey: String
    ): String {
        require(endpoint.isNotBlank() && bucket.isNotBlank() && accessKey.isNotBlank() && secretKey.isNotBlank()) {
            "请先填写完整的 S3 配置"
        }

        val normalizedEndpoint = endpoint.trim().trimEnd('/')
        val targetUrl = "$normalizedEndpoint/${bucket.trim()}"
        val request = Request.Builder()
            .url(targetUrl)
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                return when (response.code) {
                    in 200..299 -> "S3 连接测试成功"
                    301, 302, 307, 308 -> "S3 连接测试成功，需要确认 Bucket 路径或区域配置"
                    401, 403 -> "S3 服务可达，但鉴权失败，请检查 Access Key / Secret Key"
                    404 -> "找不到对应的 Bucket，请检查 Endpoint 或 Bucket 名称"
                    else -> {
                        val body = response.body?.string()
                        buildConnectionFailureMessage(response.code, body).replaceFirst("连接测试失败", "S3 连接测试失败")
                    }
                }
            }
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("S3 Endpoint 格式不正确")
        } catch (e: IOException) {
            throw IOException("无法连接到 S3 服务，请检查 Endpoint 是否可访问", e)
        }
    }
}
