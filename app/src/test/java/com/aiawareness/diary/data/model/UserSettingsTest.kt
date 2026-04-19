package com.aiawareness.diary.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserSettingsTest {

    @Test
    fun defaultDisplayNickname_matchesWarmDefaultCopy() {
        assertEquals("蜉蝣", defaultDisplayNickname())
    }

    @Test
    fun defaultModelName_startsBlank() {
        assertEquals("", UserSettings().modelName)
    }
}
