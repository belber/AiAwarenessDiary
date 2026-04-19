package com.aiawareness.diary.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.aiawareness.diary.data.local.DiaryDatabase
import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.data.model.UserSettings
import com.aiawareness.diary.data.model.defaultDisplayNickname
import com.aiawareness.diary.data.repository.DiaryRepository
import com.aiawareness.diary.data.repository.RecordRepository
import com.aiawareness.diary.data.repository.SettingsRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class LocalBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: DiaryDatabase,
    private val recordRepository: RecordRepository,
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository
) {

    private val gson = Gson()

    suspend fun exportToZip(destination: Uri): String {
        val records = recordRepository.getAllRecords()
        val diaries = diaryRepository.getAllDiaries()
        val settings = settingsRepository.userSettings.first()
        val avatarBytes = readAvatarBytes(settings.avatarPath)
        val avatarFileName = avatarBytes?.let { "avatar.jpg" }
        val exportedRecords = exportedRecords(records)
        val bundle = BackupBundle(
            manifest = BackupManifest(
                exportedAt = System.currentTimeMillis(),
                recordCount = records.size,
                diaryCount = diaries.size,
                hasSettings = true,
                avatarFileName = avatarFileName
            ),
            records = exportedRecords,
            diaries = diaries,
            settings = settings.copy(
                avatarPath = if (avatarBytes != null) "assets/avatar/avatar.jpg" else ""
            ),
            avatarBytes = avatarBytes,
            avatarFileName = avatarFileName
        )

        context.contentResolver.openOutputStream(destination)?.use { output ->
            ZipOutputStream(output).use { zip ->
                writeJson(zip, "manifest.json", bundle.manifest)
                writeJson(zip, "records.json", bundle.records)
                writeJson(zip, "diaries.json", bundle.diaries)
                writeJson(zip, "settings.json", bundle.settings)
                exportRecordPhotoEntries(records).forEach { asset ->
                    zip.putNextEntry(ZipEntry(asset.path))
                    zip.write(asset.bytes)
                    zip.closeEntry()
                }
                writeDailyMarkdownEntries(
                    zip = zip,
                    nickname = settings.nickname.ifBlank { defaultDisplayNickname() },
                    records = records,
                    diaries = diaries,
                    exportedAt = bundle.manifest.exportedAt
                )
                if (bundle.avatarBytes != null && bundle.avatarFileName != null) {
                    zip.putNextEntry(ZipEntry("assets/avatar/avatar.jpg"))
                    zip.write(bundle.avatarBytes)
                    zip.closeEntry()
                }
            }
        } ?: error("无法打开导出文件")

        return "数据已导出"
    }

    suspend fun previewImport(source: Uri): ImportPreview {
        val bundle = readBundle(source)
        return buildImportPreview(
            localRecords = recordRepository.getAllRecords(),
            importedRecords = bundle.records,
            localDiaries = diaryRepository.getAllDiaries(),
            importedDiaries = bundle.diaries,
            importedSettings = bundle.settings
        )
    }

    suspend fun importFromZip(source: Uri, strategy: ImportConflictStrategy): String {
        val bundle = readBundle(source)
        val localRecords = recordRepository.getAllRecords()
        val localDiaries = diaryRepository.getAllDiaries()
        val localSettings = settingsRepository.userSettings.first()
        val recordPlan = planRecordImport(localRecords, bundle.records, strategy)
        val importedDiaryByDate = bundle.diaries.associateBy { it.date }
        val localDiaryDates = localDiaries.map { it.date }.toSet()
        val settingsToApply = bundle.settings?.let {
            val avatarPath = persistImportedAvatar(bundle.avatarBytes, bundle.avatarFileName)
            mergeImportedSettings(
                local = localSettings,
                imported = it.copy(avatarPath = avatarPath ?: it.avatarPath),
                strategy = strategy
            )
        }

        database.withTransaction {
            recordPlan.deleteKeys.forEach { key ->
                val parts = key.split("|")
                if (parts.size == 3) {
                    recordRepository.deleteByFingerprint(
                        Record(date = parts[0], time = parts[1], content = parts[2])
                    )
                }
            }
            recordPlan.recordsToInsert.forEach { imported ->
                recordRepository.insertRecord(imported.copy(id = 0))
            }

            importedDiaryByDate.forEach { (date, diary) ->
                when (strategy) {
                    ImportConflictStrategy.Merge -> if (date !in localDiaryDates) {
                        diaryRepository.saveDiary(diary.copy(id = 0))
                    }
                    ImportConflictStrategy.ReplaceConflicts -> {
                        diaryRepository.saveDiary(diary.copy(id = 0))
                    }
                }
            }

            if (settingsToApply != null) {
                settingsRepository.replaceAll(settingsToApply)
            }
        }

        return when (strategy) {
            ImportConflictStrategy.Merge -> "导入完成，已合并不冲突的数据"
            ImportConflictStrategy.ReplaceConflicts -> "导入完成，已替换冲突项"
        }
    }

    private fun writeJson(zip: ZipOutputStream, name: String, value: Any?) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(gson.toJson(value).toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun writeDailyMarkdownEntries(
        zip: ZipOutputStream,
        nickname: String,
        records: List<Record>,
        diaries: List<Diary>,
        exportedAt: Long
    ) {
        val recordsByDate = records.groupBy { it.date }
        val diariesByDate = diaries.associateBy { it.date }
        val allDates = (recordsByDate.keys + diariesByDate.keys).toSortedSet()

        allDates.forEach { date ->
            zip.putNextEntry(ZipEntry("assets/journals/$date/images/"))
            zip.closeEntry()
            val markdown = exportDailyMarkdown(
                nickname = nickname,
                date = date,
                diary = diariesByDate[date],
                records = recordsByDate[date].orEmpty(),
                exportedAt = exportedAt
            )
            zip.putNextEntry(ZipEntry("assets/journals/$date/entry.md"))
            zip.write(markdown.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }

    private fun readBundle(source: Uri): BackupBundle {
        val entries = mutableMapOf<String, ByteArray>()
        context.contentResolver.openInputStream(source)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        entries[entry.name] = zip.readBytes()
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: error("无法读取导入文件")

        val settingsText = entries["settings.json"]?.toString(Charsets.UTF_8)
        val settings = settingsText?.takeIf { it.isNotBlank() && it != "null" }?.let {
            gson.fromJson(it, UserSettings::class.java)
        }
        val manifest = gson.fromJson(entries.requireText("manifest.json"), BackupManifest::class.java)
        val avatarEntryName = settings?.avatarPath?.takeIf { it.isNotBlank() }
            ?: manifest.avatarFileName?.let { "assets/avatar/$it" }
        val records = gson.fromJson(entries.requireText("records.json"), Array<Record>::class.java)
            ?.toList()
            .orEmpty()
            .map { record ->
                if (record.photoPath.isBlank()) {
                    record
                } else {
                    record.copy(
                        photoPath = restoreImportedRecordPhoto(
                            filesDir = context.filesDir,
                            pathInZip = record.photoPath,
                            entries = entries
                        ).orEmpty()
                    )
                }
            }
        val diaries = gson.fromJson(entries.requireText("diaries.json"), Array<Diary>::class.java)?.toList().orEmpty()
        return BackupBundle(
            manifest = manifest,
            records = records,
            diaries = diaries,
            settings = settings,
            avatarBytes = avatarEntryName?.let { entries[it] },
            avatarFileName = avatarEntryName
        )
    }

    private fun readAvatarBytes(path: String): ByteArray? {
        if (path.isBlank()) return null
        return openAvatarStream(path)?.use(InputStream::readBytes)
    }

    private fun openAvatarStream(path: String): InputStream? {
        return when {
            path.startsWith("content://") -> {
                val uri = Uri.parse(path)
                context.contentResolver.openInputStream(uri)
            }
            path.startsWith("file://") -> {
                val uri = Uri.parse(path)
                File(uri.path.orEmpty()).takeIf { it.exists() }?.inputStream()
            }
            else -> File(path).takeIf { it.exists() }?.inputStream()
        }
    }

    private fun avatarFileName(path: String): String {
        return when {
            path.startsWith("content://") -> {
                val uri = Uri.parse(path)
                val raw = uri.lastPathSegment ?: File(path).name.ifBlank { "avatar.bin" }
                raw.substringAfterLast('/').ifBlank { "avatar.bin" }
            }
            path.startsWith("file://") -> {
                val uri = Uri.parse(path)
                File(uri.path.orEmpty()).name.ifBlank { "avatar.bin" }
            }
            else -> File(path).name.ifBlank { "avatar.bin" }
        }
    }

    private fun persistImportedAvatar(bytes: ByteArray?, sourcePath: String?): String? {
        if (bytes == null || sourcePath.isNullOrBlank()) return null
        val fileName = File(sourcePath).name.ifBlank { return null }
        val avatarDir = File(context.filesDir, "imported_avatars").apply { mkdirs() }
        val target = File(avatarDir, "${System.currentTimeMillis()}_$fileName")
        target.writeBytes(bytes)
        return target.absolutePath
    }

    private fun Map<String, ByteArray>.requireText(name: String): String =
        this[name]?.toString(Charsets.UTF_8) ?: error("备份文件缺少 $name")
}

fun restoreImportedRecordPhoto(
    filesDir: File,
    pathInZip: String,
    entries: Map<String, ByteArray>
): String? {
    val bytes = entries[pathInZip] ?: return null
    val fileName = pathInZip.substringAfterLast('/').ifBlank { return null }
    val targetDir = File(filesDir, "record_photos").apply { mkdirs() }
    val target = File(targetDir, "import_${System.currentTimeMillis()}_$fileName")
    return runCatching {
        target.writeBytes(bytes)
        target.absolutePath
    }.getOrNull()
}
