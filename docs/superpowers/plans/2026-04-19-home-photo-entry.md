# 首页图片入口交互 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将首页输入框的图片入口从文件选择器改为底部菜单，支持稳定的“从相册选择 / 拍照”双路径。

**Architecture:** 首页 `InputScreen` 负责图片入口 UI、activity result launcher 和 snackbar 反馈；`MainViewModel` 继续统一维护 pending photo 状态；`RecordPhotoStorage` 与 `FileProvider` 复用现有临时文件和落盘能力，不引入新的照片存储路径。相册与拍照最终都汇合到现有保存记录流程，避免改动记录持久化主链路。

**Tech Stack:** Kotlin, Jetpack Compose, Android Activity Result APIs (`PickVisualMedia`, `TakePicture`), Hilt, JUnit4, Gradle

---

### Task 1: 锁定图片入口与拍照状态的回归测试

**Files:**
- Modify: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`
- Modify: `app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt`

- [ ] **Step 1: 为首页图片入口模式写失败测试**

在 `HomeScreenModelsTest.kt` 新增一个只描述入口行为的测试，锁定“上传图片按钮不再直接走文件选择器，而是先弹底部菜单”。

```kotlin
@Test
fun homePhotoEntry_usesActionSheetBeforeLaunchingPicker() {
    assertEquals(HomePhotoEntryMode.ActionSheet, homePhotoEntryMode())
    assertEquals("从相册选择", homePhotoSheetPickLabel())
    assertEquals("拍照", homePhotoSheetCaptureLabel())
}
```

- [ ] **Step 2: 运行单测并确认是红灯**

Run:

```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest
```

Expected: `compileDebugUnitTestKotlin FAILED`，因为 `HomePhotoEntryMode`、`homePhotoEntryMode()`、`homePhotoSheetPickLabel()`、`homePhotoSheetCaptureLabel()` 还不存在。

- [ ] **Step 3: 为拍照 pending 状态写失败测试**

在 `MainViewModelTest.kt` 增加一个测试，验证拍照成功后进入 captured pending 状态，并且来源为 `Captured`。

```kotlin
@Test
fun setPendingCapturedPhoto_setsCapturedPreviewState() = runTest {
    val tempFile = File.createTempFile("capture_", ".jpg")

    viewModel.setPendingCapturedPhoto(tempFile)

    val state = viewModel.uiState.value
    assertEquals(null, state.pendingPhotoUri)
    assertEquals(tempFile, state.pendingCapturedPhotoFile)
    assertEquals(PendingPhotoSource.Captured, state.pendingPhotoSource)

    tempFile.delete()
}
```

- [ ] **Step 4: 为“替换旧拍照临时文件”写失败测试**

继续在 `MainViewModelTest.kt` 增加替换测试，锁定旧临时文件会被清掉。

```kotlin
@Test
fun setPendingCapturedPhoto_replacesAndDeletesOldTempFile() = runTest {
    val first = File.createTempFile("capture_old_", ".jpg").apply { writeText("old") }
    val second = File.createTempFile("capture_new_", ".jpg").apply { writeText("new") }

    viewModel.setPendingCapturedPhoto(first)
    viewModel.setPendingCapturedPhoto(second)

    assertEquals(second, viewModel.uiState.value.pendingCapturedPhotoFile)
    assertEquals(false, first.exists())

    second.delete()
}
```

- [ ] **Step 5: 运行 ViewModel 相关测试并确认是红灯或部分红灯**

Run:

```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest
```

Expected: 至少新增的首页入口 helper 测试为红灯；若 `MainViewModel` 已覆盖该行为，则记录拍照状态测试可能直接为绿灯，这是可接受的，继续下一步。

- [ ] **Step 6: 提交测试基线**

```bash
git add app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt
git commit -m "test: add home photo entry regression coverage"
```

### Task 2: 接入底部菜单、系统相册与系统相机

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/HomeScreenModels.kt` or `InputScreen.kt` helper section

- [ ] **Step 1: 为首页图片入口 helper 写最小实现**

在现有首页 helper 区域新增入口模式与文案 helper，尽量与现有 `homeSendButtonLabel()` 这类函数放在一起。

```kotlin
enum class HomePhotoEntryMode { ActionSheet }

internal fun homePhotoEntryMode(): HomePhotoEntryMode = HomePhotoEntryMode.ActionSheet
internal fun homePhotoSheetPickLabel(): String = "从相册选择"
internal fun homePhotoSheetCaptureLabel(): String = "拍照"
internal fun homePhotoCaptureFailureMessage(): String = "拍照失败，请重试"
internal fun homePhotoNoCameraMessage(): String = "未找到可用相机"
```

- [ ] **Step 2: 先运行首页 helper 测试确保转绿**

Run:

```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest
```

Expected: `BUILD SUCCESSFUL`，新增 helper 测试通过。

- [ ] **Step 3: 在 `InputScreen` 新增底部菜单状态与 launcher**

用 `remember { mutableStateOf(false) }` 管理底部菜单开关；把当前 `OpenDocument` launcher 替换为 `PickVisualMedia`，再增加 `TakePicture` launcher 和拍照临时文件 `Uri` 状态。

```kotlin
var showPhotoEntrySheet by remember { mutableStateOf(false) }
var pendingCameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri ->
    if (uri != null) {
        viewModel.importPendingPhoto(uri)
    }
}

val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success ->
    val pendingUri = pendingCameraPhotoUri
    if (success && pendingUri != null) {
        val tempFile = viewModel.createPendingCameraPhotoFileOrNull()
        // 这里不要真的按这个片段实现，下一步会把“建文件”和“发起拍照”拆清楚。
    } else if (!success) {
        snackbarHostState.showSnackbar(homePhotoCaptureFailureMessage())
    }
    pendingCameraPhotoUri = null
}
```

- [ ] **Step 4: 把拍照文件创建职责放回 ViewModel/Storage 边界**

避免在 `InputScreen` 里手写缓存目录路径。给 `MainViewModel` 增加一个仅负责生成相机临时文件的方法，直接复用 `RecordPhotoStorage.createCameraTempFile()`。

```kotlin
fun createPendingCameraTempFile(): File = photoStorage.createCameraTempFile()
```

如果你担心 UI 层抛异常，可以改成：

```kotlin
fun createPendingCameraTempFileOrNull(): File? = runCatching {
    photoStorage.createCameraTempFile()
}.getOrNull()
```

本计划推荐第二种，这样 `InputScreen` 可以直接根据 `null` 给出 snackbar，而不把异常处理散到 launcher 回调里。

- [ ] **Step 5: 在 `InputScreen` 实现拍照启动逻辑**

使用 `FileProvider.getUriForFile(...)` 生成 `Uri`，并在启动失败时用 snackbar 提示。

```kotlin
fun launchCameraCapture() {
    val tempFile = viewModel.createPendingCameraTempFileOrNull()
    if (tempFile == null) {
        scope.launch { snackbarHostState.showSnackbar(homePhotoCaptureFailureMessage()) }
        return
    }

    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, tempFile)
    pendingCameraPhotoUri = uri
    viewModel.setPendingCapturedPhoto(tempFile, PendingPhotoSource.Captured)
    cameraLauncher.launch(uri)
}
```

实现时需要修正一处细节：`setPendingCapturedPhoto(...)` 不应在启动相机前就覆盖现有图片。正确顺序是先保存一个局部 `tempFile` 状态，只有 `success == true` 时才调用：

```kotlin
var pendingCameraTempFile by remember { mutableStateOf<File?>(null) }
```

然后：

```kotlin
pendingCameraTempFile = tempFile
pendingCameraPhotoUri = uri
cameraLauncher.launch(uri)
```

在回调里：

```kotlin
if (success && pendingCameraTempFile != null) {
    viewModel.setPendingCapturedPhoto(pendingCameraTempFile, PendingPhotoSource.Captured)
}
```

- [ ] **Step 6: 为图片按钮接入底部菜单**

把 `HomeInputBar` 的 `onPickPhoto` 从直接 `photoPicker.launch(...)` 改成先打开菜单：

```kotlin
onPickPhoto = { showPhotoEntrySheet = true }
```

并在 `InputScreen` 根部增加 `ModalBottomSheet` 或当前项目已在用的 Material3 底部弹层：

```kotlin
if (showPhotoEntrySheet) {
    ModalBottomSheet(
        onDismissRequest = { showPhotoEntrySheet = false },
        containerColor = HomeSurface
    ) {
        ListItem(
            headlineContent = { Text(homePhotoSheetPickLabel()) },
            modifier = Modifier.clickable {
                showPhotoEntrySheet = false
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
        ListItem(
            headlineContent = { Text(homePhotoSheetCaptureLabel()) },
            modifier = Modifier.clickable {
                showPhotoEntrySheet = false
                launchCameraCapture()
            }
        )
    }
}
```

- [ ] **Step 7: 处理取消与失败分支**

把以下规则明确写进 launcher 回调：

```kotlin
// 相册取消：什么都不做
if (uri == null) return@rememberLauncherForActivityResult

// 拍照取消：删除这次创建的新 temp file，但不清空已有 pending 图片
if (!success) {
    pendingCameraTempFile?.takeIf(File::exists)?.delete()
    pendingCameraTempFile = null
    pendingCameraPhotoUri = null
    return@rememberLauncherForActivityResult
}
```

如果 `cameraLauncher.launch(uri)` 抛出 `ActivityNotFoundException`，则：

```kotlin
scope.launch { snackbarHostState.showSnackbar(homePhotoNoCameraMessage()) }
```

- [ ] **Step 8: 运行首页与 ViewModel 测试**

Run:

```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: 提交交互实现**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt app/src/main/java/com/aiawareness/diary/ui/screens/HomeScreenModels.kt app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt
git commit -m "feat: add home photo action sheet entry"
```

### Task 3: 收口异常处理与最终验证

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`

- [ ] **Step 1: 收口拍照失败与取消后的临时状态清理**

确保 `pendingCameraTempFile` 和 `pendingCameraPhotoUri` 在成功、失败、取消三条路径都会归零，避免悬挂状态。

```kotlin
fun clearPendingCameraLaunchState() {
    pendingCameraTempFile = null
    pendingCameraPhotoUri = null
}
```

在回调里统一调用：

```kotlin
if (success && pendingCameraTempFile != null) {
    viewModel.setPendingCapturedPhoto(pendingCameraTempFile, PendingPhotoSource.Captured)
}
clearPendingCameraLaunchState()
```

- [ ] **Step 2: 检查文案与 spec 一致**

确认以下 helper 文案最终保持一致：

```kotlin
homeAttachPhotoButtonLabel() == "上传图片"
homePhotoSheetPickLabel() == "从相册选择"
homePhotoSheetCaptureLabel() == "拍照"
homePhotoCaptureFailureMessage() == "拍照失败，请重试"
homePhotoNoCameraMessage() == "未找到可用相机"
```

如果首页 placeholder 或其他无关文案此前被改动，不在这个任务里顺手调整。

- [ ] **Step 3: 跑目标测试集**

Run:

```bash
./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest --tests com.aiawareness.diary.ui.screens.MainViewModelTest --tests com.aiawareness.diary.ui.screens.HomeTimelineTest
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 跑一次编译验证**

Run:

```bash
./gradlew compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 提交最终收口**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt app/src/main/java/com/aiawareness/diary/ui/screens/MainViewModel.kt app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt app/src/test/java/com/aiawareness/diary/ui/screens/MainViewModelTest.kt
git commit -m "fix: stabilize home photo capture flow"
```

## Self-Review

- Spec coverage:
  - 底部菜单入口：Task 2 Step 6
  - 系统相册替换 `OpenDocument`：Task 2 Step 3 and Step 6
  - 系统相机接入：Task 2 Step 4 and Step 5
  - 取消不清空已有图片：Task 2 Step 7
  - 失败 snackbar：Task 2 Step 7 and Task 3 Step 2
  - 测试补充：Task 1、Task 2 Step 8、Task 3 Step 3
- Placeholder scan: 已去除 `TODO`/`TBD`；所有命令、文件和 helper 名称都已明确。
- Type consistency: 计划统一使用 `pendingCameraTempFile`、`pendingCameraPhotoUri`、`createPendingCameraTempFileOrNull()`、`homePhotoSheetPickLabel()`、`homePhotoSheetCaptureLabel()`。
