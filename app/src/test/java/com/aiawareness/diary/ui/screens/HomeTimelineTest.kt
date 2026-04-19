package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Record
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTimelineTest {

    @Test
    fun `groups records by approved home periods in chronological order`() {
        val records = listOf(
            Record(date = "2026-04-12", time = "21:18", content = "夜晚"),
            Record(date = "2026-04-12", time = "08:10", content = "清晨"),
            Record(date = "2026-04-12", time = "15:05", content = "下午")
        )

        val sections = buildHomeTimelineSections(records)

        assertEquals(listOf("清晨", "下午", "夜晚"), sections.map { it.title })
        assertEquals(listOf("08:10"), sections[0].records.map { it.time })
        assertEquals(listOf("15:05"), sections[1].records.map { it.time })
        assertEquals(listOf("21:18"), sections[2].records.map { it.time })
    }

    @Test
    fun `maps representative times to expected period labels`() {
        assertEquals("清晨", homePeriodLabel("06:30"))
        assertEquals("上午", homePeriodLabel("10:42"))
        assertEquals("下午", homePeriodLabel("14:20"))
        assertEquals("夜晚", homePeriodLabel("18:30"))
        assertEquals("夜晚", homePeriodLabel("22:08"))
    }

    @Test
    fun `falls back to night when time cannot be parsed`() {
        assertEquals("夜晚", homePeriodLabel("unknown"))
    }

    @Test
    fun `uses tighter timeline bottom padding near input area`() {
        assertEquals(12.dp, homeTimelineBottomPadding(hasPendingPhoto = false))
        assertTrue(homeTimelineBottomPadding(hasPendingPhoto = true) > homeTimelineBottomPadding(hasPendingPhoto = false))
    }
}
