package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.R
import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenModelsTest {

    @Test
    fun homeDecorations_removeLegacyProgressDots() {
        assertEquals(emptyList<Boolean>(), progressDots(recordCount = 7))
    }

    @Test
    fun iconForRecordContent_mapsWindTextToAir() {
        val record = Record(id = 1, date = "2026-04-13", time = "18:30", content = "窗外的风吹动了窗帘")
        assertEquals(HomeRecordIcon.Air, iconForHomeRecord(record))
    }

    @Test
    fun homeRecordPhotoVisibility_reflectsWhetherPhotoPathExists() {
        assertEquals(
            true,
            homeRecordShowsPhoto(
                Record(id = 1, date = "2026-04-13", time = "18:30", content = "拍了一张窗外", photoPath = "/tmp/p1.jpg")
            )
        )
        assertEquals(
            false,
            homeRecordShowsPhoto(
                Record(id = 2, date = "2026-04-13", time = "18:31", content = "没有图片", photoPath = "")
            )
        )
    }

    @Test
    fun recordDateBadge_formatsMonthDayAndWeekday() {
        assertEquals("4月13日 周一", homeRecordDateBadge("2026-04-13"))
    }

    @Test
    fun headlineDate_reusesDateBadgeFormat() {
        assertEquals("4月13日 周一", homeHeadlineDate("2026-04-13"))
    }

    @Test
    fun headlineDate_splitsIntoMonthDayAndWeekday() {
        assertEquals("4月13日", homeDateMonthDay("2026-04-13"))
        assertEquals("周一", homeDateWeekday("2026-04-13"))
    }

    @Test
    fun homeCopy_preservesKeyHomepageLabels() {
        assertEquals("AI日记本", homeTitleLabel())
        assertEquals("回顾", homeReviewButtonLabel())
        assertEquals("推荐豆包语音输入更快", homeInputPlaceholderText())
        assertEquals("上传图片", homeAttachPhotoButtonLabel())
        assertEquals("已添加图片", homePendingPhotoStatusLabel())
        assertEquals("图片预览", homePendingPhotoPreviewLabel())
        assertEquals(false, homePendingPhotoShowsLabels())
        assertEquals("发送", homeSendButtonLabel())
        assertEquals("回到呼吸，心里浮现什么？", homeProgressSubtitle())
        assertEquals("觉察提示", homeProgressEyebrow())
        assertEquals("今天的觉察，还没开始", homeEmptyStateTitle())
        assertEquals("从一个很小的当下开始，写下此刻的呼吸、身体感觉，或刚刚闪过的念头。", homeEmptyStateBody())
    }

    @Test
    fun homePhotoEntry_usesActionSheetBeforeLaunchingPicker() {
        assertEquals(HomePhotoEntryMode.ActionSheet, homePhotoEntryMode())
        assertEquals("从相册选择", homePhotoSheetPickLabel())
        assertEquals("拍照", homePhotoSheetCaptureLabel())
    }

    @Test
    fun homeEmptyStateVisuals_reduceNoiseAndSeparateFromDiaryCards() {
        assertEquals(false, homeEmptyStateShowsIcon())
        assertEquals(true, homeEmptyStateUsesAlternateSurface())
    }

    @Test
    fun homeBranding_usesDedicatedSparkAssets() {
        assertEquals(R.drawable.ic_home_title_spark, homeTitleBrandIconRes())
        assertEquals(R.drawable.ic_home_input_spark, homeInputBrandIconRes())
    }

    @Test
    fun homeAiFabLayout_placesButtonOutsideInputBarAtTopRight() {
        assertEquals(HomeAiFabHorizontalAlignment.End, homeAiFabHorizontalAlignment())
        assertEquals(HomeAiFabVerticalAlignment.Bottom, homeAiFabVerticalAlignment())
        assertTrue(homeAiFabBottomPadding(hasPendingPhoto = false).value > homeInputBarVerticalPadding().value)
        assertTrue(homeAiFabBottomPadding(hasPendingPhoto = false).value > homeAiFabSize().value)
        assertTrue(homeAiFabEndPadding().value >= 16f)
        assertTrue(homeAiFabBottomPadding(hasPendingPhoto = true).value > homeAiFabBottomPadding(hasPendingPhoto = false).value)
    }

    @Test
    fun homeTimelineBottomPadding_reducesDefaultGapButExpandsForPendingPhoto() {
        assertEquals(12f, homeTimelineBottomPadding(hasPendingPhoto = false).value)
        assertTrue(homeTimelineBottomPadding(hasPendingPhoto = true).value > homeTimelineBottomPadding(hasPendingPhoto = false).value)
    }

    @Test
    fun homeAiEntryLabel_matchesExpectedStates() {
        assertEquals("先记录，再生成 AI 日记", homeAiEntryButtonLabel(recordCount = 0, hasApiKey = false, hasDiary = false, isGenerating = false))
        assertEquals("先配置 AI", homeAiEntryButtonLabel(recordCount = 2, hasApiKey = false, hasDiary = false, isGenerating = false))
        assertEquals("生成 AI 日记", homeAiEntryButtonLabel(recordCount = 2, hasApiKey = true, hasDiary = false, isGenerating = false))
        assertEquals("重新生成", homeAiEntryButtonLabel(recordCount = 2, hasApiKey = true, hasDiary = true, isGenerating = false))
        assertEquals("生成中...", homeAiEntryButtonLabel(recordCount = 2, hasApiKey = true, hasDiary = true, isGenerating = true))
    }

    @Test
    fun homeAiEntryAction_matchesExpectedStates() {
        assertEquals(HomeAiEntryAction.ShowRecordHint, homeAiEntryAction(recordCount = 0, hasApiKey = false, isGenerating = false))
        assertEquals(HomeAiEntryAction.ShowAiConfigHint, homeAiEntryAction(recordCount = 2, hasApiKey = false, isGenerating = false))
        assertEquals(HomeAiEntryAction.GenerateDiary, homeAiEntryAction(recordCount = 2, hasApiKey = true, isGenerating = false))
        assertEquals(HomeAiEntryAction.None, homeAiEntryAction(recordCount = 2, hasApiKey = true, isGenerating = true))
    }

    @Test
    fun homeAiEntryCopy_preservesHelperText() {
        assertEquals("请前往配置 AI 和每日自动生成 AI 日记时间", homeAiEntryHint(hasApiKey = false))
        assertEquals(null, homeAiEntryHint(hasApiKey = true))
    }

    @Test
    fun homeAiConfigRequiredSnackbarMessage_explainsWhyTapDidNotNavigate() {
        assertEquals("可一键生成AI日记，请先前往设置中完成 AI配置", homeAiConfigRequiredSnackbarMessage())
    }

    @Test
    fun homeAiEntryCopy_prefersAutoGenerationFailureHint() {
        assertEquals(
            "AI 日记生成失败，请检查账号、网络状态、连接配置等",
            homeAiEntryHint(hasApiKey = true, autoGenerationFailed = true)
        )
        assertEquals(
            "AI 日记生成失败，请检查账号、网络状态、连接配置等",
            homeAiEntryHint(hasApiKey = false, autoGenerationFailed = true)
        )
    }

    @Test
    fun homeAiEntryCopy_supportsCatchUpAndTodayAutoGeneratedHints() {
        assertEquals(
            "已补生成 3 篇 AI 日记，可前往回顾查看",
            homeAiEntryHint(hasApiKey = true, catchUpGeneratedCount = 3, todayAutoGenerated = false, autoGenerationFailed = false)
        )
        assertEquals(
            "今日 AI 日记已自动生成，可前往回顾查看",
            homeAiEntryHint(hasApiKey = true, catchUpGeneratedCount = 0, todayAutoGenerated = true, autoGenerationFailed = false)
        )
    }

    @Test
    fun homeAiSnackbarMessage_prioritizesReadyThenInlineThenAutoHints() {
        assertEquals(
            "AI 日记已生成，可前往回顾页查看",
            homeAiSnackbarMessage(
                generatedDiaryReadyDate = "2026-04-19",
                inlineHint = "先记录今天的内容，再生成 AI 日记",
                autoGenerationHint = "已补生成 3 篇 AI 日记，可前往回顾查看"
            )
        )
        assertEquals(
            "先记录今天的内容，再生成 AI 日记",
            homeAiSnackbarMessage(
                generatedDiaryReadyDate = null,
                inlineHint = "先记录今天的内容，再生成 AI 日记",
                autoGenerationHint = "已补生成 3 篇 AI 日记，可前往回顾查看",
            )
        )
        assertEquals(
            "已补生成 3 篇 AI 日记，可前往回顾查看",
            homeAiSnackbarMessage(
                generatedDiaryReadyDate = null,
                inlineHint = null,
                autoGenerationHint = "已补生成 3 篇 AI 日记，可前往回顾查看"
            )
        )
        assertEquals(
            null,
            homeAiSnackbarMessage(generatedDiaryReadyDate = null, inlineHint = null, autoGenerationHint = null)
        )
    }

    @Test
    fun homeAutoGenerationSnackbarHint_onlyReturnsSnackbarWorthyMessages() {
        assertEquals(
            "已补生成 2 篇 AI 日记，可前往回顾查看",
            homeAutoGenerationSnackbarHint(
                catchUpGeneratedCount = 2,
                todayAutoGenerated = false,
                autoGenerationFailed = false
            )
        )
        assertEquals(
            "今日 AI 日记已自动生成，可前往回顾查看",
            homeAutoGenerationSnackbarHint(
                catchUpGeneratedCount = 0,
                todayAutoGenerated = true,
                autoGenerationFailed = false
            )
        )
        assertEquals(
            "AI 日记生成失败，请检查账号、网络状态、连接配置等",
            homeAutoGenerationSnackbarHint(
                catchUpGeneratedCount = 0,
                todayAutoGenerated = false,
                autoGenerationFailed = true
            )
        )
        assertEquals(
            null,
            homeAutoGenerationSnackbarHint(
                catchUpGeneratedCount = 0,
                todayAutoGenerated = false,
                autoGenerationFailed = false
            )
        )
    }

}
