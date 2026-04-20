package com.aiawareness.diary.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserSettingsTest {

    @Test
    fun defaultDisplayNickname_matchesWarmDefaultCopy() {
        assertEquals("蜉蝣", defaultDisplayNickname())
    }

    @Test
    fun defaultApiEndpoint_matchesDashScopeCompatibleEndpoint() {
        assertEquals(
            "https://dashscope.aliyuncs.com/compatible-mode/v1",
            UserSettings().apiEndpoint
        )
    }

    @Test
    fun defaultModelName_matchesDashScopeTurboLatest() {
        assertEquals("qwen-turbo-latest", UserSettings().modelName)
    }
}
