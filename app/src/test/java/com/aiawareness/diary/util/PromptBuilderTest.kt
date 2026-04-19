package com.aiawareness.diary.util

import com.aiawareness.diary.data.model.Record
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    @Test
    fun buildPrompt_includesNicknameFormattedDateAndRecords() {
        val records = listOf(
            Record(date = "2026-04-11", time = "09:30", content = "晨跑 5 公里"),
            Record(date = "2026-04-11", time = "21:00", content = "整理阅读笔记")
        )

        val prompt = PromptBuilder.buildPrompt(
            nickname = "小贝",
            date = "2026-04-11",
            records = records
        )

        assertTrue(prompt.contains("用户昵称：小贝"))
        assertTrue(prompt.contains("日期：2026年4月11日"))
        assertTrue(prompt.contains("- 09:30 这条记录仅用于理解先后顺序，不得在【日记】中直接复述时刻：晨跑 5 公里"))
        assertTrue(prompt.contains("- 21:00 这条记录仅用于理解先后顺序，不得在【日记】中直接复述时刻：整理阅读笔记"))
        assertTrue(prompt.contains("请把下面这些原始记录整理成一篇“忠于记录、不补充事实”的简短日记"))
        assertTrue(prompt.contains("如果信息不足，就写得很短"))
        assertTrue(prompt.contains("不能新增任何记录中没有出现的信息"))
        assertTrue(prompt.contains("按记录顺序和主题关系整理成 1 到 3 段短文"))
        assertTrue(prompt.contains("尽量保留记录里的真实信息层次"))
    }

    @Test
    fun buildPrompt_withLowSignalInput_usesMoreConservativeInstructions() {
        val records = listOf(
            Record(date = "2026-04-11", time = "09:30", content = "nihao")
        )

        val prompt = PromptBuilder.buildPrompt(
            nickname = "小贝",
            date = "2026-04-11",
            records = records
        )

        assertTrue(prompt.contains("这些记录整体信息量偏低"))
        assertTrue(prompt.contains("正文最多写 1 到 2 句"))
        assertTrue(prompt.contains("不要把短词、测试词、占位词扩写成事件、情绪或故事"))
        assertTrue(prompt.contains("如果信息不足，请直接承认信息不足"))
    }

    @Test
    fun parseAiResponse_extractsDiaryAndInsightSections() {
        val response = """
            【日记】
            今天过得很充实，先去晨跑，晚上又整理了阅读笔记。

            【启发】
            节奏稳定时，思考也更清晰。
        """.trimIndent()

        val parsed = PromptBuilder.parseAiResponse(response)

        assertEquals("今天过得很充实，先去晨跑，晚上又整理了阅读笔记。", parsed.first)
        assertEquals("节奏稳定时，思考也更清晰。", parsed.second)
    }

    @Test
    fun systemPrompt_enforcesFaithfulReconstructionAndLowInference() {
        val prompt = PromptBuilder.getSystemPrompt()

        assertTrue(prompt.contains("你不是创作助手，你是“日记记录整理员”"))
        assertTrue(prompt.contains("宁可简短、不完整，也不能补充不存在的事实"))
        assertTrue(prompt.contains("你绝对不能新增以下内容"))
        assertTrue(prompt.contains("未被记录的事件"))
        assertTrue(prompt.contains("未被记录的情绪"))
        assertTrue(prompt.contains("无意义字符、测试词、占位词"))
        assertTrue(prompt.contains("不得扩写成完整故事"))
        assertTrue(prompt.contains("润色”只允许发生在表达层，不允许发生在事实层"))
        assertTrue(prompt.contains("不要为了“像日记”而脑补细节"))
        assertTrue(prompt.contains("天气未知"))
        assertTrue(prompt.contains("只要原始记录不为空，就必须输出一句具体的启发"))
        assertTrue(prompt.contains("禁止输出“今天的信息较少"))
        assertTrue(prompt.contains("如果找不到依据，就删除"))
        assertFalse(prompt.contains("标题：YYYY年M月D日 周X 天气未知"))
    }

    @Test
    fun parseAiResponse_stripsLegacyTitlePrefix() {
        val response = """
            【日记】
            标题：2026年4月19日 周日 天气未知
            今天把要做的事一条条记了下来。

            【启发】
            你像是在用记录帮自己把注意力收回来。
        """.trimIndent()

        val parsed = PromptBuilder.parseAiResponse(response)

        assertEquals("2026年4月19日 周日 天气未知\n今天把要做的事一条条记了下来。", parsed.first)
        assertEquals("你像是在用记录帮自己把注意力收回来。", parsed.second)
    }

    @Test
    fun ensureInsightText_usesSpecificFallbackWhenModelReturnsGenericCopy() {
        val records = listOf(
            Record(date = "2026-04-19", time = "09:20", content = "开会前先写下今天最重要的两件事"),
            Record(date = "2026-04-19", time = "21:10", content = "晚上又回看了上午列的清单")
        )

        val insight = PromptBuilder.ensureInsightText(
            records = records,
            insight = "今天的信息较少，暂时看不出更清晰的线索。"
        )

        assertFalse(insight.contains("今天的信息较少"))
        assertTrue(insight.contains("开会前先写下今天最重要的两件事"))
    }

    @Test
    fun ensureInsightText_keepsModelInsightWhenItIsSpecific() {
        val records = listOf(
            Record(date = "2026-04-19", time = "09:20", content = "晨跑后精神好了很多")
        )

        val insight = PromptBuilder.ensureInsightText(
            records = records,
            insight = "你像是在通过先动起来，让状态慢慢归位。"
        )

        assertEquals("你像是在通过先动起来，让状态慢慢归位。", insight)
    }
}
