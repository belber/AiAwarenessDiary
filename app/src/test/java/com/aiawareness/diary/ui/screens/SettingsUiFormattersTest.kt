package com.aiawareness.diary.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SettingsUiFormattersTest {

    @Test
    fun formatGenerationTime_padsHourAndMinute() {
        assertEquals("每日 07:05 自动生成AI日记和AI觉察", formatGenerationTimeSummary(7, 5))
    }

    @Test
    fun settingsProfile_disablesQuoteSection() {
        assertFalse(settingsProfileShowsQuote())
    }

    @Test
    fun formatGenerationTimeSummary_usesTwoDigitStyle() {
        assertEquals("每日 22:00 自动生成AI日记和AI觉察", formatGenerationTimeSummary(22, 0))
    }

    @Test
    fun settingsMenuEntries_useUpdatedAiConfigCopy() {
        val entries = settingsMenuEntries()

        assertEquals("AI 配置", entries.first().title)
        assertEquals("配置 AI 访问地址、模型与生成时间", entries.first().subtitle)
    }

    @Test
    fun settingsOverviewCards_formatsMissingDates() {
        val cards = settingsOverviewCards(
            SettingsOverviewStats(
                totalDays = 0,
                totalRecords = 0,
                firstRecordDate = null,
                latestRecordDate = null
            )
        )

        assertEquals("还没有", cards[2].value)
        assertEquals("还没有", cards[3].value)
    }
}
