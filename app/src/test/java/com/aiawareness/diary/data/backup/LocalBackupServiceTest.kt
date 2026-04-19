package com.aiawareness.diary.data.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.aiawareness.diary.data.local.DiaryDatabase
import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.data.model.UserSettings
import com.aiawareness.diary.data.repository.DiaryRepository
import com.aiawareness.diary.data.repository.RecordRepository
import com.aiawareness.diary.data.repository.SettingsRepository
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipFile
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LocalBackupServiceTest {

    private val gson = Gson()
    private val context: Context = mock()
    private val contentResolver: ContentResolver = mock()
    private val database: DiaryDatabase = mock()
    private val recordRepository: RecordRepository = mock()
    private val diaryRepository: DiaryRepository = mock()
    private val settingsRepository: SettingsRepository = mock()

    @Test
    fun exportToZip_writesAssetsUnderDateScopedJournalDirectories() = runTest {
        val tempDir = createTempDirectory(prefix = "backup-export-layout").toFile()
        try {
            val photoFile = File(tempDir, "record.jpg").apply {
                writeBytes(byteArrayOf(1, 2, 3))
            }
            val avatarFile = File(tempDir, "profile-image.png").apply {
                writeBytes(byteArrayOf(4, 5, 6))
            }
            val zipFile = File(tempDir, "backup.zip")
            val destination: Uri = mock()

            whenever(context.contentResolver).thenReturn(contentResolver)
            whenever(contentResolver.openOutputStream(destination)).thenReturn(FileOutputStream(zipFile))
            whenever(recordRepository.getAllRecords()).thenReturn(
                listOf(
                    Record(
                        date = "2026-04-15",
                        time = "09:20",
                        content = "晨间散步",
                        photoPath = photoFile.absolutePath
                    )
                )
            )
            whenever(diaryRepository.getAllDiaries()).thenReturn(
                listOf(
                    Diary(
                        date = "2026-04-15",
                        aiDiary = "今天整理了待办。",
                        aiInsight = "慢下来能看到更多细节。"
                    )
                )
            )
            whenever(settingsRepository.userSettings).thenReturn(
                flowOf(
                    UserSettings(
                        nickname = "小贝",
                        avatarPath = avatarFile.absolutePath
                    )
                )
            )

            val service = LocalBackupService(
                context = context,
                database = database,
                recordRepository = recordRepository,
                diaryRepository = diaryRepository,
                settingsRepository = settingsRepository
            )

            service.exportToZip(destination)

            val entryNames = readZipEntryNames(zipFile)

            assertTrue(entryNames.contains("manifest.json"))
            assertTrue(entryNames.contains("records.json"))
            assertTrue(entryNames.contains("diaries.json"))
            assertTrue(entryNames.contains("settings.json"))
            assertTrue(entryNames.contains("assets/journals/2026-04-15/entry.md"))
            assertTrue(entryNames.contains("assets/journals/2026-04-15/images/001.jpg"))
            assertTrue(entryNames.contains("assets/avatar/avatar.jpg"))
            assertFalse(entryNames.any { it.startsWith("records/photos/") })
            assertFalse(entryNames.any { it == "avatar/avatar.jpg" })

            val exportedRecord = readZipEntryText(zipFile, "records.json")
                .let { gson.fromJson(it, Array<Record>::class.java).single() }
            val exportedManifest = readZipEntryText(zipFile, "manifest.json")
                .let { gson.fromJson(it, BackupManifest::class.java) }
            val exportedSettings = readZipEntryText(zipFile, "settings.json")
                .let { gson.fromJson(it, UserSettings::class.java) }

            assertTrue(exportedRecord.photoPath == "assets/journals/2026-04-15/images/001.jpg")
            assertTrue(exportedManifest.avatarFileName == "avatar.jpg")
            assertTrue(exportedSettings.avatarPath == "assets/avatar/avatar.jpg")
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun exportToZip_producesSpecCompliantStructure() = runTest {
        val tempDir = createTempDirectory(prefix = "backup-export-spec").toFile()
        try {
            val photoOne = File(tempDir, "record-one.jpg").apply {
                writeBytes(byteArrayOf(1, 2, 3))
            }
            val photoTwo = File(tempDir, "record-two.jpg").apply {
                writeBytes(byteArrayOf(4, 5, 6))
            }
            val avatarFile = File(tempDir, "profile-image.png").apply {
                writeBytes(byteArrayOf(7, 8, 9))
            }
            val zipFile = File(tempDir, "backup.zip")
            val destination: Uri = mock()

            whenever(context.contentResolver).thenReturn(contentResolver)
            whenever(contentResolver.openOutputStream(destination)).thenReturn(FileOutputStream(zipFile))
            whenever(recordRepository.getAllRecords()).thenReturn(
                listOf(
                    Record(
                        date = "2026-04-15",
                        time = "09:20",
                        content = "晨间散步",
                        photoPath = photoOne.absolutePath
                    ),
                    Record(
                        date = "2026-04-16",
                        time = "21:05",
                        content = "晚间复盘",
                        photoPath = photoTwo.absolutePath
                    )
                )
            )
            whenever(diaryRepository.getAllDiaries()).thenReturn(
                listOf(
                    Diary(
                        date = "2026-04-15",
                        aiDiary = "今天整理了待办。",
                        aiInsight = "慢下来能看到更多细节。"
                    ),
                    Diary(
                        date = "2026-04-16",
                        aiDiary = "今天收尾了工作。",
                        aiInsight = "稳定推进比一次冲刺更重要。"
                    )
                )
            )
            whenever(settingsRepository.userSettings).thenReturn(
                flowOf(
                    UserSettings(
                        nickname = "小贝",
                        avatarPath = avatarFile.absolutePath
                    )
                )
            )

            val service = LocalBackupService(
                context = context,
                database = database,
                recordRepository = recordRepository,
                diaryRepository = diaryRepository,
                settingsRepository = settingsRepository
            )

            service.exportToZip(destination)

            val entryNames = readZipEntryNames(zipFile).toSet()
            val exportedRecords = readZipEntryText(zipFile, "records.json")
                .let { gson.fromJson(it, Array<Record>::class.java).toList() }
            val exportedSettings = readZipEntryText(zipFile, "settings.json")
                .let { gson.fromJson(it, UserSettings::class.java) }

            assertTrue(entryNames.containsAll(setOf("manifest.json", "records.json", "diaries.json", "settings.json")))
            assertTrue(entryNames.contains("assets/journals/2026-04-15/entry.md"))
            assertTrue(entryNames.contains("assets/journals/2026-04-16/entry.md"))
            assertTrue(exportedRecords.all { it.photoPath.startsWith("assets/journals/") })
            assertTrue(exportedRecords.all { entryNames.contains(it.photoPath) })
            assertEquals("assets/avatar/avatar.jpg", exportedSettings.avatarPath)
            assertTrue(entryNames.contains(exportedSettings.avatarPath))
            assertFalse(entryNames.any { it.startsWith("records/photos/") })
            assertFalse(entryNames.any { it.startsWith("journals/") })
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun importFromZip_restoresRecordPhotosAndAvatarFromAssetsPaths() = runTest {
        val tempDir = createTempDirectory(prefix = "backup-import-roundtrip").toFile()
        try {
            val zipFile = File(tempDir, "backup.zip")
            writeImportZip(
                zipFile = zipFile,
                manifest = BackupManifest(
                    exportedAt = 1713110400000,
                    recordCount = 1,
                    diaryCount = 1,
                    hasSettings = true,
                    avatarFileName = "legacy-avatar.png"
                ),
                recordPhotoPath = "assets/journals/2026-04-15/images/001.jpg",
                avatarPath = "assets/avatar/avatar.jpg"
            )

            val source: Uri = mock()
            whenever(context.contentResolver).thenReturn(contentResolver)
            whenever(context.filesDir).thenReturn(tempDir)
            whenever(contentResolver.openInputStream(source)).thenReturn(zipFile.inputStream())
            whenever(database.transactionExecutor).thenReturn(Executor { runnable -> runnable.run() })
            whenever(database.suspendingTransactionId).thenReturn(ThreadLocal())
            whenever(recordRepository.getAllRecords()).thenReturn(emptyList())
            whenever(diaryRepository.getAllDiaries()).thenReturn(emptyList())
            whenever(settingsRepository.userSettings).thenReturn(
                flowOf(
                    UserSettings(
                        nickname = "本地",
                        avatarPath = ""
                    )
                )
            )

            val service = LocalBackupService(
                context = context,
                database = database,
                recordRepository = recordRepository,
                diaryRepository = diaryRepository,
                settingsRepository = settingsRepository
            )

            service.importFromZip(source, ImportConflictStrategy.ReplaceConflicts)

            val recordCaptor = argumentCaptor<Record>()
            verify(recordRepository).insertRecord(recordCaptor.capture())
            val importedRecord = recordCaptor.firstValue
            assertTrue(importedRecord.photoPath.startsWith(tempDir.absolutePath))
            assertTrue(importedRecord.photoPath.contains("record_photos"))
            assertTrue(File(importedRecord.photoPath).readBytes().contentEquals(byteArrayOf(1, 2, 3)))

            val settingsCaptor = argumentCaptor<UserSettings>()
            verify(settingsRepository).replaceAll(settingsCaptor.capture())
            val localAvatarPath = settingsCaptor.firstValue.avatarPath
            assertTrue(localAvatarPath.startsWith(tempDir.absolutePath))
            assertTrue(localAvatarPath.contains("imported_avatars"))
            assertTrue(File(localAvatarPath).readBytes().contentEquals(byteArrayOf(4, 5, 6)))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun readZipEntryText(zipFile: File, entryName: String): String =
        ZipFile(zipFile).use { zip ->
            zip.getEntry(entryName)?.let { entry ->
                zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: error("Missing zip entry: $entryName")
        }

    private fun readZipEntryNames(zipFile: File): List<String> =
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().map { it.name }.toList()
        }

    private fun writeImportZip(
        zipFile: File,
        manifest: BackupManifest,
        recordPhotoPath: String,
        avatarPath: String
    ) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
            writeJson(zip, "manifest.json", manifest)
            writeJson(
                zip,
                "records.json",
                listOf(
                    Record(
                        date = "2026-04-15",
                        time = "09:20",
                        content = "晨间散步",
                        photoPath = recordPhotoPath
                    )
                )
            )
            writeJson(
                zip,
                "diaries.json",
                listOf(
                    Diary(
                        date = "2026-04-15",
                        aiDiary = "今天整理了待办。",
                        aiInsight = "慢下来能看到更多细节。"
                    )
                )
            )
            writeJson(
                zip,
                "settings.json",
                UserSettings(
                    nickname = "导入",
                    avatarPath = avatarPath
                )
            )
            zip.putNextEntry(ZipEntry(recordPhotoPath))
            zip.write(byteArrayOf(1, 2, 3))
            zip.closeEntry()
            zip.putNextEntry(ZipEntry(avatarPath))
            zip.write(byteArrayOf(4, 5, 6))
            zip.closeEntry()
        }
    }

    private fun writeJson(zip: ZipOutputStream, name: String, value: Any?) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(gson.toJson(value).toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}
