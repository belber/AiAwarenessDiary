package com.aiawareness.diary.ui.screens

import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyContentTest {

    @Test
    fun privacyPolicyFallback_mentionsCoreDataFlows() {
        val text = privacyPolicyFallbackText()

        assertTrue(text.contains("本地"))
        assertTrue(text.contains("OpenAI-compatible", ignoreCase = true))
        assertTrue(text.contains("S3-compatible", ignoreCase = true))
        assertTrue(text.contains("Aliyun APM", ignoreCase = true))
    }
}
