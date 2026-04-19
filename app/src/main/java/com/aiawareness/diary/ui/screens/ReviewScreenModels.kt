package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

enum class ReviewSectionKind {
    RawRecords,
    AiDiary,
    AiSummary
}

enum class ReviewAccentTone {
    Sage,
    Clay,
    Stone
}

data class ReviewSectionAccent(
    val iconName: String,
    val eyebrow: String,
    val tone: ReviewAccentTone
)

data class ReviewDiaryDisplay(
    val title: String,
    val body: String
)

fun reviewMonthTitle(year: Int, month: Int): String =
    "${Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH)} $year"

fun reviewMonthChipLabel(month: Int): String = "${month}月"

fun reviewYearOptions(currentYear: Int, datesWithRecords: Set<String>): List<Int> {
    val years = datesWithRecords.mapNotNull {
        runCatching { LocalDate.parse(it).year }.getOrNull()
    }
    val minYear = minOf(years.minOrNull() ?: currentYear, currentYear - 2)
    val maxYear = maxOf(years.maxOrNull() ?: currentYear, currentYear + 1)
    return (minYear..maxYear).toList().reversed()
}

fun hasDiarySummary(diary: Diary?): Boolean =
    diary != null && (diary.aiDiary.isNotBlank() || diary.aiInsight.isNotBlank())

fun reviewEntryCountLabel(count: Int): String = "$count entries"

fun reviewDiaryActionLabel(hasApiKey: Boolean, hasDiary: Boolean, isGenerating: Boolean): String =
    when {
        isGenerating -> "生成中..."
        !hasApiKey -> "前往配置"
        hasDiary -> "重新生成"
        else -> "生成AI日记"
    }

fun reviewDiaryGenerationHint(recordCount: Int): String? =
    if (recordCount == 0) "先记录今天的内容，再生成 AI 日记" else null

fun reviewJumpToTodayLabel(): String = "回到今日"

fun reviewAiSummaryTitle(): String = "AI觉察"

fun reviewTimelineBottomPadding() = 24.dp

fun reviewDiaryPhotoPaths(hasAiDiary: Boolean, records: List<Record>): List<String> =
    if (!hasAiDiary) {
        emptyList()
    } else {
        records.map { it.photoPath }.filter { it.isNotBlank() }.distinct()
    }

fun reviewDiaryDisplay(text: String): ReviewDiaryDisplay {
    val lines = text.lines().filter { it.isNotBlank() }
    val title = lines.firstOrNull().orEmpty()
        .removePrefix("标题：")
        .removePrefix("标题:")
        .trim()
    val body = lines.drop(1).joinToString("\n").trim()
    return ReviewDiaryDisplay(title = title, body = body)
}

fun reviewRecordShowsPhoto(record: Record): Boolean = record.photoPath.isNotBlank()

fun reviewInsightHasPattern(tone: ReviewAccentTone): Boolean =
    tone == ReviewAccentTone.Sage

fun reviewSectionDisplayOrder(): List<ReviewSectionKind> = listOf(
    ReviewSectionKind.AiDiary,
    ReviewSectionKind.AiSummary,
    ReviewSectionKind.RawRecords
)

fun reviewSectionAccent(kind: ReviewSectionKind): ReviewSectionAccent =
    when (kind) {
        ReviewSectionKind.RawRecords -> ReviewSectionAccent(
            iconName = "menu_book",
            eyebrow = "ORIGINAL NOTES",
            tone = ReviewAccentTone.Stone
        )
        ReviewSectionKind.AiDiary -> ReviewSectionAccent(
            iconName = "auto_awesome",
            eyebrow = "AI DIARY",
            tone = ReviewAccentTone.Sage
        )
        ReviewSectionKind.AiSummary -> ReviewSectionAccent(
            iconName = "analytics",
            eyebrow = "AI SUMMARY",
            tone = ReviewAccentTone.Clay
        )
    }
