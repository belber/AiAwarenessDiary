package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Record

data class HomeTimelineSection(
    val title: String,
    val records: List<Record>
)

private val HomeTimelineOrder = listOf("清晨", "上午", "下午", "傍晚", "夜晚")

fun buildHomeTimelineSections(records: List<Record>): List<HomeTimelineSection> {
    val grouped = records
        .sortedBy { it.time }
        .groupBy { homePeriodLabel(it.time) }

    return HomeTimelineOrder.mapNotNull { title ->
        val sectionRecords = grouped[title] ?: return@mapNotNull null
        HomeTimelineSection(title = title, records = sectionRecords)
    }
}

fun homePeriodLabel(time: String): String {
    val hour = time.substringBefore(":").toIntOrNull() ?: return "夜晚"
    return when (hour) {
        in 0..8 -> "清晨"
        in 9..11 -> "上午"
        in 12..17 -> "下午"
        in 18..20 -> "傍晚"
        else -> "夜晚"
    }
}
