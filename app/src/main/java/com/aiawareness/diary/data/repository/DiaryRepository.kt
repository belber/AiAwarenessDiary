package com.aiawareness.diary.data.repository

import com.aiawareness.diary.data.local.DiaryDao
import com.aiawareness.diary.data.local.DiaryEntity
import com.aiawareness.diary.data.model.Diary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao
) {

    suspend fun saveDiary(diary: Diary) {
        val existingDiary = diaryDao.getDiaryByDate(diary.date)
        if (existingDiary == null) {
            diaryDao.insert(DiaryEntity.fromModel(diary))
        } else {
            diaryDao.update(
                DiaryEntity(
                    id = existingDiary.id,
                    date = diary.date,
                    aiDiary = diary.aiDiary,
                    aiInsight = diary.aiInsight,
                    generatedAt = existingDiary.generatedAt,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateDiary(diary: Diary) {
        diaryDao.update(DiaryEntity.fromModel(diary))
    }

    suspend fun getDiaryByDate(date: String): Diary? = diaryDao.getDiaryByDate(date)?.toModel()

    fun getDiaryByDateFlow(date: String): Flow<Diary?> =
        diaryDao.getDiaryByDateFlow(date).map { it?.toModel() }

    suspend fun deleteDiaryByDate(date: String) {
        diaryDao.deleteByDate(date)
    }

    suspend fun hasDiaryForDate(date: String): Boolean = diaryDao.existsByDate(date)

    suspend fun getAllDiaries(): List<Diary> = diaryDao.getAllDiaries().map { it.toModel() }
}
