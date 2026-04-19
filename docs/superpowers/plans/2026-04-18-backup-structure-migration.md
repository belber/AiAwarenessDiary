# Backup Structure Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild backup export/import so records, AI diaries, images, avatar, and settings round-trip through a single coherent zip structure without duplicate or ambiguous image paths.

**Architecture:** Keep `records.json` / `diaries.json` / `settings.json` as the machine-readable import sources, and move all exported assets under a single `assets/` tree grouped by journal date. Replace the current global record photo directory in zip with per-day image folders, then restore imported images and avatar back into app-private absolute paths during import.

**Tech Stack:** Kotlin, Room, DataStore Preferences, Gson, ZipInputStream/ZipOutputStream, JUnit4, Gradle unit tests

---

## File Structure

- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/BackupModels.kt`
  Purpose: Define zip path helpers and exported photo path mapping.
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/LocalBackupService.kt`
  Purpose: Export new zip layout, restore imported images/avatar from new relative paths.
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/BackupMarkdownExporter.kt`
  Purpose: Keep markdown metadata aligned with the new asset layout.
- Test: `app/src/test/java/com/aiawareness/diary/data/backup/BackupMergePlannerTest.kt`
  Purpose: Lock path mapping, image export, and restore behavior.
- Create or modify: `app/src/test/java/com/aiawareness/diary/data/backup/LocalBackupServiceTest.kt`
  Purpose: Verify zip entry layout and import/export round-trip behavior at service level.

### Task 1: Lock New Zip Path Rules in Unit Tests

**Files:**
- Modify: `app/src/test/java/com/aiawareness/diary/data/backup/BackupMergePlannerTest.kt`

- [ ] **Step 1: Write the failing test for exported record photo paths**

```kotlin
@Test
fun exportedRecords_mapsPhotoPathIntoPerDateJournalDirectory() {
    val exported = exportedRecords(
        listOf(
            Record(
                id = 7L,
                date = "2026-04-15",
                time = "09:20",
                content = "晨间散步",
                photoPath = "/data/user/0/com.app/files/record_photos/p1.jpg"
            )
        )
    )

    assertEquals(
        "assets/journals/2026-04-15/images/001.jpg",
        exported.single().photoPath
    )
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.BackupMergePlannerTest.exportedRecords_mapsPhotoPathIntoPerDateJournalDirectory
```
Expected: FAIL because current code still returns `records/photos/p1.jpg`.

- [ ] **Step 3: Write the failing test for per-date asset entries**

```kotlin
@Test
fun exportRecordPhotoEntries_placesBytesUnderDateScopedJournalDirectory() {
    val tempDir = createTempDirectory(prefix = "backup-photo-export").toFile()
    try {
        val photoFile = File(tempDir, "p1.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        val entries = exportRecordPhotoEntries(
            listOf(
                Record(
                    date = "2026-04-15",
                    time = "09:20",
                    content = "晨间散步",
                    photoPath = photoFile.absolutePath
                )
            )
        )

        assertEquals(listOf("assets/journals/2026-04-15/images/001.jpg"), entries.map { it.path })
    } finally {
        tempDir.deleteRecursively()
    }
}
```

- [ ] **Step 4: Run test to verify it fails**

Run:
```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.BackupMergePlannerTest.exportRecordPhotoEntries_placesBytesUnderDateScopedJournalDirectory
```
Expected: FAIL because current code still writes `records/photos/...`.

- [ ] **Step 5: Write the failing test for restore from date-scoped asset path**

```kotlin
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
        assertTrue(File(restoredPath!!).readBytes().contentEquals(byteArrayOf(4, 5, 6)))
    } finally {
        filesDir.deleteRecursively()
    }
}
```

- [ ] **Step 6: Run test to verify it fails or compiles red as needed**

Run:
```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.BackupMergePlannerTest.restoreImportedRecordPhoto_readsDateScopedJournalAssetPath
```
Expected: FAIL until path helpers are updated consistently.

- [ ] **Step 7: Commit**

```bash
git add app/src/test/java/com/aiawareness/diary/data/backup/BackupMergePlannerTest.kt
git commit -m "test: lock backup asset path migration"
```

### Task 2: Implement Date-Scoped Asset Path Mapping

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/BackupModels.kt`
- Test: `app/src/test/java/com/aiawareness/diary/data/backup/BackupMergePlannerTest.kt`

- [ ] **Step 1: Add helper functions for journal asset paths**

```kotlin
private const val ASSET_ROOT_DIR = "assets"
private const val JOURNAL_ASSET_DIR = "$ASSET_ROOT_DIR/journals"

private fun exportedPhotoPath(date: String, index: Int, originalPath: String): String {
    val extension = File(originalPath).extension.ifBlank { "jpg" }
    return "$JOURNAL_ASSET_DIR/$date/images/${(index + 1).toString().padStart(3, '0')}.$extension"
}
```

- [ ] **Step 2: Update `exportedRecords` to rewrite `photoPath` using per-date image paths**

```kotlin
fun exportedRecords(records: List<Record>): List<Record> {
    val photoIndexByDate = mutableMapOf<String, Int>()
    return records.map { record ->
        if (record.photoPath.isBlank()) {
            record.copy(photoPath = "")
        } else {
            val nextIndex = photoIndexByDate.getOrDefault(record.date, 0)
            photoIndexByDate[record.date] = nextIndex + 1
            record.copy(photoPath = exportedPhotoPath(record.date, nextIndex, record.photoPath))
        }
    }
}
```

- [ ] **Step 3: Update `exportRecordPhotoEntries` to emit the same paths as `exportedRecords`**

```kotlin
fun exportRecordPhotoEntries(records: List<Record>): List<BackupPhotoAsset> {
    val photoIndexByDate = mutableMapOf<String, Int>()
    return records.mapNotNull { record ->
        if (record.photoPath.isBlank()) return@mapNotNull null
        val file = File(record.photoPath)
        if (!file.exists() || !file.isFile) return@mapNotNull null
        val nextIndex = photoIndexByDate.getOrDefault(record.date, 0)
        photoIndexByDate[record.date] = nextIndex + 1
        BackupPhotoAsset(
            path = exportedPhotoPath(record.date, nextIndex, record.photoPath),
            bytes = file.readBytes()
        )
    }
}
```

- [ ] **Step 4: Run the backup path tests**

Run:
```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.BackupMergePlannerTest
```
Expected: PASS for the updated path mapping tests.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/data/backup/BackupModels.kt app/src/test/java/com/aiawareness/diary/data/backup/BackupMergePlannerTest.kt
git commit -m "feat: group backup record photos by journal date"
```

### Task 3: Rebuild Zip Export Layout Around `assets/`

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/LocalBackupService.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/BackupMarkdownExporter.kt`
- Create or modify: `app/src/test/java/com/aiawareness/diary/data/backup/LocalBackupServiceTest.kt`

- [ ] **Step 1: Write the failing export layout test**

```kotlin
@Test
fun exportToZip_writesAssetsUnderDateScopedJournalDirectories() = runTest {
    // arrange repositories/settings with one record, one diary, one avatar
    // export to a temp zip
    // read zip entries
    assertTrue(entries.contains("manifest.json"))
    assertTrue(entries.contains("records.json"))
    assertTrue(entries.contains("diaries.json"))
    assertTrue(entries.contains("settings.json"))
    assertTrue(entries.contains("assets/journals/2026-04-15/entry.md"))
    assertTrue(entries.contains("assets/journals/2026-04-15/images/001.jpg"))
    assertTrue(entries.contains("assets/avatar/avatar.jpg"))
    assertFalse(entries.any { it.startsWith("records/photos/") })
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.LocalBackupServiceTest.exportToZip_writesAssetsUnderDateScopedJournalDirectories
```
Expected: FAIL because export still writes `records/photos/...` and old avatar path rules.

- [ ] **Step 3: Update `exportToZip` to write only the new asset structure**

```kotlin
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
    records = bundle.records,
    diaries = diaries,
    exportedAt = bundle.manifest.exportedAt
)

if (bundle.avatarBytes != null) {
    zip.putNextEntry(ZipEntry("assets/avatar/avatar.jpg"))
    zip.write(bundle.avatarBytes)
    zip.closeEntry()
}
```

- [ ] **Step 4: Update markdown export metadata to match actual folder layout**

```kotlin
images_dir: images/
```

Keep this value, but ensure `entry.md` is only emitted under:
```text
assets/journals/<date>/entry.md
```
and remove empty legacy zip directories unrelated to actual assets.

- [ ] **Step 5: Run export layout tests**

Run:
```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.LocalBackupServiceTest --tests com.aiawareness.diary.data.backup.BackupMergePlannerTest
```
Expected: PASS with no `records/photos/` entries remaining.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/data/backup/LocalBackupService.kt app/src/main/java/com/aiawareness/diary/data/backup/BackupMarkdownExporter.kt app/src/test/java/com/aiawareness/diary/data/backup/LocalBackupServiceTest.kt app/src/test/java/com/aiawareness/diary/data/backup/BackupMergePlannerTest.kt
git commit -m "feat: export backups under unified assets structure"
```

### Task 4: Rebuild Import to Restore From New Relative Paths

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/LocalBackupService.kt`
- Test: `app/src/test/java/com/aiawareness/diary/data/backup/LocalBackupServiceTest.kt`

- [ ] **Step 1: Write the failing round-trip import test**

```kotlin
@Test
fun importFromZip_restoresRecordPhotosAndAvatarFromAssetsPaths() = runTest {
    // arrange zip entries with:
    // records.json -> photoPath "assets/journals/2026-04-15/images/001.jpg"
    // settings.json -> avatarPath "assets/avatar/avatar.jpg"
    // binary entries at those exact paths
    // import zip using ReplaceConflicts
    // assert imported record photoPath is absolute local path
    // assert imported settings avatarPath is absolute local path
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.LocalBackupServiceTest.importFromZip_restoresRecordPhotosAndAvatarFromAssetsPaths
```
Expected: FAIL until import reads the new relative paths.

- [ ] **Step 3: Update `readBundle` and avatar handling for new asset paths**

```kotlin
val avatarEntryName = settings?.avatarPath?.takeIf { it.isNotBlank() }

val records = parsedRecords.map { record ->
    if (record.photoPath.isBlank()) record else record.copy(
        photoPath = restoreImportedRecordPhoto(
            filesDir = context.filesDir,
            pathInZip = record.photoPath,
            entries = entries
        ).orEmpty()
    )
}
```

- [ ] **Step 4: Normalize exported avatar path in settings**

```kotlin
settings = settings.copy(avatarPath = if (avatarBytes != null) "assets/avatar/avatar.jpg" else "")
```

- [ ] **Step 5: Run the import tests**

Run:
```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.LocalBackupServiceTest
```
Expected: PASS for import restoration from `assets/...` paths.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/data/backup/LocalBackupService.kt app/src/test/java/com/aiawareness/diary/data/backup/LocalBackupServiceTest.kt
git commit -m "feat: restore backup assets from new zip paths"
```

### Task 5: End-to-End Verification Against the Spec

**Files:**
- Modify if needed: `app/src/test/java/com/aiawareness/diary/data/backup/LocalBackupServiceTest.kt`
- Reference: `docs/superpowers/specs/2026-04-18-backup-structure-design.md`

- [ ] **Step 1: Add a spec-oriented verification test**

```kotlin
@Test
fun exportToZip_producesSpecCompliantStructure() = runTest {
    // export sample data
    // assert:
    // - root json files exist
    // - per-date entry.md exists
    // - every exported record photoPath points to an existing zip entry
    // - avatarPath points to an existing zip entry when avatar exists
    // - no legacy records/photos entries exist
}
```

- [ ] **Step 2: Run the full backup test suite**

Run:
```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.BackupMergePlannerTest --tests com.aiawareness.diary.data.backup.LocalBackupServiceTest
```
Expected: PASS.

- [ ] **Step 3: Self-check the generated zip manually if a fixture test writes one to temp**

Check:
- root JSON files are present
- `assets/journals/<date>/entry.md` and `images/` match
- no duplicate image directories
- imported paths become local absolute paths again

- [ ] **Step 4: Commit**

```bash
git add app/src/test/java/com/aiawareness/diary/data/backup/LocalBackupServiceTest.kt docs/superpowers/specs/2026-04-18-backup-structure-design.md docs/superpowers/plans/2026-04-18-backup-structure-migration.md
git commit -m "test: verify backup structure round-trip"
```
