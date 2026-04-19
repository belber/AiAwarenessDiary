package com.aiawareness.diary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RecordEntity::class, DiaryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
    abstract fun diaryDao(): DiaryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE records ADD COLUMN photoPath TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
