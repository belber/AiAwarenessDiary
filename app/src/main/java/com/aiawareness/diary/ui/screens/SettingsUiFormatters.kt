package com.aiawareness.diary.ui.screens

import androidx.compose.ui.graphics.Color
import com.aiawareness.diary.ui.theme.JournalTokens

const val AppVersionLabel = "Version 1.0.4"

data class SettingsMenuEntry(
    val title: String,
    val subtitle: String,
    val trailingText: String?
)

data class SettingsOverviewCard(
    val title: String,
    val value: String,
    val containerColor: Color,
    val borderColor: Color,
    val accentColor: Color,
    val accentLabel: String,
    val showsAccentBar: Boolean
)

fun formatGenerationTimeSummary(hour: Int, minute: Int): String =
    "每日 %02d:%02d 自动生成AI日记和AI觉察".format(hour, minute)

fun profileAvatarFallbackLabel(nickname: String): String =
    nickname.trim().firstOrNull()?.toString() ?: "用"

fun settingsProfileShowsQuote(): Boolean = false

fun settingsOverviewCards(stats: SettingsOverviewStats): List<SettingsOverviewCard> =
    listOf(
        SettingsOverviewCard(
            title = "记录天数",
            value = stats.totalDays.toString(),
            containerColor = JournalTokens.SurfaceLow,
            borderColor = JournalTokens.Stone.copy(alpha = 0.14f),
            accentColor = HomePrimary.copy(alpha = 0.78f),
            accentLabel = "uniform",
            showsAccentBar = false
        ),
        SettingsOverviewCard(
            title = "记录条数",
            value = stats.totalRecords.toString(),
            containerColor = JournalTokens.SurfaceLow,
            borderColor = JournalTokens.Stone.copy(alpha = 0.14f),
            accentColor = HomePrimary.copy(alpha = 0.78f),
            accentLabel = "uniform",
            showsAccentBar = false
        ),
        SettingsOverviewCard(
            title = "开始记录",
            value = stats.firstRecordDate?.replace("-", ".") ?: "还没有",
            containerColor = JournalTokens.SurfaceLow,
            borderColor = JournalTokens.Stone.copy(alpha = 0.14f),
            accentColor = HomePrimary.copy(alpha = 0.78f),
            accentLabel = "uniform",
            showsAccentBar = false
        ),
        SettingsOverviewCard(
            title = "最近记录",
            value = stats.latestRecordDate?.replace("-", ".") ?: "还没有",
            containerColor = JournalTokens.SurfaceLow,
            borderColor = JournalTokens.Stone.copy(alpha = 0.14f),
            accentColor = HomePrimary.copy(alpha = 0.78f),
            accentLabel = "uniform",
            showsAccentBar = false
        )
    )

fun settingsMenuEntries(): List<SettingsMenuEntry> =
    listOf(
        SettingsMenuEntry("AI 配置", "配置 AI 访问地址、模型与生成时间", null),
        SettingsMenuEntry("数据管理", "本地优先，支持导出与导入备份", null),
        SettingsMenuEntry("关于", "理念、版本与隐私承诺", AppVersionLabel)
    )
