package com.aiawareness.diary.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SettingsScreenContentTest {

    @Test
    fun profileAvatarFallbackLabel_usesTrimmedNicknameInitial() {
        assertEquals("觉", profileAvatarFallbackLabel(" 觉察者 "))
        assertEquals("用", profileAvatarFallbackLabel(""))
    }

    @Test
    fun settingsMenuEntries_flattensToThreePrototypeRows() {
        assertEquals(
            listOf(
                SettingsMenuEntry("AI 配置", "配置 AI 访问地址、模型与生成时间", null),
                SettingsMenuEntry("数据管理", "本地优先，支持导出与导入备份", null),
                SettingsMenuEntry("关于", "理念、版本与隐私承诺", AppVersionLabel)
            ),
            settingsMenuEntries()
        )
    }

    @Test
    fun settingsOverviewCards_useFourDistinctSoftStyles() {
        val cards = settingsOverviewCards(
            SettingsOverviewStats(
                totalDays = 8,
                totalRecords = 21,
                firstRecordDate = "2026-04-07",
                latestRecordDate = "2026-04-14"
            )
        )

        assertEquals(4, cards.size)
        assertEquals("记录天数", cards[0].title)
        assertEquals("8", cards[0].value)
        assertEquals("记录条数", cards[1].title)
        assertEquals("21", cards[1].value)
        assertEquals("开始记录", cards[2].title)
        assertEquals("2026.04.07", cards[2].value)
        assertEquals("最近记录", cards[3].title)
        assertEquals("2026.04.14", cards[3].value)
        assertEquals(1, cards.map { it.containerColor }.toSet().size)
        assertEquals(1, cards.map { it.borderColor }.toSet().size)
        assertEquals(1, cards.map { it.accentColor }.toSet().size)
        assertEquals(false, cards[0].showsAccentBar)
    }

    @Test
    fun settingsHero_noLongerNeedsFallbackQuote() {
        assertFalse(settingsProfileShowsQuote())
    }
}
