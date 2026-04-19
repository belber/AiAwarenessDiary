package com.aiawareness.diary.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiGuideScreenModelsTest {

    @Test
    fun aiGuideCopy_matchesExpectedLabels() {
        assertEquals("查看 AI 配置教程", aiConfigGuideEntryLabel())
        assertEquals("AI 配置教程", aiConfigGuideScreenTitle())
        assertEquals("教程加载失败，请检查网络后重试", aiConfigGuideLoadErrorMessage())
        assertEquals("重新加载", aiConfigGuideRetryLabel())
        assertEquals("help", aiConfigGuideEntryIconName())
        assertTrue(aiConfigGuideEntryShowsTrailingArrow())
    }

    @Test
    fun aiGuideUrl_pointsToConfiguredFeishuDoc() {
        assertTrue(aiConfigGuideUrl().startsWith("https://acnhno6ekylq.feishu.cn/wiki/"))
    }
}
