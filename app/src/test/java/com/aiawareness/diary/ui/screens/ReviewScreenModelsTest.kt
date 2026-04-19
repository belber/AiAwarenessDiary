package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewScreenModelsTest {

    @Test
    fun reviewHeader_formatsMonthTitle() {
        assertEquals("April 2026", reviewMonthTitle(2026, 4))
    }

    @Test
    fun reviewMonthChipLabel_formatsChineseMonth() {
        assertEquals("4月", reviewMonthChipLabel(4))
    }

    @Test
    fun hasDiarySummary_isFalseWhenDiaryEmpty() {
        assertTrue(!hasDiarySummary(Diary(date = "2026-04-13", aiDiary = "", aiInsight = "")))
    }

    @Test
    fun reviewEntryCountLabel_formatsEnglishCount() {
        assertEquals("3 entries", reviewEntryCountLabel(3))
    }

    @Test
    fun reviewSectionAccent_usesPrototypeMetadata() {
        assertEquals(
            ReviewSectionAccent(iconName = "auto_awesome", eyebrow = "AI DIARY", tone = ReviewAccentTone.Sage),
            reviewSectionAccent(ReviewSectionKind.AiDiary)
        )
    }

    @Test
    fun reviewDiaryActionLabel_matchesState() {
        assertEquals("前往配置", reviewDiaryActionLabel(hasApiKey = false, hasDiary = false, isGenerating = false))
        assertEquals("生成AI日记", reviewDiaryActionLabel(hasApiKey = true, hasDiary = false, isGenerating = false))
        assertEquals("重新生成", reviewDiaryActionLabel(hasApiKey = true, hasDiary = true, isGenerating = false))
        assertEquals("生成中...", reviewDiaryActionLabel(hasApiKey = true, hasDiary = true, isGenerating = true))
    }

    @Test
    fun reviewDiaryGenerationHint_requiresRecordsBeforeGeneration() {
        assertEquals("先记录今天的内容，再生成 AI 日记", reviewDiaryGenerationHint(recordCount = 0))
        assertEquals(null, reviewDiaryGenerationHint(recordCount = 2))
    }

    @Test
    fun reviewJumpToTodayLabel_matchesUpdatedCopy() {
        assertEquals("回到今日", reviewJumpToTodayLabel())
    }

    @Test
    fun reviewAiSummaryTitle_matchesUpdatedCopy() {
        assertEquals("AI觉察", reviewAiSummaryTitle())
    }

    @Test
    fun reviewSectionDisplayOrder_prioritizesAiContentBeforeRawRecords() {
        assertEquals(
            listOf(
                ReviewSectionKind.AiDiary,
                ReviewSectionKind.AiSummary,
                ReviewSectionKind.RawRecords
            ),
            reviewSectionDisplayOrder()
        )
    }

    @Test
    fun reviewYearOptions_coversRecordYearsAndNearbyYears() {
        assertEquals(
            listOf(2027, 2026, 2025, 2024),
            reviewYearOptions(
                currentYear = 2026,
                datesWithRecords = setOf("2024-05-01", "2026-04-13")
            )
        )
    }

    @Test
    fun reviewRecordTimeStyle_matchesHomePalette() {
        assertEquals(HomePrimary, reviewRecordTimeTextColor())
        assertEquals(HomePrimarySoft, reviewRecordTimeContainerColor())
        assertFalse(reviewRecordTimeUsesAccentColor())
    }

    @Test
    fun reviewInsightPattern_isEnabledOnlyForAiDiaryTone() {
        assertTrue(reviewInsightHasPattern(ReviewAccentTone.Sage))
        assertFalse(reviewInsightHasPattern(ReviewAccentTone.Clay))
        assertFalse(reviewInsightHasPattern(ReviewAccentTone.Stone))
    }

    @Test
    fun reviewDiaryPhotoPaths_returnsUniquePhotosOnlyWhenAiDiaryExists() {
        val records = listOf(
            Record(id = 1, date = "2026-04-15", time = "09:00", content = "a", photoPath = "/tmp/p1.jpg"),
            Record(id = 2, date = "2026-04-15", time = "10:00", content = "b", photoPath = ""),
            Record(id = 3, date = "2026-04-15", time = "11:00", content = "c", photoPath = "/tmp/p1.jpg"),
            Record(id = 4, date = "2026-04-15", time = "12:00", content = "d", photoPath = "/tmp/p2.jpg")
        )

        assertEquals(
            listOf("/tmp/p1.jpg", "/tmp/p2.jpg"),
            reviewDiaryPhotoPaths(hasAiDiary = true, records = records)
        )
        assertEquals(
            emptyList<String>(),
            reviewDiaryPhotoPaths(hasAiDiary = false, records = records)
        )
    }

    @Test
    fun reviewRecordPhotoVisibility_reflectsWhetherPhotoPathExists() {
        assertTrue(
            reviewRecordShowsPhoto(
                Record(id = 1, date = "2026-04-15", time = "09:00", content = "a", photoPath = "/tmp/p1.jpg")
            )
        )
        assertFalse(
            reviewRecordShowsPhoto(
                Record(id = 2, date = "2026-04-15", time = "10:00", content = "b", photoPath = "")
            )
        )
    }

    @Test
    fun reviewDiaryDisplay_splitTitleFromBody() {
        val display = reviewDiaryDisplay("2026年4月16日 周四 天气晴\n今天把想到的事情慢慢记了下来。")

        assertEquals("2026年4月16日 周四 天气晴", display.title)
        assertEquals("今天把想到的事情慢慢记了下来。", display.body)
    }

    @Test
    fun reviewDiaryDisplay_stripsLegacyTitlePrefix() {
        val display = reviewDiaryDisplay("标题：2026年4月16日 周四 天气晴\n今天把想到的事情慢慢记了下来。")

        assertEquals("2026年4月16日 周四 天气晴", display.title)
        assertEquals("今天把想到的事情慢慢记了下来。", display.body)
    }

    @Test
    fun reviewTimelineBottomPadding_keepsLastRecordCloseToBottom() {
        assertEquals(24f, reviewTimelineBottomPadding().value)
    }
}
