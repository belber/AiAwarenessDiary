package com.aiawareness.diary.ui.screens

fun formatGenerationTimeSummary(hour: Int, minute: Int): String =
    "每日 %02d:%02d 自动汇总结语".format(hour, minute)

fun profileQuoteOrFallback(value: String): String =
    if (value.isBlank()) "“回到呼吸，也回到自己”" else value
