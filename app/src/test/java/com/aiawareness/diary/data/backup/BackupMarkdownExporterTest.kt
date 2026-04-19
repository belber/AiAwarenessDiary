package com.aiawareness.diary.data.backup

import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupMarkdownExporterTest {

    @Test
    fun exportDailyMarkdown_includesFrontMatterDiaryInsightAndRecords() {
        val markdown = exportDailyMarkdown(
            nickname = "小贝",
            date = "2026-04-14",
            diary = Diary(
                date = "2026-04-14",
                aiDiary = "今天把想到的内容慢慢记了下来。",
                aiInsight = "也许节奏放慢时，更容易看见真实感受。"
            ),
            records = listOf(
                Record(date = "2026-04-14", time = "08:30", content = "晨跑后心跳慢慢平下来"),
                Record(date = "2026-04-14", time = "21:10", content = "睡前读了几页书")
            ),
            exportedAt = 1713024000000L
        )

        assertTrue(markdown.contains("---"))
        assertTrue(markdown.contains("date: 2026-04-14"))
        assertTrue(markdown.contains("nickname: 小贝"))
        assertTrue(markdown.contains("images_dir: images/"))
        assertTrue(markdown.contains("# AI 日记"))
        assertTrue(markdown.contains("## 启发"))
        assertTrue(markdown.contains("## 原始记录"))
        assertTrue(markdown.contains("### 08:30"))
        assertTrue(markdown.contains("晨跑后心跳慢慢平下来"))
        assertTrue(markdown.contains("### 21:10"))
    }
}
