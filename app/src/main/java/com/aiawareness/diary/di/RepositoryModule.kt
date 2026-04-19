package com.aiawareness.diary.di

import com.aiawareness.diary.data.local.DiaryDao
import com.aiawareness.diary.data.local.RecordDao
import com.aiawareness.diary.data.local.UserPreferences
import com.aiawareness.diary.data.repository.DiaryRepository
import com.aiawareness.diary.data.repository.RecordRepository
import com.aiawareness.diary.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRecordRepository(recordDao: RecordDao): RecordRepository = RecordRepository(recordDao)

    @Provides
    @Singleton
    fun provideDiaryRepository(diaryDao: DiaryDao): DiaryRepository = DiaryRepository(diaryDao)

    @Provides
    @Singleton
    fun provideSettingsRepository(userPreferences: UserPreferences): SettingsRepository =
        SettingsRepository(userPreferences)
}
