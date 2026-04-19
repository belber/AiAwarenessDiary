package com.aiawareness.diary.data.backup

import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun exportDailyMarkdown(
    nickname: String,
    date: String,
    diary: Diary?,
    records: List<Record>,
    exportedAt: Long
): String {
    val exportTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(exportedAt))
    val diaryText = diary?.aiDiary?.ifBlank { "这一天还没有生成 AI 日记。" } ?: "这一天还没有生成 AI 日记。"
    val insightText = diary?.aiInsight?.ifBlank { "这一天还没有生成启发总结。" } ?: "这一天还没有生成启发总结。"
    val recordSection = if (records.isEmpty()) {
        "- 暂无原始记录"
    } else {
        records.joinToString("\n\n") { record ->
            buildString {
                append("### ${record.time}\n")
                append(record.content)
            }
        }
    }

    return """
        ---
        date: $date
        nickname: $nickname
        exported_at: $exportTime
        images_dir: images/
        ---

        # AI 日记

        $diaryText

        ## 启发

        $insightText

        ## 原始记录

        $recordSection
    """.trimIndent()
}
