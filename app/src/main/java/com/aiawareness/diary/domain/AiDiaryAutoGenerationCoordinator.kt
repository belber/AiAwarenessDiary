package com.aiawareness.diary.domain

import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.data.model.UserSettings
import com.aiawareness.diary.data.remote.OpenAIRequest
import com.aiawareness.diary.data.remote.OpenAIApiServiceFactory
import com.aiawareness.diary.data.remote.safeLogError
import com.aiawareness.diary.data.repository.DiaryRepository
import com.aiawareness.diary.data.repository.RecordRepository
import com.aiawareness.diary.data.repository.SettingsRepository
import com.aiawareness.diary.util.DateUtil
import com.aiawareness.diary.util.PromptBuilder
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

data class AutoGenerationOutcome(
    val generated: Boolean,
    val failed: Boolean
)

data class CatchUpGenerationOutcome(
    val generatedCount: Int,
    val failed: Boolean
)

@Singleton
class AiDiaryAutoGenerationCoordinator @Inject constructor(
    private val recordRepository: RecordRepository,
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository,
    private val apiServiceFactory: OpenAIApiServiceFactory
) {

    suspend fun generateTodayIfNeeded(): AutoGenerationOutcome {
        val settings = settingsRepository.userSettings.first()
        if (!settings.hasCompleteApiConfig()) {
            return AutoGenerationOutcome(generated = false, failed = false)
        }

        val today = DateUtil.getCurrentDate()
        val records = recordRepository.getRecordsByDate(today)
        if (!shouldGenerateForDate(today, records)) {
            return AutoGenerationOutcome(generated = false, failed = false)
        }

        val generated = generateDiaryForDate(
            targetDate = today,
            records = records,
            settings = settings
        )
        return AutoGenerationOutcome(generated = generated, failed = !generated)
    }

    suspend fun generateRecentMissingDiaries(limit: Int = 5): CatchUpGenerationOutcome {
        val settings = settingsRepository.userSettings.first()
        if (!settings.hasCompleteApiConfig() || limit <= 0) {
            return CatchUpGenerationOutcome(generatedCount = 0, failed = false)
        }

        var generatedCount = 0
        var failed = false
        val recentDates = recordRepository.getDatesWithRecords()
            .mapNotNull(::parseDateOrNull)
            .sortedDescending()
            .map(LocalDate::toString)

        for (date in recentDates) {
            if (generatedCount >= limit) {
                break
            }
            val records = recordRepository.getRecordsByDate(date)
            if (!shouldGenerateForDate(date, records)) {
                continue
            }

            if (generateDiaryForDate(date, records, settings)) {
                generatedCount += 1
            } else {
                failed = true
            }
        }

        return CatchUpGenerationOutcome(
            generatedCount = generatedCount,
            failed = failed
        )
    }

    private suspend fun generateDiaryForDate(
        targetDate: String,
        records: List<Record>,
        settings: UserSettings
    ): Boolean {
        return try {
            val apiService = apiServiceFactory.create(settings.apiEndpoint)
            val prompt = PromptBuilder.buildPrompt(
                nickname = settings.nickname,
                date = targetDate,
                records = records
            )
            val request = OpenAIRequest(
                model = settings.modelName,
                messages = listOf(
                    OpenAIRequest.Message("system", PromptBuilder.getSystemPrompt()),
                    OpenAIRequest.Message("user", prompt)
                ),
                temperature = PromptBuilder.DIARY_GENERATION_TEMPERATURE
            )
            val response = apiService.generateCompletion(
                authorization = "Bearer ${settings.apiKey}",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.choices.firstOrNull()?.message?.content.orEmpty()
                val (diaryText, insightText) = PromptBuilder.parseAiResponse(content)
                val normalizedInsight = PromptBuilder.ensureInsightText(
                    records = records,
                    insight = insightText
                )
                diaryRepository.saveDiary(
                    Diary(
                        date = targetDate,
                        aiDiary = diaryText,
                        aiInsight = normalizedInsight
                    )
                )
                true
            } else {
                val errorBody = response.errorBody()?.string()
                safeLogError(
                    "AiDiaryAutoGenerationCoordinator",
                    "generateDiary failed: date=$targetDate code=${response.code()} body=$errorBody"
                )
                false
            }
        } catch (e: Exception) {
            safeLogError("AiDiaryAutoGenerationCoordinator", "generateDiary exception for $targetDate", e)
            false
        }
    }

    private fun parseDateOrNull(value: String): LocalDate? =
        try {
            LocalDate.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }

    private suspend fun shouldGenerateForDate(
        targetDate: String,
        records: List<Record>
    ): Boolean {
        if (records.isEmpty()) {
            return false
        }

        val existingDiary = diaryRepository.getDiaryByDate(targetDate) ?: return true
        val latestRecordUpdateAt = records.maxOf { maxOf(it.updatedAt, it.createdAt) }
        return latestRecordUpdateAt > existingDiary.updatedAt
    }

    private fun UserSettings.hasCompleteApiConfig(): Boolean =
        apiEndpoint.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank()
}
