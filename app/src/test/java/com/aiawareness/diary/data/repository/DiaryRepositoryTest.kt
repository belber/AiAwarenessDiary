package com.aiawareness.diary.data.repository

import com.aiawareness.diary.data.local.DiaryDao
import com.aiawareness.diary.data.local.DiaryEntity
import com.aiawareness.diary.data.model.Diary
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DiaryRepositoryTest {

    private val diaryDao: DiaryDao = mock()
    private val repository = DiaryRepository(diaryDao)

    @Test
    fun saveDiary_updatesExistingDiaryForSameDate() = runTest {
        whenever(diaryDao.getDiaryByDate("2026-04-14")).thenReturn(
            DiaryEntity(
                id = 7,
                date = "2026-04-14",
                aiDiary = "旧日记",
                aiInsight = "旧摘要",
                generatedAt = 10L,
                updatedAt = 20L
            )
        )

        repository.saveDiary(
            Diary(
                date = "2026-04-14",
                aiDiary = "新日记",
                aiInsight = "新摘要"
            )
        )

        verify(diaryDao, never()).insert(any())
        verify(diaryDao).update(
            any<DiaryEntity>()
        )
    }

    @Test
    fun saveDiary_insertsWhenDateDoesNotExist() = runTest {
        whenever(diaryDao.getDiaryByDate("2026-04-14")).thenReturn(null)

        repository.saveDiary(
            Diary(
                date = "2026-04-14",
                aiDiary = "新日记",
                aiInsight = "新摘要"
            )
        )

        verify(diaryDao).insert(any())
        verify(diaryDao, never()).update(any())
    }
}
