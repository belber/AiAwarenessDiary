package com.aiawareness.diary.data.backup

import com.aiawareness.diary.data.model.Diary
import com.aiawareness.diary.data.model.Record
import com.aiawareness.diary.data.model.UserSettings
import java.io.File
import kotlin.io.path.createTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupMergePlannerTest {

    @Test
    fun buildImportPreview_countsAddsAndConflicts() {
        val localRecords = listOf(
            Record(date = "2026-04-14", time = "08:00", content = "晨跑"),
            Record(date = "2026-04-14", time = "21:00", content = "阅读")
        )
        val importedRecords = listOf(
            Record(date = "2026-04-14", time = "08:00", content = "晨跑"),
            Record(date = "2026-04-15", time = "09:00", content = "散步")
        )
        val localDiaries = listOf(Diary(date = "2026-04-14", aiDiary = "本地", aiInsight = "本地"))
        val importedDiaries = listOf(
            Diary(date = "2026-04-14", aiDiary = "导入", aiInsight = "导入"),
            Diary(date = "2026-04-15", aiDiary = "新增", aiInsight = "新增")
        )

        val preview = buildImportPreview(
            localRecords = localRecords,
            importedRecords = importedRecords,
            localDiaries = localDiaries,
            importedDiaries = importedDiaries,
            importedSettings = UserSettings(nickname = "贝")
        )

        assertEquals(1, preview.recordConflicts)
        assertEquals(1, preview.recordAdds)
        assertEquals(1, preview.diaryConflicts)
        assertEquals(1, preview.diaryAdds)
        assertTrue(preview.hasSettings)
    }

    @Test
    fun planRecordImport_merge_skipsConflicts() {
        val localRecords = listOf(Record(id = 1, date = "2026-04-14", time = "08:00", content = "晨跑"))
        val importedRecords = listOf(
            Record(id = 8, date = "2026-04-14", time = "08:00", content = "晨跑"),
            Record(id = 9, date = "2026-04-15", time = "09:00", content = "散步")
        )

        val plan = planRecordImport(localRecords, importedRecords, ImportConflictStrategy.Merge)

        assertEquals(0, plan.deleteKeys.size)
        assertEquals(1, plan.recordsToInsert.size)
        assertEquals("2026-04-15|09:00|散步", plan.recordsToInsert.single().fingerprint())
    }

    @Test
    fun planRecordImport_replace_replacesConflictsOnly() {
        val localRecords = listOf(
            Record(id = 1, date = "2026-04-14", time = "08:00", content = "晨跑"),
            Record(id = 2, date = "2026-04-14", time = "21:00", content = "阅读")
        )
        val importedRecords = listOf(
            Record(id = 8, date = "2026-04-14", time = "08:00", content = "晨跑"),
            Record(id = 9, date = "2026-04-15", time = "09:00", content = "散步")
        )

        val plan = planRecordImport(localRecords, importedRecords, ImportConflictStrategy.ReplaceConflicts)

        assertEquals(setOf("2026-04-14|08:00|晨跑"), plan.deleteKeys)
        assertEquals(2, plan.recordsToInsert.size)
    }

    @Test
    fun planRecordImport_replaceConflicts_prefersImportedRecordPhotoPath() {
        val localRecords = listOf(
            Record(date = "2026-04-15", time = "09:00", content = "散步", photoPath = "/local/old.jpg")
        )
        val importedRecords = listOf(
            Record(date = "2026-04-15", time = "09:00", content = "散步", photoPath = "assets/journals/2026-04-15/images/001.jpg")
        )

        val plan = planRecordImport(localRecords, importedRecords, ImportConflictStrategy.ReplaceConflicts)

        assertEquals("assets/journals/2026-04-15/images/001.jpg", plan.recordsToInsert.single().photoPath)
    }

    @Test
    fun exportedRecords_mapsPhotoPathIntoPerDateJournalDirectory() {
        val tempDir = createTempDirectory(prefix = "backup-photo-exported").toFile()
        try {
            val photoFile = File(tempDir, "p1.png").apply { writeBytes(byteArrayOf(1, 2, 3)) }

            val exported = exportedRecords(
                listOf(
                    Record(
                        id = 7L,
                        date = "2026-04-15",
                        time = "09:20",
                        content = "晨间散步",
                        photoPath = photoFile.absolutePath
                    )
                )
            )

            assertEquals("assets/journals/2026-04-15/images/001.jpg", exported.single().photoPath)
            assertEquals(listOf("assets/journals/2026-04-15/images/001.jpg"), exportRecordPhotoEntries(listOf(
                Record(
                    id = 7L,
                    date = "2026-04-15",
                    time = "09:20",
                    content = "晨间散步",
                    photoPath = photoFile.absolutePath
                )
            )).map { it.path })
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun exportedRecords_numbersSameDatePhotosSequentially() {
        val tempDir = createTempDirectory(prefix = "backup-photo-exported").toFile()
        try {
            val firstPhoto = File(tempDir, "p1.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
            val secondPhoto = File(tempDir, "p2.jpg").apply { writeBytes(byteArrayOf(4, 5, 6)) }

            val exported = exportedRecords(
                listOf(
                    Record(
                        date = "2026-04-15",
                        time = "09:20",
                        content = "晨间散步",
                        photoPath = firstPhoto.absolutePath
                    ),
                    Record(
                        date = "2026-04-15",
                        time = "12:20",
                        content = "午间散步",
                        photoPath = secondPhoto.absolutePath
                    )
                )
            )

            assertEquals(
                listOf(
                    "assets/journals/2026-04-15/images/001.jpg",
                    "assets/journals/2026-04-15/images/002.jpg"
                ),
                exported.map { it.photoPath }
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun exportedRecords_resetsPhotoNumberingPerDate() {
        val tempDir = createTempDirectory(prefix = "backup-photo-exported").toFile()
        try {
            val firstPhoto = File(tempDir, "p1.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
            val secondPhoto = File(tempDir, "p2.jpg").apply { writeBytes(byteArrayOf(4, 5, 6)) }

            val exported = exportedRecords(
                listOf(
                    Record(
                        date = "2026-04-15",
                        time = "09:20",
                        content = "晨间散步",
                        photoPath = firstPhoto.absolutePath
                    ),
                    Record(
                        date = "2026-04-16",
                        time = "09:20",
                        content = "次日散步",
                        photoPath = secondPhoto.absolutePath
                    )
                )
            )

            assertEquals(
                listOf(
                    "assets/journals/2026-04-15/images/001.jpg",
                    "assets/journals/2026-04-16/images/001.jpg"
                ),
                exported.map { it.photoPath }
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun exportedRecords_skipsMissingPhotosSoJsonNeverPointsAtMissingZipEntries() {
        val tempDir = createTempDirectory(prefix = "backup-photo-consistency").toFile()
        try {
            val missingPhoto = File(tempDir, "missing.jpg").absolutePath
            val existingPhoto = File(tempDir, "p1.jpg").apply {
                writeBytes(byteArrayOf(7, 8, 9))
            }.absolutePath

            val records = listOf(
                Record(
                    date = "2026-04-15",
                    time = "09:20",
                    content = "缺失照片",
                    photoPath = missingPhoto
                ),
                Record(
                    date = "2026-04-15",
                    time = "10:20",
                    content = "存在照片",
                    photoPath = existingPhoto
                )
            )

            val exported = exportedRecords(records)
            val entries = exportRecordPhotoEntries(records)

            assertEquals("", exported.first().photoPath)
            assertEquals("assets/journals/2026-04-15/images/001.jpg", exported.last().photoPath)
            assertEquals(listOf("assets/journals/2026-04-15/images/001.jpg"), entries.map { it.path })
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun exportRecordPhotoEntries_placesBytesUnderDateScopedJournalDirectory() {
        val tempDir = createTempDirectory(prefix = "backup-photo-export").toFile()
        try {
            val photoFile = File(tempDir, "p1.jpg").apply {
                writeBytes(byteArrayOf(1, 2, 3))
            }

            val entries = exportRecordPhotoEntries(
                listOf(
                    Record(date = "2026-04-15", time = "09:20", content = "晨间散步", photoPath = photoFile.absolutePath),
                    Record(date = "2026-04-15", time = "10:20", content = "午间散步", photoPath = File(tempDir, "missing.jpg").absolutePath),
                    Record(date = "2026-04-15", time = "11:20", content = "晚间散步")
                )
            )

            assertEquals(listOf("assets/journals/2026-04-15/images/001.jpg"), entries.map { it.path })
            assertTrue(entries.single().bytes.contentEquals(byteArrayOf(1, 2, 3)))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun exportRecordPhotoEntries_numbersSameDatePhotosSequentially() {
        val tempDir = createTempDirectory(prefix = "backup-photo-export").toFile()
        try {
            val firstPhoto = File(tempDir, "p1.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
            val secondPhoto = File(tempDir, "p2.jpg").apply { writeBytes(byteArrayOf(4, 5, 6)) }

            val entries = exportRecordPhotoEntries(
                listOf(
                    Record(date = "2026-04-15", time = "09:20", content = "晨间散步", photoPath = firstPhoto.absolutePath),
                    Record(date = "2026-04-15", time = "10:20", content = "午间散步", photoPath = secondPhoto.absolutePath)
                )
            )

            assertEquals(
                listOf(
                    "assets/journals/2026-04-15/images/001.jpg",
                    "assets/journals/2026-04-15/images/002.jpg"
                ),
                entries.map { it.path }
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun restoreImportedRecordPhoto_readsDateScopedJournalAssetPath() {
        val filesDir = createTempDirectory(prefix = "backup-photo-import").toFile()
        try {
            val restoredPath = restoreImportedRecordPhoto(
                filesDir = filesDir,
                pathInZip = "assets/journals/2026-04-15/images/001.jpg",
                entries = mapOf("assets/journals/2026-04-15/images/001.jpg" to byteArrayOf(4, 5, 6))
            )

            assertNotNull(restoredPath)
            val localPath = restoredPath ?: error("expected restored photo path")
            assertTrue(localPath.startsWith(filesDir.absolutePath))
            assertTrue(localPath.startsWith(File(filesDir, "record_photos").absolutePath))
            assertTrue(File(localPath).readBytes().contentEquals(byteArrayOf(4, 5, 6)))
        } finally {
            filesDir.deleteRecursively()
        }
    }

    @Test
    fun restoreImportedRecordPhoto_returnsNullForMissingZipAsset() {
        val filesDir = createTempDirectory(prefix = "backup-photo-missing").toFile()
        try {
            val restoredPath = restoreImportedRecordPhoto(
                filesDir = filesDir,
                pathInZip = "assets/journals/2026-04-15/images/001.jpg",
                entries = emptyMap()
            )

            assertNull(restoredPath)
        } finally {
            filesDir.deleteRecursively()
        }
    }

    @Test
    fun mergeSettings_prefersLocalValuesWhenRequested() {
        val local = UserSettings(nickname = "本地", apiEndpoint = "https://local", modelName = "local-model")
        val imported = UserSettings(nickname = "导入", apiEndpoint = "https://import", modelName = "import-model")

        val merged = mergeImportedSettings(local, imported, ImportConflictStrategy.Merge)

        assertEquals("本地", merged.nickname)
        assertEquals("https://local", merged.apiEndpoint)
        assertEquals("local-model", merged.modelName)
    }

    @Test
    fun mergeSettings_replace_usesImportedValues() {
        val local = UserSettings(nickname = "本地")
        val imported = UserSettings(nickname = "导入", s3Bucket = "backup")

        val merged = mergeImportedSettings(local, imported, ImportConflictStrategy.ReplaceConflicts)

        assertEquals("导入", merged.nickname)
        assertEquals("backup", merged.s3Bucket)
        assertFalse(merged.s3AutoSync)
    }
}
