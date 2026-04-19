package com.aiawareness.diary.data.repository

import com.aiawareness.diary.data.local.RecordDao
import com.aiawareness.diary.data.local.RecordEntity
import com.aiawareness.diary.data.model.Record
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RecordRepositoryTest {

    private val recordDao: RecordDao = mock()
    private val repository = RecordRepository(recordDao)

    @Test
    fun getRecordsByDate_mapsEntitiesToModels() = runTest {
        whenever(recordDao.getRecordsByDate("2026-04-11")).thenReturn(
            listOf(
                RecordEntity(
                    id = 7L,
                    date = "2026-04-11",
                    time = "08:00",
                    content = "完成晨间复盘",
                    createdAt = 100L,
                    updatedAt = 120L
                )
            )
        )

        val records = repository.getRecordsByDate("2026-04-11")

        assertEquals(
            listOf(
                Record(
                    id = 7L,
                    date = "2026-04-11",
                    time = "08:00",
                    content = "完成晨间复盘",
                    createdAt = 100L,
                    updatedAt = 120L
                )
            ),
            records
        )
    }

    @Test
    fun getRecordsByDate_mapsPhotoPathFromEntity() = runTest {
        whenever(recordDao.getRecordsByDate("2026-04-15")).thenReturn(
            listOf(
                RecordEntity(
                    id = 8L,
                    date = "2026-04-15",
                    time = "09:20",
                    content = "晨间散步",
                    photoPath = "/files/record_photos/p1.jpg",
                    createdAt = 100L,
                    updatedAt = 120L
                )
            )
        )

        val records = repository.getRecordsByDate("2026-04-15")

        assertEquals("/files/record_photos/p1.jpg", records.single().photoPath)
    }

    @Test
    fun insertRecord_convertsModelAndDelegatesToDao() = runTest {
        val record = Record(
            id = 5L,
            date = "2026-04-11",
            time = "10:00",
            content = "和朋友喝咖啡",
            createdAt = 11L,
            updatedAt = 22L
        )
        whenever(recordDao.insert(RecordEntity.fromModel(record))).thenReturn(33L)

        val insertedId = repository.insertRecord(record)

        assertEquals(33L, insertedId)
        verify(recordDao).insert(RecordEntity.fromModel(record))
    }
}
