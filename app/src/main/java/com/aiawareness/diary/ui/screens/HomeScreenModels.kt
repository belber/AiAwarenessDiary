package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Record

enum class HomeRecordIcon { Breath, Air, Rest, Heart, Reflection }

fun progressDots(recordCount: Int): List<Boolean> =
    List(4) { index -> index < recordCount.coerceAtMost(4) }

fun iconForHomeRecord(record: Record): HomeRecordIcon {
    val content = record.content
    return when {
        "风" in content || "窗" in content -> HomeRecordIcon.Air
        "疲惫" in content || "困" in content -> HomeRecordIcon.Rest
        "呼吸" in content -> HomeRecordIcon.Breath
        "心跳" in content || "胸" in content -> HomeRecordIcon.Heart
        else -> HomeRecordIcon.Reflection
    }
}
