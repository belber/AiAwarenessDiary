package com.aiawareness.diary.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class OpenAiApiSupportTest {

    @Test
    fun normalizeApiBaseUrl_appendsTrailingSlashWithoutDuplicatingV1() {
        assertEquals(
            "https://dashscope.aliyuncs.com/compatible-mode/v1/",
            normalizeApiBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
        )
    }

    @Test
    fun buildApiFailureMessage_includesStatusCodeAndProviderMessage() {
        assertEquals(
            "API 调用失败(404): Model not found",
            buildApiFailureMessage(404, "{\"message\":\"Model not found\"}")
        )
    }
}
