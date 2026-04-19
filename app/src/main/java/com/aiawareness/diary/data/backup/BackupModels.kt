package com.aiawareness.diary.data.backup

import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.data.model.UserSettings
import java.io.File

enum class ImportConflictStrategy {
    Merge,
    ReplaceConflicts
}

private const val BACKUP_ASSET_ROOT_DIR = "assets"
private const val BACKUP_JOURNAL_ASSET_DIR = "$BACKUP_ASSET_ROOT_DIR/journals"
private const val BACKUP_JOURNAL_IMAGE_DIR = "images"
private const val BACKUP_JOURNAL_IMAGE_EXTENSION = "jpg"

data class BackupManifest(
    val version: Int = 1,
    val exportedAt: Long,
    val recordCount: Int,
    val diaryCount: Int,
    val hasSettings: Boolean,
    val avatarFileName: String?
)

data class BackupBundle(
    val manifest: BackupManifest,
    val records: List<Record>,
    val diaries: List<Diary>,
    val settings: UserSettings?,
    val avatarBytes: ByteArray?,
    val avatarFileName: String?
)

data class ImportPreview(
    val recordAdds: Int,
    val recordConflicts: Int,
    val diaryAdds: Int,
    val diaryConflicts: Int,
    val hasSettings: Boolean
)

data class RecordImportPlan(
    val deleteKeys: Set<String>,
    val recordsToInsert: List<Record>
)

data class BackupPhotoAsset(
    val path: String,
    val bytes: ByteArray
)

fun Record.fingerprint(): String = "$date|$time|$content"

fun exportedRecords(records: List<Record>): List<Record> {
    val photoIndexByDate = mutableMapOf<String, Int>()
    return records.map { record ->
        val sourcePhoto = File(record.photoPath)
        val exportedPhotoPath =
            if (sourcePhoto.exists() && sourcePhoto.isFile) {
                record.photoPath.toJournalAssetPath(photoIndexByDate, record.date)
            } else {
                null
            }

        record.copy(photoPath = exportedPhotoPath.orEmpty())
    }
}

fun exportRecordPhotoEntries(records: List<Record>): List<BackupPhotoAsset> {
    val photoIndexByDate = mutableMapOf<String, Int>()
    return records.mapNotNull { record ->
        val file = File(record.photoPath)
        if (!file.exists() || !file.isFile) return@mapNotNull null

        val relativePhotoPath = record.photoPath.toJournalAssetPath(photoIndexByDate, record.date)
            ?: return@mapNotNull null

        BackupPhotoAsset(
            path = relativePhotoPath,
            bytes = file.readBytes()
        )
    }
}

fun buildImportPreview(
    localRecords: List<Record>,
    importedRecords: List<Record>,
    localDiaries: List<Diary>,
    importedDiaries: List<Diary>,
    importedSettings: UserSettings?
): ImportPreview {
    val localRecordKeys = localRecords.map { it.fingerprint() }.toSet()
    val importedRecordKeys = importedRecords.map { it.fingerprint() }.toSet()
    val localDiaryDates = localDiaries.map { it.date }.toSet()
    val importedDiaryDates = importedDiaries.map { it.date }.toSet()

    val recordConflicts = importedRecordKeys.count { it in localRecordKeys }
    val diaryConflicts = importedDiaryDates.count { it in localDiaryDates }

    return ImportPreview(
        recordAdds = importedRecordKeys.size - recordConflicts,
        recordConflicts = recordConflicts,
        diaryAdds = importedDiaryDates.size - diaryConflicts,
        diaryConflicts = diaryConflicts,
        hasSettings = importedSettings != null
    )
}

fun planRecordImport(
    localRecords: List<Record>,
    importedRecords: List<Record>,
    strategy: ImportConflictStrategy
): RecordImportPlan {
    val localKeys = localRecords.map { it.fingerprint() }.toSet()
    val importedDistinct = importedRecords.distinctBy { it.fingerprint() }

    return when (strategy) {
        ImportConflictStrategy.Merge -> RecordImportPlan(
            deleteKeys = emptySet(),
            recordsToInsert = importedDistinct.filterNot { it.fingerprint() in localKeys }
        )
        ImportConflictStrategy.ReplaceConflicts -> {
            val conflictingKeys = importedDistinct.map { it.fingerprint() }.filter { it in localKeys }.toSet()
            RecordImportPlan(
                deleteKeys = conflictingKeys,
                recordsToInsert = importedDistinct
            )
        }
    }
}

fun mergeImportedSettings(
    local: UserSettings,
    imported: UserSettings,
    strategy: ImportConflictStrategy
): UserSettings =
    when (strategy) {
        ImportConflictStrategy.ReplaceConflicts -> imported
        ImportConflictStrategy.Merge -> UserSettings(
            nickname = local.nickname.ifBlank { imported.nickname },
            avatarPath = local.avatarPath.ifBlank { imported.avatarPath },
            profileQuote = local.profileQuote.ifBlank { imported.profileQuote },
            apiEndpoint = local.apiEndpoint.ifBlank { imported.apiEndpoint },
            apiKey = local.apiKey.ifBlank { imported.apiKey },
            modelName = local.modelName.takeIf { it.isNotBlank() && it != UserSettings().modelName } ?: imported.modelName,
            diaryGenerationHour = if (local.diaryGenerationHour != UserSettings().diaryGenerationHour) local.diaryGenerationHour else imported.diaryGenerationHour,
            diaryGenerationMinute = if (local.diaryGenerationMinute != UserSettings().diaryGenerationMinute) local.diaryGenerationMinute else imported.diaryGenerationMinute,
            s3Endpoint = local.s3Endpoint.ifBlank { imported.s3Endpoint },
            s3Bucket = local.s3Bucket.ifBlank { imported.s3Bucket },
            s3AccessKey = local.s3AccessKey.ifBlank { imported.s3AccessKey },
            s3SecretKey = local.s3SecretKey.ifBlank { imported.s3SecretKey },
            s3AutoSync = local.s3AutoSync || imported.s3AutoSync
        )
    }

private fun String.toJournalAssetPath(
    photoIndexByDate: MutableMap<String, Int>,
    date: String
): String? {
    if (isBlank()) return null

    val nextIndex = photoIndexByDate.getOrDefault(date, 0)
    photoIndexByDate[date] = nextIndex + 1

    val fileName = "${(nextIndex + 1).toString().padStart(3, '0')}.$BACKUP_JOURNAL_IMAGE_EXTENSION"
    return "$BACKUP_JOURNAL_ASSET_DIR/$date/$BACKUP_JOURNAL_IMAGE_DIR/$fileName"
}
