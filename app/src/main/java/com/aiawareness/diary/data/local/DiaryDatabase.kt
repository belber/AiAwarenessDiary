package com.aiawareness.diary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecordEntity::class, DiaryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
    abstract fun diaryDao(): DiaryDao
}