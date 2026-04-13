package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Record
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeScreenModelsTest {

    @Test
    fun progressDots_capsAtFour() {
        assertEquals(listOf(true, true, true, true), progressDots(recordCount = 7))
    }

    @Test
    fun iconForRecordContent_mapsWindTextToAir() {
        val record = Record(id = 1, date = "2026-04-13", time = "18:30", content = "窗外的风吹动了窗帘")
        assertEquals(HomeRecordIcon.Air, iconForHomeRecord(record))
    }
}
