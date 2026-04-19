package com.aiawareness.diary.data.remote

import android.util.Log

fun normalizeApiBaseUrl(endpoint: String): String =
    endpoint.trim().removeSuffix("/") + "/"

fun buildApiFailureMessage(statusCode: Int, errorBody: String?): String {
    val providerMessage = extractProviderErrorMessage(errorBody)
    return if (providerMessage.isBlank()) {
        "API 调用失败($statusCode)"
    } else {
        "API 调用失败($statusCode): $providerMessage"
    }
}

fun buildConnectionFailureMessage(statusCode: Int, errorBody: String?): String {
    val providerMessage = extractProviderErrorMessage(errorBody)
    return if (providerMessage.isBlank()) {
        "连接测试失败($statusCode)"
    } else {
        "连接测试失败($statusCode): $providerMessage"
    }
}

fun buildExceptionMessage(prefix: String, throwable: Throwable): String {
    val detail = throwable.message?.trim().takeUnless { it.isNullOrEmpty() } ?: throwable::class.java.simpleName
    return "$prefix: $detail"
}

fun safeLogError(tag: String, message: String, throwable: Throwable? = null) {
    runCatching {
        if (throwable == null) {
            Log.e(tag, message)
        } else {
            Log.e(tag, message, throwable)
        }
    }
}

private fun extractProviderErrorMessage(errorBody: String?): String {
    if (errorBody.isNullOrBlank()) return ""
    val normalized = errorBody.trim()
    Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
        .find(normalized)
        ?.groupValues
        ?.getOrNull(1)
        ?.let { return it.trim() }
    Regex("\"code\"\\s*:\\s*\"([^\"]+)\"")
        .find(normalized)
        ?.groupValues
        ?.getOrNull(1)
        ?.let { return it.trim() }
    return normalized
}
