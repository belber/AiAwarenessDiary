# AI 自动生成与首页提示 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 AI 日记补齐每日后台自动生成、首页进入补最近 5 次生成，以及首页底部 AI 区域的会话级轻提示。

**Architecture:** 新增一个可复用的 AI 日记自动生成协调器，统一承载“按日期生成”“补最近 5 次”“后台当日自动生成”的业务逻辑。`WorkManager` 只负责到点调度当天任务，`MainViewModel` 只负责首页会话内的提示状态与进入首页后的补生成触发。

**Tech Stack:** Kotlin, Hilt, WorkManager, Room, DataStore, Compose, JUnit4, Mockito, Coroutines Test

---

### Task 1: 接入 WorkManager 依赖与注入入口

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/java/com/aiawareness/diary/di/WorkManagerModule.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Test: 无

- [ ] **Step 1: 添加 WorkManager 依赖**

```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.0")
implementation("androidx.hilt:hilt-work:1.1.0")
kapt("androidx.hilt:hilt-compiler:1.1.0")
```

- [ ] **Step 2: 注册 WorkerFactory 与启动器**

```kotlin
@HiltAndroidApp
class DiaryApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

- [ ] **Step 3: 在 Manifest 指向自定义 Application**

```xml
<application
    android:name=".DiaryApplication"
    ... />
```

- [ ] **Step 4: 编译验证**

Run: `./gradlew compileDebugKotlin`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/build.gradle.kts app/src/main/java/com/aiawareness/diary/di/WorkManagerModule.kt app/src/main/AndroidManifest.xml app/src/main/java/com/aiawareness/diary/DiaryApplication.kt
git commit -m "feat: wire workmanager for ai diary automation"
```

### Task 2: 提取自动生成协调器与调度器

**Files:**
- Create: `app/src/main/java/com/aiawareness/diary/domain/AiDiaryAutoGenerationCoordinator.kt`
- Create: `app/src/main/java/com/aiawareness/diary/domain/AiDiaryGenerationScheduler.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/di/RepositoryModule.kt`
- Test: `app/src/test/java/com/aiawareness/diary/domain/AiDiaryAutoGenerationCoordinatorTest.kt`

- [ ] **Step 1: 写协调器失败测试**

```kotlin
@Test
fun generateMissingRecentDiaries_generatesMostRecentFiveDates() = runTest {
    // 配置完整、最近 6 天有记录、都没 AI 日记
    // 断言只生成最近 5 天
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.domain.AiDiaryAutoGenerationCoordinatorTest`
Expected: FAIL with missing class or unresolved reference

- [ ] **Step 3: 写最小协调器实现**

```kotlin
class AiDiaryAutoGenerationCoordinator @Inject constructor(
    private val recordRepository: RecordRepository,
    private val diaryRepository: DiaryRepository,
    private val settingsRepository: SettingsRepository,
    private val apiServiceFactory: OpenAIApiServiceFactory
) {
    suspend fun generateTodayIfNeeded(): AutoGenerationOutcome { ... }
    suspend fun generateRecentMissingDiaries(limit: Int = 5): CatchUpGenerationOutcome { ... }
}
```

- [ ] **Step 4: 写调度器最小实现**

```kotlin
class AiDiaryGenerationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleDaily(hour: Int, minute: Int) { ... }
    fun cancelDaily() { ... }
}
```

- [ ] **Step 5: 运行协调器测试确认通过**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.domain.AiDiaryAutoGenerationCoordinatorTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/domain/AiDiaryAutoGenerationCoordinator.kt app/src/main/java/com/aiawareness/diary/domain/AiDiaryGenerationScheduler.kt app/src/main/java/com/aiawareness/diary/di/RepositoryModule.kt app/src/test/java/com/aiawareness/diary/domain/AiDiaryAutoGenerationCoordinatorTest.kt
git commit -m "feat: extract ai diary auto-generation coordinator"
```

### Task 3: 实现每日后台 Worker

**Files:**
- Create: `app/src/main/java/com/aiawareness/diary/work/AiDiaryAutoGenerationWorker.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/SettingsViewModel.kt`
- Test: `app/src/test/java/com/aiawareness/diary/work/AiDiaryAutoGenerationWorkerTest.kt`

- [ ] **Step 1: 写 Worker 失败测试**

```kotlin
@Test
fun doWork_withCompleteConfigAndRecords_generatesTodayDiary() = runTest {
    // 协调器返回成功
    // 断言 worker 返回 Result.success()
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.work.AiDiaryAutoGenerationWorkerTest`
Expected: FAIL with missing worker

- [ ] **Step 3: 写 Worker 最小实现**

```kotlin
@HiltWorker
class AiDiaryAutoGenerationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val coordinator: AiDiaryAutoGenerationCoordinator
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        coordinator.generateTodayIfNeeded()
        return Result.success()
    }
}
```

- [ ] **Step 4: 接入设置保存后的调度重建**

```kotlin
fun saveDiaryGenerationTime(hour: Int, minute: Int) {
    settingsRepository.updateDiaryGenerationTime(hour, minute)
    aiDiaryGenerationScheduler.scheduleDaily(hour, minute)
}
```

- [ ] **Step 5: 运行 Worker 测试确认通过**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.work.AiDiaryAutoGenerationWorkerTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/work/AiDiaryAutoGenerationWorker.kt app/src/main/java/com/aiawareness/diary/ui/screens/SettingsViewModel.kt app/src/test/java/com/aiawareness/diary/work/AiDiaryAutoGenerationWorkerTest.kt
git commit -m "feat: add daily ai diary background worker"
```

### Task 4: 首页接入会话级补生成与提示

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/HomeScreenModels.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`

- [ ] **Step 1: 写首页提示失败测试**

```kotlin
@Test
fun startupCatchUp_success_setsSessionHintWithGeneratedCount() = runTest {
    // 协调器返回生成 3 篇
    // 断言 uiState.autoDiaryGenerationHint == "已补生成 3 篇 AI 日记，可前往回顾查看"
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest`
Expected: FAIL with missing count-based hint state

- [ ] **Step 3: 写最小实现**

```kotlin
data class CatchUpGenerationOutcome(
    val generatedCount: Int,
    val failed: Boolean
)
```

```kotlin
_uiState.update {
    it.copy(
        autoDiaryGenerationHint = when {
            outcome.failed -> "AI 日记生成失败，请检查账号、网络状态、连接配置等"
            outcome.generatedCount > 0 -> "已补生成 ${outcome.generatedCount} 篇 AI 日记，可前往回顾查看"
            else -> null
        }
    )
}
```

- [ ] **Step 4: 运行首页测试确认通过**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt app/src/main/java/com/aiawareness/diary/ui/screens/HomeScreenModels.kt app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt
git commit -m "feat: show session-scoped ai diary auto-generation hints"
```

### Task 5: 启动时补调度与全量验证

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/DiaryApplication.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt`

- [ ] **Step 1: 写启动补调度失败测试**

```kotlin
@Test
fun init_withSavedGenerationTime_reschedulesDailyWorker() = runTest {
    // 断言 scheduler.scheduleDaily(hour, minute) 被调用
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest`
Expected: FAIL with missing reschedule behavior

- [ ] **Step 3: 写最小实现**

```kotlin
init {
    loadUserSettings()
    ensureDailyGenerationSchedule()
    loadRecordsForDate(DateUtil.getCurrentDate())
    loadDatesWithRecords()
    checkAndGenerateDiaryIfNeeded()
}
```

- [ ] **Step 4: 跑核心测试与打包**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.domain.AiDiaryAutoGenerationCoordinatorTest --tests com.aiawareness.diary.work.AiDiaryAutoGenerationWorkerTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest`
Expected: PASS

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/DiaryApplication.kt app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt
git commit -m "feat: reschedule ai diary auto-generation on startup"
```
