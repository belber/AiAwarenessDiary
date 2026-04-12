package com.aiawareness.diary.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsUiFormattersTest {

    @Test
    fun formatGenerationTime_padsHourAndMinute() {
        assertEquals("每日 07:05 自动汇总结语", formatGenerationTimeSummary(7, 5))
    }

    @Test
    fun profileQuote_usesFallbackWhenBlank() {
        assertEquals("“回到呼吸，也回到自己”", profileQuoteOrFallback(""))
    }
}
