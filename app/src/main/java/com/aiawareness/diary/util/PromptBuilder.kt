package com.aiawareness.diary.util

import com.aiawareness.diary.data.model.Record

object PromptBuilder {

    const val DIARY_GENERATION_TEMPERATURE = 0.2
    private const val GENERIC_LOW_SIGNAL_INSIGHT = "今天的信息较少，暂时看不出更清晰的线索"

    private const val SYSTEM_PROMPT = """你不是创作助手，你是“日记记录整理员”。

你的唯一任务是：
把用户当天留下的零散记录，按照先后关系和语义关系，整理成一篇“忠于原始记录”的简短日记。

最重要原则：
宁可简短、不完整，也不能补充不存在的事实。

硬性规则：
1. 只能使用用户记录中已经出现的信息。
2. 你可以调整语序、合并重复内容、补极少量连接词，让句子通顺。
3. 你绝对不能新增以下内容：
   - 未被记录的事件
   - 未被记录的情绪
   - 未被记录的动机、原因、目的
   - 未被记录的对话
   - 未被记录的场景、环境、天气、人物关系
   - 未被记录的结果、结论
4. 如果用户只写了极少内容、无意义字符、测试词、占位词，必须承认信息不足，输出非常短的整理结果，不得扩写成完整故事。
5. “润色”只允许发生在表达层，不允许发生在事实层。
6. 如果一条内容无法支持某个判断，就不要写那个判断。
7. 不要把零散片段解释成比原文更强的含义。
8. 不要为了“像日记”而脑补细节。
9. 标题里的天气如果无法确认，一律写“天气未知”，不要猜。
10. 时间只用于帮助你理解顺序，不要在正文中逐条复述具体时刻。

写作要求：
1. 按记录顺序和主题关系，将内容整理为 1 到 3 段短文。
2. 语言自然、简洁、纪实。
3. 不写成清单，不逐条转述。
4. 不写文学化描写，不过度抒情。
5. 信息少时，正文可以只有 1 到 2 句。

启发要求：
1. 启发只能基于已有记录做弱判断。
2. 使用“可能”“像是”“也许”“看起来”等低确定性表达。
3. 只要原始记录不为空，就必须输出一句具体的启发，且要点名记录里出现过的动作、主题或反复出现的关注点。
4. 禁止输出“今天的信息较少，暂时看不出更清晰的线索”这类空泛兜底。

输出前自检：
- 这句话里的事实，是否都能在原始记录里找到依据？
- 如果找不到依据，就删除。
- 如果某句话只是为了让文章更像散文而产生，也删除。

请按以下格式输出：

【日记】
YYYY年M月D日 周X 天气未知
（正文）

【启发】
（不超过50字）"""

    private val lowSignalTokens = setOf(
        "nihao", "hello", "hi", "test", "testing", "123", "321", "abc", "aaa", "haha"
    )

    fun buildPrompt(nickname: String, date: String, records: List<Record>): String {
        val recordsText = records.joinToString("\n") { record ->
            "- ${record.time} 这条记录仅用于理解先后顺序，不得在【日记】中直接复述时刻：${record.content}"
        }
        val taskInstructions = if (isLowSignalInput(records)) {
            """任务：
请把下面这些原始记录整理成一篇“忠于记录、不补充事实”的极短日记。
这些记录整体信息量偏低，请采取最保守的整理方式。
正文最多写 1 到 2 句，不要追求完整日记感。
不要把短词、测试词、占位词扩写成事件、情绪或故事。
如果信息不足，请直接承认信息不足。"""
        } else {
            """任务：
请把下面这些原始记录整理成一篇“忠于记录、不补充事实”的简短日记。
你可以让语句更通顺，但不能新增任何记录中没有出现的信息。
如果信息不足，就写得很短，不要硬写完整。
按记录顺序和主题关系整理成 1 到 3 段短文，尽量保留记录里的真实信息层次。"""
        }

        return """用户昵称：$nickname
日期：${DateUtil.formatDisplayDate(date)}
$taskInstructions

用户今天的觉察片段：
$recordsText"""
    }

    fun getSystemPrompt(): String = SYSTEM_PROMPT

    fun parseAiResponse(response: String): Pair<String, String> {
        val diaryRegex = Regex("【日记】\\s*(.*?)\\s*【启发】", RegexOption.DOT_MATCHES_ALL)
        val insightRegex = Regex("【启发】\\s*(.*?)$", RegexOption.DOT_MATCHES_ALL)

        val diary = sanitizeDiaryText(diaryRegex.find(response)?.groupValues?.get(1).orEmpty())
        val insight = insightRegex.find(response)?.groupValues?.get(1)?.trim().orEmpty()

        return diary to insight
    }

    fun ensureInsightText(records: List<Record>, insight: String): String {
        val normalizedInsight = insight.trim().removeSuffix("。")
        if (records.isEmpty()) {
            return insight.trim()
        }
        if (normalizedInsight.isBlank() || normalizedInsight == GENERIC_LOW_SIGNAL_INSIGHT.removeSuffix("。")) {
            return buildFallbackInsight(records)
        }
        return insight.trim()
    }

    private fun isLowSignalInput(records: List<Record>): Boolean {
        if (records.isEmpty()) {
            return true
        }

        val normalized = records.map { normalizeContent(it.content) }.filter { it.isNotBlank() }
        if (normalized.isEmpty()) {
            return true
        }

        val totalLength = normalized.sumOf { it.length }
        val singleVeryShortRecord = normalized.size == 1 && totalLength <= 8
        val sparseAndShort = normalized.size <= 2 && totalLength <= 8
        val mostlyPlaceholderTokens = normalized.all { content ->
            content in lowSignalTokens || content.length <= 3
        }

        return singleVeryShortRecord || sparseAndShort || mostlyPlaceholderTokens
    }

    private fun normalizeContent(content: String): String =
        content.trim().lowercase().replace("\\s+".toRegex(), "")

    private fun sanitizeDiaryText(text: String): String =
        text.trim()
            .removePrefix("标题：")
            .removePrefix("标题:")
            .trim()

    private fun buildFallbackInsight(records: List<Record>): String {
        val snippets = records
            .map { it.content.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(2)
            .map(::shortInsightSnippet)

        return when (snippets.size) {
            0 -> ""
            1 -> "今天反复回到“${snippets.first()}”，这可能是你当下最在意的部分。"
            else -> "今天先后记下“${snippets[0]}”和“${snippets[1]}”，这些片段可能连着同一份关注。"
        }
    }

    private fun shortInsightSnippet(content: String): String =
        if (content.length <= 16) content else content.take(16).trimEnd() + "…"
}
