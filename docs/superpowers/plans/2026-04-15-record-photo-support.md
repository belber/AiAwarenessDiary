# Record Photo Support Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add one-photo-per-record support with local compressed storage, album/camera input, thumbnail display, fullscreen preview, backup import/export, and S3 backup compatibility.

**Architecture:** Extend `Record` with a single `photoPath` field and store only app-private JPEG files. Route album and camera inputs through a dedicated `RecordPhotoStorage`, then reuse the existing backup zip pipeline so record photos import/export and sync with current S3 flows.

**Tech Stack:** Kotlin, Room, Jetpack Compose, Activity Result APIs, FileProvider, Coil, Gson/Zip backup pipeline, JUnit4, Mockito

---

## File Structure

- Modify: `app/src/main/java/com/aiawareness/diary/data/model/Record.kt`
  Add `photoPath` to the app model.
- Modify: `app/src/main/java/com/aiawareness/diary/data/local/RecordEntity.kt`
  Persist `photoPath` in Room and map it to/from `Record`.
- Modify: `app/src/main/java/com/aiawareness/diary/data/local/RecordDao.kt`
  Keep DAO behavior aligned with the widened entity.
- Modify: `app/src/main/java/com/aiawareness/diary/data/local/DiaryDatabase.kt`
  Bump schema version and add migration for `photoPath`.
- Modify: `app/src/main/java/com/aiawareness/diary/data/repository/RecordRepository.kt`
  Add deletion helper that can return the record being removed so callers can clean up attached photos.
- Create: `app/src/main/java/com/aiawareness/diary/data/local/RecordPhotoStorage.kt`
  Import, compress, save, replace, and delete record photos in app-private storage.
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt`
  Manage pending selected photo, save/update/delete flows, and photo cleanup.
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
  Add pick/capture UI, pending photo preview, per-record thumbnail, and fullscreen preview.
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/CalendarScreen.kt`
  Show thumbnails, fullscreen preview, and photo replacement inside edit flow.
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/BackupModels.kt`
  Carry relative photo names in backup payloads and conflict helpers.
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/LocalBackupService.kt`
  Export/import record photos inside the existing zip structure.
- Modify: `app/src/main/AndroidManifest.xml`
  Register `FileProvider`.
- Create: `app/src/main/res/xml/file_paths.xml`
  Define camera temp/shared app-private directories for `FileProvider`.
- Test: `app/src/test/java/com/aiawareness/diary/data/repository/RecordRepositoryTest.kt`
  Model/entity/repository coverage for `photoPath`.
- Test: `app/src/test/java/com/aiawareness/diary/data/backup/BackupMergePlannerTest.kt`
  Import conflict semantics when records differ by photo path.
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt`
  Save validation and cleanup hooks.
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`
  Thumbnail-related labels/helpers if new helpers are introduced.
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/ReviewScreenModelsTest.kt`
  Review-level copy/helpers if added.

### Task 1: Add Record Photo Data Model And Migration

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/data/model/Record.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/data/local/RecordEntity.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/data/local/DiaryDatabase.kt`
- Test: `app/src/test/java/com/aiawareness/diary/data/repository/RecordRepositoryTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun getRecordsByDate_mapsPhotoPathFromEntity() = runTest {
    whenever(recordDao.getRecordsByDate("2026-04-15")).thenReturn(
        listOf(
            RecordEntity(
                id = 7L,
                date = "2026-04-15",
                time = "09:20",
                content = "晨间散步",
                photoPath = "/files/record_photos/p1.jpg",
                createdAt = 100L,
                updatedAt = 120L
            )
        )
    )

    val records = repository.getRecordsByDate("2026-04-15")

    assertEquals("/files/record_photos/p1.jpg", records.single().photoPath)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.repository.RecordRepositoryTest`

Expected: FAIL with a compile error or assertion failure because `photoPath` does not exist yet on `Record` / `RecordEntity`.

- [ ] **Step 3: Write minimal implementation**

```kotlin
data class Record(
    val id: Long = 0,
    val date: String,
    val time: String,
    val content: String,
    val photoPath: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

```kotlin
@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val time: String,
    val content: String,
    val photoPath: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toModel(): Record = Record(
        id = id,
        date = date,
        time = time,
        content = content,
        photoPath = photoPath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: Record): RecordEntity = RecordEntity(
            id = model.id,
            date = model.date,
            time = model.time,
            content = model.content,
            photoPath = model.photoPath,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }
}
```

```kotlin
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
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.repository.RecordRepositoryTest`

Expected: PASS with the new `photoPath` mapping covered.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/data/model/Record.kt app/src/main/java/com/aiawareness/diary/data/local/RecordEntity.kt app/src/main/java/com/aiawareness/diary/data/local/DiaryDatabase.kt app/src/test/java/com/aiawareness/diary/data/repository/RecordRepositoryTest.kt
git commit -m "feat: add record photo path model"
```

### Task 2: Add Local Record Photo Storage

**Files:**
- Create: `app/src/main/java/com/aiawareness/diary/data/local/RecordPhotoStorage.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/xml/file_paths.xml`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun saveRecord_withPendingPhoto_savesImportedPath() = runTest {
    val photoStorage: RecordPhotoStorage = mock()
    whenever(photoStorage.persistImportedPhoto(any())).thenReturn("/files/record_photos/p1.jpg")
    val viewModel = createViewModel(photoStorage = photoStorage)

    viewModel.setPendingPhoto(mock())
    advanceUntilIdle()
    viewModel.saveRecord("记录一条新的觉察")
    advanceUntilIdle()

    verify(recordRepository).insertRecord(
        check { record ->
            assertEquals("/files/record_photos/p1.jpg", record.photoPath)
        }
    )
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest`

Expected: FAIL because `RecordPhotoStorage` and pending-photo APIs do not exist.

- [ ] **Step 3: Write minimal implementation**

```kotlin
@Singleton
class RecordPhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun persistImportedPhoto(uri: Uri): String = persistDecodedBitmap(
        context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            ?: error("无法读取图片")
    )

    fun persistCapturedPhoto(tempFile: File): String = tempFile.inputStream().use { stream ->
        persistDecodedBitmap(BitmapFactory.decodeStream(stream) ?: error("无法读取拍照图片"))
    }

    fun createCameraTempFile(): File =
        File(context.cacheDir, "record_photo_capture").apply { mkdirs() }
            .resolve("capture_${System.currentTimeMillis()}.jpg")

    fun deletePhoto(path: String) {
        if (path.isBlank()) return
        File(path).takeIf { it.exists() }?.delete()
    }

    private fun persistDecodedBitmap(bitmap: Bitmap): String {
        val photoDir = File(context.filesDir, "record_photos").apply { mkdirs() }
        val target = photoDir.resolve("photo_${System.currentTimeMillis()}.jpg")
        FileOutputStream(target).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)
        }
        return target.absolutePath
    }
}
```

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

```xml
<paths>
    <cache-path name="record_photo_capture" path="record_photo_capture/" />
</paths>
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest`

Expected: PASS with pending-photo persistence wired in at the ViewModel seam.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/data/local/RecordPhotoStorage.kt app/src/main/AndroidManifest.xml app/src/main/res/xml/file_paths.xml app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt
git commit -m "feat: add record photo storage"
```

### Task 3: Wire Save/Edit/Delete Flows Through MainViewModel

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/data/repository/RecordRepository.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun updateRecord_withReplacementPhoto_deletesOldPhotoAfterPersistingNewOne() = runTest {
    val photoStorage: RecordPhotoStorage = mock()
    whenever(photoStorage.persistImportedPhoto(any())).thenReturn("/files/record_photos/new.jpg")
    val viewModel = createViewModel(photoStorage = photoStorage)
    val original = Record(
        id = 9L,
        date = "2026-04-15",
        time = "20:00",
        content = "旧内容",
        photoPath = "/files/record_photos/old.jpg"
    )

    viewModel.replaceRecordPhoto(original, mock())
    advanceUntilIdle()

    verify(recordRepository).updateRecord(check { updated ->
        assertEquals("/files/record_photos/new.jpg", updated.photoPath)
    })
    verify(photoStorage).deletePhoto("/files/record_photos/old.jpg")
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest`

Expected: FAIL because photo replacement and cleanup logic do not exist.

- [ ] **Step 3: Write minimal implementation**

```kotlin
class MainViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository,
    private val apiServiceFactory: OpenAIApiServiceFactory,
    private val recordPhotoStorage: RecordPhotoStorage
) : ViewModel() {
    private var pendingPhotoUri: Uri? = null

    fun setPendingPhoto(uri: Uri?) {
        pendingPhotoUri = uri
    }

    fun saveRecord(content: String) {
        viewModelScope.launch {
            if (content.isBlank()) {
                _uiState.update { it.copy(error = "请先输入文字内容") }
                return@launch
            }
            val photoPath = pendingPhotoUri?.let(recordPhotoStorage::persistImportedPhoto).orEmpty()
            recordRepository.insertRecord(
                Record(
                    date = DateUtil.getCurrentDate(),
                    time = DateUtil.getCurrentTime(),
                    content = content,
                    photoPath = photoPath
                )
            )
            pendingPhotoUri = null
            loadRecordsForDate(DateUtil.getCurrentDate())
            loadDatesWithRecords()
        }
    }

    fun replaceRecordPhoto(record: Record, uri: Uri) {
        viewModelScope.launch {
            val newPath = recordPhotoStorage.persistImportedPhoto(uri)
            recordRepository.updateRecord(record.copy(photoPath = newPath, updatedAt = System.currentTimeMillis()))
            recordPhotoStorage.deletePhoto(record.photoPath)
            loadRecordsForDate(record.date)
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            val record = recordRepository.getRecordById(id)
            recordRepository.deleteRecord(id)
            record?.photoPath?.let(recordPhotoStorage::deletePhoto)
            loadRecordsForDate(_uiState.value.currentDate)
            loadDatesWithRecords()
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest`

Expected: PASS with pending-photo save, replace, validation, and delete cleanup covered.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/data/repository/RecordRepository.kt app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt
git commit -m "feat: wire record photo flows through viewmodel"
```

### Task 4: Add Backup Import/Export Support For Record Photos

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/BackupModels.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/data/backup/LocalBackupService.kt`
- Test: `app/src/test/java/com/aiawareness/diary/data/backup/BackupMergePlannerTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun replaceConflicts_prefersImportedRecordPhotoPath() {
    val local = listOf(
        Record(date = "2026-04-15", time = "09:00", content = "散步", photoPath = "/local/old.jpg")
    )
    val imported = listOf(
        Record(date = "2026-04-15", time = "09:00", content = "散步", photoPath = "records/photos/new.jpg")
    )

    val plan = planRecordImport(local, imported, ImportConflictStrategy.ReplaceConflicts)

    assertEquals("records/photos/new.jpg", plan.recordsToInsert.single().photoPath)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.BackupMergePlannerTest`

Expected: FAIL because the backup model code does not yet account for record photo paths inside the zip payload.

- [ ] **Step 3: Write minimal implementation**

```kotlin
private fun exportRecordPhotoEntries(zip: ZipOutputStream, records: List<Record>) {
    records.forEach { record ->
        if (record.photoPath.isBlank()) return@forEach
        val file = File(record.photoPath)
        if (!file.exists()) return@forEach
        zip.putNextEntry(ZipEntry("records/photos/${file.name}"))
        zip.write(file.readBytes())
        zip.closeEntry()
    }
}
```

```kotlin
private fun exportedRecords(records: List<Record>): List<Record> =
    records.map { record ->
        if (record.photoPath.isBlank()) record
        else record.copy(photoPath = "records/photos/${File(record.photoPath).name}")
    }
```

```kotlin
private fun restoreImportedRecordPhoto(pathInZip: String, entries: Map<String, ByteArray>): String? {
    val bytes = entries[pathInZip] ?: return null
    val targetDir = File(context.filesDir, "record_photos").apply { mkdirs() }
    val target = targetDir.resolve("import_${System.currentTimeMillis()}_${pathInZip.substringAfterLast('/')}")
    target.writeBytes(bytes)
    return target.absolutePath
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.backup.BackupMergePlannerTest --tests com.aiawareness.diary.data.backup.BackupMarkdownExporterTest`

Expected: PASS with record-photo relative-path handling and import conflict semantics preserved.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/data/backup/BackupModels.kt app/src/main/java/com/aiawareness/diary/data/backup/LocalBackupService.kt app/src/test/java/com/aiawareness/diary/data/backup/BackupMergePlannerTest.kt
git commit -m "feat: include record photos in backup sync"
```

### Task 5: Add Homepage Photo Input, Thumbnail Rendering, And Preview

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/components/CardComponents.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun homeInputPlaceholder_stillRequiresTextWhenPhotoSelected() {
    assertEquals("请先输入文字内容", homePhotoWithoutTextError())
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest`

Expected: FAIL because no photo-validation helper/copy exists yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
internal fun homePhotoWithoutTextError(): String = "请先输入文字内容"
```

```kotlin
val albumLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) viewModel.setPendingPhoto(uri)
}
val captureTempFile = remember { mutableStateOf<File?>(null) }
val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
    val temp = captureTempFile.value ?: return@rememberLauncherForActivityResult
    if (success) viewModel.setPendingCapturedPhoto(temp)
}
```

```kotlin
if (pendingPhotoModel != null) {
    SubcomposeAsyncImage(
        model = pendingPhotoModel,
        contentDescription = "待保存图片",
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(14.dp)),
        contentScale = ContentScale.Crop
    )
}
```

```kotlin
record.photoPath.takeIf { it.isNotBlank() }?.let { photoPath ->
    SubcomposeAsyncImage(
        model = photoPath,
        contentDescription = "记录图片",
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { previewPath = photoPath },
        contentScale = ContentScale.Crop
    )
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest`

Expected: PASS with text-required validation covered and homepage photo state compiling cleanly.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt app/src/main/java/com/aiawareness/diary/ui/components/CardComponents.kt app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt
git commit -m "feat: add homepage record photo UI"
```

### Task 6: Add Review Photo Rendering, Photo Replacement, And Fullscreen Preview

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/CalendarScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/ReviewScreenModels.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/ReviewScreenModelsTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun reviewPhotoPreviewCloseLabel_matchesCopy() {
    assertEquals("关闭图片", reviewPhotoPreviewCloseLabel())
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.ReviewScreenModelsTest`

Expected: FAIL because the preview helper/copy does not exist yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
fun reviewPhotoPreviewCloseLabel(): String = "关闭图片"
```

```kotlin
record.photoPath.takeIf { it.isNotBlank() }?.let { photoPath ->
    SubcomposeAsyncImage(
        model = photoPath,
        contentDescription = "记录图片",
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { previewPath = photoPath },
        contentScale = ContentScale.Crop
    )
}
```

```kotlin
AlertDialog(
    onDismissRequest = { previewPath = null },
    confirmButton = {
        TextButton(onClick = { previewPath = null }) {
            Text(reviewPhotoPreviewCloseLabel())
        }
    },
    text = {
        SubcomposeAsyncImage(
            model = previewPath,
            contentDescription = "大图预览",
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Fit
        )
    }
)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.ReviewScreenModelsTest`

Expected: PASS with review preview helpers covered and review screen compiling with image support.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/screens/CalendarScreen.kt app/src/main/java/com/aiawareness/diary/ui/screens/ReviewScreenModels.kt app/src/test/java/com/aiawareness/diary/ui/screens/ReviewScreenModelsTest.kt
git commit -m "feat: add review record photo preview"
```

### Task 7: Final Verification

**Files:**
- Modify: none
- Test: existing targeted suites

- [ ] **Step 1: Run focused unit tests**

Run:

```bash
./gradlew testDebugUnitTest \
  --tests com.aiawareness.diary.data.repository.RecordRepositoryTest \
  --tests com.aiawareness.diary.data.backup.BackupMergePlannerTest \
  --tests com.aiawareness.diary.ui.screens.MainViewModelTest \
  --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest \
  --tests com.aiawareness.diary.ui.screens.ReviewScreenModelsTest
```

Expected: PASS across data, backup, and UI model coverage.

- [ ] **Step 2: Run a broader regression suite**

Run:

```bash
./gradlew testDebugUnitTest \
  --tests com.aiawareness.diary.data.backup.BackupMarkdownExporterTest \
  --tests com.aiawareness.diary.data.repository.DiaryRepositoryTest \
  --tests com.aiawareness.diary.ui.screens.SettingsViewModelTest \
  --tests com.aiawareness.diary.util.PromptBuilderTest
```

Expected: PASS, proving backup, AI generation, and settings flows still behave in no-photo scenarios.

- [ ] **Step 3: Build debug APK**

Run: `./gradlew assembleDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app
git commit -m "feat: support one photo per diary record"
```

## Self-Review

- Spec coverage:
  Data model, local storage, album/camera flow, homepage UI, review UI, fullscreen preview, backup import/export, S3 zip compatibility, conflict semantics, and text-required validation all map to Tasks 1-7.
- Placeholder scan:
  Removed vague “handle later” language; every task names exact files, commands, and minimal code.
- Type consistency:
  Plan uses one field name, `photoPath`, and one storage type, `RecordPhotoStorage`, throughout. ViewModel methods referenced later (`setPendingPhoto`, `replaceRecordPhoto`) are introduced in Task 3 before later UI tasks depend on them.
