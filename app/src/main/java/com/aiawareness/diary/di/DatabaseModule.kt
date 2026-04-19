package com.aiawareness.diary.di

import android.content.Context
import androidx.room.Room
import com.aiawareness.diary.data.local.DiaryDao
import com.aiawareness.diary.data.local.DiaryDatabase
import com.aiawareness.diary.data.local.RecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DiaryDatabase =
        Room.databaseBuilder(
            context,
            DiaryDatabase::class.java,
            "diary_database"
        )
            .addMigrations(DiaryDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideRecordDao(database: DiaryDatabase): RecordDao = database.recordDao()

    @Provides
    fun provideDiaryDao(database: DiaryDatabase): DiaryDao = database.diaryDao()
}
