package com.aiawareness.diary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diary: DiaryEntity): Long

    @Update
    suspend fun update(diary: DiaryEntity)

    @Query("SELECT * FROM diaries WHERE date = :date ORDER BY updatedAt DESC, id DESC LIMIT 1")
    suspend fun getDiaryByDate(date: String): DiaryEntity?

    @Query("SELECT * FROM diaries WHERE date = :date ORDER BY updatedAt DESC, id DESC LIMIT 1")
    fun getDiaryByDateFlow(date: String): Flow<DiaryEntity?>

    @Query("DELETE FROM diaries WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT EXISTS(SELECT 1 FROM diaries WHERE date = :date)")
    suspend fun existsByDate(date: String): Boolean

    @Query("SELECT * FROM diaries ORDER BY date ASC, updatedAt ASC, id ASC")
    suspend fun getAllDiaries(): List<DiaryEntity>
}
