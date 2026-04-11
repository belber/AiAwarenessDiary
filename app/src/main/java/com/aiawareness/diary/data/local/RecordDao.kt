package com.aiawareness.diary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM records WHERE date = :date ORDER BY time ASC")
    suspend fun getRecordsByDate(date: String): List<RecordEntity>

    @Query("SELECT * FROM records WHERE date = :date ORDER BY time ASC")
    fun getRecordsByDateFlow(date: String): Flow<List<RecordEntity>>

    @Query("SELECT DISTINCT date FROM records ORDER BY date DESC")
    suspend fun getDatesWithRecords(): List<String>

    @Query("SELECT COUNT(*) FROM records WHERE date = :date")
    suspend fun getRecordCountByDate(date: String): Int

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getRecordById(id: Long): RecordEntity?
}