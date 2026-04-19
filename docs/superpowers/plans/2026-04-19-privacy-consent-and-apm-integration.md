# Privacy Consent And APM Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Aliyun APM with a privacy-consent gate, show the privacy policy in-app from a GitHub Pages URL with a local fallback, and expose the policy from the About screen.

**Architecture:** `DiaryApplication` will always perform `Apm.preStart(...)`, while a new persisted privacy-consent flag controls whether `Apm.start()` may run. The UI layer will add a blocking startup dialog plus a reusable privacy-policy screen backed by WebView and a bundled fallback copy. Existing DataStore and navigation patterns stay in place to minimize surface area.

**Tech Stack:** Android app with Kotlin, Jetpack Compose, Navigation Compose, Hilt, DataStore Preferences, WebView, Aliyun APM Gradle plugin and SDK, JUnit4, existing Gradle Kotlin DSL build.

---

## File Map

- Modify `settings.gradle.kts`
  - Add Aliyun Maven repository to `pluginManagement` and `dependencyResolutionManagement`.
- Modify `app/build.gradle.kts`
  - Apply `com.aliyun.emas.apm` plugin version `3.1.0`.
  - Add `com.aliyun.ams:alicloud-apm:2.8.0`.
- Modify `gradle.properties`
  - Add `android.enableJetifier=true`.
- Modify `app/proguard-rules.pro`
  - Add `-keep class com.aliyun.emas.apm.**{*;}`.
- Modify `app/src/main/java/com/aiawareness/diary/DiaryApplication.kt`
  - Add `Apm.preStart(...)` with all components.
- Modify `app/src/main/java/com/aiawareness/diary/data/local/UserPreferences.kt`
  - Add privacy consent key, read flow, and write API.
- Create `app/src/test/java/com/aiawareness/diary/data/local/UserPreferencesTest.kt`
  - Test consent preference defaults and persistence.
- Modify `app/src/main/java/com/aiawareness/diary/ui/MainActivity.kt`
  - Wire startup gating and `Apm.start()` behavior into the root Compose content.
- Modify `app/src/main/java/com/aiawareness/diary/ui/navigation/NavGraph.kt`
  - Add privacy-policy route and pass navigation callbacks.
- Modify `app/src/main/java/com/aiawareness/diary/ui/screens/AboutScreen.kt`
  - Add privacy-policy entry UI and callback.
- Create `app/src/main/java/com/aiawareness/diary/ui/screens/PrivacyPolicyScreen.kt`
  - WebView loading + fallback text UI.
- Create `app/src/main/java/com/aiawareness/diary/ui/screens/PrivacyConsentDialog.kt`
  - Blocking startup dialog content.
- Create `app/src/test/java/com/aiawareness/diary/ui/screens/PrivacyPolicyContentTest.kt`
  - Test fixed privacy copy constants and fallback URL binding if copy is modeled.
- Modify `app/src/main/res/values/strings.xml`
  - Add privacy strings and URL placeholder.
- Create `docs/legal/privacy-policy.md`
  - Formal privacy policy text source.

### Task 1: Build And Runtime Dependency Setup

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `app/build.gradle.kts`
- Modify: `gradle.properties`
- Modify: `app/proguard-rules.pro`

- [ ] **Step 1: Write the failing build expectation down in the plan workspace**

Expected missing pieces before implementation:

```text
- No Aliyun Maven repository
- No com.aliyun.emas.apm plugin
- No alicloud-apm dependency
- No android.enableJetifier=true
- No APM keep rule
```

- [ ] **Step 2: Add the Aliyun Maven repository to `settings.gradle.kts`**

Add this repository block to both repository scopes:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.aliyun.com/nexus/content/repositories/releases/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.aliyun.com/nexus/content/repositories/releases/")
        }
    }
}
```

- [ ] **Step 3: Apply the APM plugin and dependency in `app/build.gradle.kts`**

Add these lines:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("com.aliyun.emas.apm") version "3.1.0"
}

dependencies {
    implementation("com.aliyun.ams:alicloud-apm:2.8.0")
}
```

If Gradle rejects the plugin version in the module file because the repo prefers root plugin management, move the plugin declaration to `build.gradle.kts` with `apply false` and leave only `id("com.aliyun.emas.apm")` in the app module.

- [ ] **Step 4: Add Jetifier and APM keep rules**

Add:

```properties
android.enableJetifier=true
```

and:

```proguard
-keep class com.aliyun.emas.apm.**{*;}
```

- [ ] **Step 5: Run a targeted Gradle parse/build check**

Run:

```bash
/bin/zsh -lc "PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew compileDebugKotlin"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 6: Commit the dependency setup**

Run:

```bash
git add settings.gradle.kts app/build.gradle.kts gradle.properties app/proguard-rules.pro
git commit -m "Add Aliyun APM build configuration"
```

### Task 2: Persist Privacy Consent State

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/data/local/UserPreferences.kt`
- Create: `app/src/test/java/com/aiawareness/diary/data/local/UserPreferencesTest.kt`

- [ ] **Step 1: Write the failing DataStore tests**

Create this test file:

```kotlin
package com.aiawareness.diary.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesTest {

    private fun createStore(file: File): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = { file })

    @Test
    fun privacyPolicyAccepted_defaultsToFalse() = runTest {
        val store = createStore(File.createTempFile("prefs", ".preferences_pb"))
        val preferences = UserPreferences(store)

        assertFalse(preferences.isPrivacyPolicyAccepted.first())
    }

    @Test
    fun markPrivacyPolicyAccepted_persistsTrue() = runTest {
        val store = createStore(File.createTempFile("prefs", ".preferences_pb"))
        val preferences = UserPreferences(store)

        preferences.setPrivacyPolicyAccepted(true)

        assertTrue(preferences.isPrivacyPolicyAccepted.first())
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
/bin/zsh -lc "PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.local.UserPreferencesTest"
```

Expected:

```text
FAILURE
```

and compiler errors for missing `isPrivacyPolicyAccepted` and `setPrivacyPolicyAccepted`.

- [ ] **Step 3: Add the minimal UserPreferences implementation**

Extend `UserPreferences.kt` with:

```kotlin
companion object {
    private val PRIVACY_POLICY_ACCEPTED = booleanPreferencesKey("privacy_policy_accepted")
}

val isPrivacyPolicyAccepted: Flow<Boolean> = dataStore.data.map { prefs ->
    prefs[PRIVACY_POLICY_ACCEPTED] ?: false
}

suspend fun setPrivacyPolicyAccepted(accepted: Boolean) {
    dataStore.edit { prefs -> prefs[PRIVACY_POLICY_ACCEPTED] = accepted }
}
```

Leave the existing `userSettings` flow untouched.

- [ ] **Step 4: Run the test to verify it passes**

Run:

```bash
/bin/zsh -lc "PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew testDebugUnitTest --tests com.aiawareness.diary.data.local.UserPreferencesTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 5: Commit the consent persistence change**

Run:

```bash
git add app/src/main/java/com/aiawareness/diary/data/local/UserPreferences.kt app/src/test/java/com/aiawareness/diary/data/local/UserPreferencesTest.kt
git commit -m "Persist privacy consent state"
```

### Task 3: Configure Aliyun APM Pre-Start

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/DiaryApplication.kt`

- [ ] **Step 1: Add the application-level APM configuration**

Update `DiaryApplication.kt` to call `Apm.preStart(...)` in `onCreate()`:

```kotlin
override fun onCreate() {
    super.onCreate()

    Apm.preStart(
        ApmOptions.Builder()
            .setApplication(this)
            .setAppKey("335703758")
            .setAppSecret("21ad46bfe80b41ec9ee33df25ca8e0fc")
            .setAppRsaSecret("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCutP8ArIwqaHNRRnoCO7suhV1lzgQoFtShWu/UogpkLjoYXd9ihgckNTjsE+/+khImZXTy+cXEVzdqD4u8GX68lPdG9tSv3Cm/jXqGt5MlUvNmAtvHIZJOpQDL3O2pvtkimNsFc6blWOOZqn2ychtNhQt3Z6C8EkTKVjpNB2H8SQIDAQAB")
            .addComponent(ApmCrashAnalysisComponent::class.java)
            .addComponent(ApmMemMonitorComponent::class.java)
            .addComponent(ApmRemoteLogComponent::class.java)
            .addComponent(ApmPerformanceComponent::class.java)
            .openDebug(BuildConfig.DEBUG)
            .build()
    )
}
```

Add the required imports:

```kotlin
import com.aliyun.emas.apm.Apm
import com.aliyun.emas.apm.ApmOptions
import com.aliyun.emas.apm.crash.ApmCrashAnalysisComponent
import com.aliyun.emas.apm.mem.monitor.ApmMemMonitorComponent
import com.aliyun.emas.apm.performance.ApmPerformanceComponent
import com.aliyun.emas.apm.remote.log.ApmRemoteLogComponent
```

- [ ] **Step 2: Run a compile check**

Run:

```bash
/bin/zsh -lc "PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew compileDebugKotlin"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 3: Commit the application configuration**

Run:

```bash
git add app/src/main/java/com/aiawareness/diary/DiaryApplication.kt
git commit -m "Configure Aliyun APM pre-start"
```

### Task 4: Add Privacy Policy Text Source And App Strings

**Files:**
- Create: `docs/legal/privacy-policy.md`
- Modify: `app/src/main/res/values/strings.xml`
- Create: `app/src/test/java/com/aiawareness/diary/ui/screens/PrivacyPolicyContentTest.kt`

- [ ] **Step 1: Write the failing content test**

Create:

```kotlin
package com.aiawareness.diary.ui.screens

import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyContentTest {

    @Test
    fun fallbackPolicy_mentionsLocalStorageAndThirdPartyServices() {
        val text = privacyPolicyFallbackText()

        assertTrue(text.contains("本地"))
        assertTrue(text.contains("OpenAI 兼容"))
        assertTrue(text.contains("S3 兼容"))
        assertTrue(text.contains("阿里云移动监控"))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
/bin/zsh -lc "PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.PrivacyPolicyContentTest"
```

Expected:

```text
FAILURE
```

with missing `privacyPolicyFallbackText`.

- [ ] **Step 3: Write the formal Markdown source**

Create `docs/legal/privacy-policy.md` with sections covering:

```markdown
# 隐私政策

## 1. 适用范围
## 2. 我们如何处理信息
## 3. 本地存储
## 4. 用户自主配置的第三方服务
## 5. 阿里云移动监控 SDK
## 6. 权限使用说明
## 7. 数据保存与删除
## 8. 你的权利
## 9. 政策更新
## 10. 联系方式
```

The content must explicitly mention:

- local diary text and images
- optional OpenAI-compatible model transmission
- optional S3-compatible object storage transmission
- no developer-operated backend
- Aliyun APM crash/performance/network/remote-log/memory collection

- [ ] **Step 4: Add app strings and fallback text model**

In `strings.xml`, add:

```xml
<string name="privacy_policy_title">隐私政策</string>
<string name="privacy_policy_url">https://YOUR_GITHUB_PAGES_URL/privacy-policy/</string>
<string name="privacy_consent_message">你需要先阅读并同意《隐私政策》后，应用才会启用监控与相关联网能力。</string>
<string name="privacy_agree">同意并继续</string>
<string name="privacy_disagree">不同意并退出</string>
<string name="privacy_load_failed">在线隐私政策暂时无法加载，以下为本地版本。</string>
```

Create a helper in the eventual privacy-policy UI package:

```kotlin
fun privacyPolicyFallbackText(): String = """
本应用默认将日记文本、图片及相关资料保存在你的设备本地。

当你主动配置 OpenAI 兼容模型服务时，你选择提交的提示词、日记内容、图片描述和 AI 生成结果可能发送到对应第三方服务。

当你主动配置 S3 兼容对象存储时，你选择备份或同步的文件可能发送到对应第三方对象存储服务。

本应用不提供开发者自建业务服务端，但客户端仍会根据你的主动配置向第三方服务发起请求。

本应用集成阿里云移动监控 SDK，用于崩溃分析、性能分析、网络监控、远程日志和内存监控；该能力仅在你同意隐私政策后启用。
""".trimIndent()
```

- [ ] **Step 5: Run the content test to verify it passes**

Run:

```bash
/bin/zsh -lc "PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.PrivacyPolicyContentTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 6: Commit the policy content foundation**

Run:

```bash
git add docs/legal/privacy-policy.md app/src/main/res/values/strings.xml app/src/test/java/com/aiawareness/diary/ui/screens/PrivacyPolicyContentTest.kt
git commit -m "Add privacy policy source content"
```

### Task 5: Add Privacy Policy Screen And Navigation

**Files:**
- Create: `app/src/main/java/com/aiawareness/diary/ui/screens/PrivacyPolicyScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/AboutScreen.kt`

- [ ] **Step 1: Create the privacy-policy screen**

Create `PrivacyPolicyScreen.kt` with a structure like:

```kotlin
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val url = stringResource(R.string.privacy_policy_url)
    var loadFailed by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JournalTokens.Paper)
    ) {
        EditorialTopBar(title = stringResource(R.string.privacy_policy_title), onBack = onNavigateBack)
        if (loadFailed) {
            Text(
                text = stringResource(R.string.privacy_load_failed),
                modifier = Modifier.padding(horizontal = JournalTokens.ScreenPadding, vertical = 12.dp),
                color = JournalTokens.MutedInk
            )
            SelectionContainer {
                Text(
                    text = privacyPolicyFallbackText(),
                    modifier = Modifier.padding(JournalTokens.ScreenPadding),
                    color = JournalTokens.Ink
                )
            }
        } else {
            AndroidView(
                factory = {
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        webViewClient = object : WebViewClient() {
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame != false) loadFailed = true
                            }
                        }
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
```

- [ ] **Step 2: Add the navigation route**

Extend `Screen` and `NavHost` with:

```kotlin
data object PrivacyPolicy : Screen("privacy_policy")
```

and:

```kotlin
composable(Screen.PrivacyPolicy.route) {
    PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
}
```

Pass callbacks so the About screen can navigate there:

```kotlin
AboutScreen(
    onNavigateBack = { navController.popBackStack() },
    onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) }
)
```

- [ ] **Step 3: Add the About screen entry**

Update `AboutScreen` signature and add a card/button row:

```kotlin
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
)
```

Add a simple tappable row labeled `隐私政策`.

- [ ] **Step 4: Run focused tests and compile**

Run:

```bash
/bin/zsh -lc "PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.AboutScreenContentTest compileDebugKotlin"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 5: Commit the privacy-policy UI**

Run:

```bash
git add app/src/main/java/com/aiawareness/diary/ui/screens/PrivacyPolicyScreen.kt app/src/main/java/com/aiawareness/diary/ui/navigation/NavGraph.kt app/src/main/java/com/aiawareness/diary/ui/screens/AboutScreen.kt
git commit -m "Add in-app privacy policy screen"
```

### Task 6: Add Startup Consent Dialog And APM Start Gate

**Files:**
- Create: `app/src/main/java/com/aiawareness/diary/ui/screens/PrivacyConsentDialog.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/MainActivity.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/navigation/NavGraph.kt`

- [ ] **Step 1: Create the blocking dialog composable**

Create:

```kotlin
@Composable
fun PrivacyConsentDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("隐私政策") },
        text = {
            Column {
                Text(stringResource(R.string.privacy_consent_message))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "查看《隐私政策》",
                    modifier = Modifier.clickable(onClick = onOpenPrivacyPolicy),
                    color = JournalTokens.Sage
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAgree) { Text(stringResource(R.string.privacy_agree)) }
        },
        dismissButton = {
            TextButton(onClick = onDisagree) { Text(stringResource(R.string.privacy_disagree)) }
        }
    )
}
```

- [ ] **Step 2: Add startup gating to `MainActivity`**

Refactor root content to collect consent and gate APM start:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()
            val accepted by userPreferences.isPrivacyPolicyAccepted.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            LaunchedEffect(accepted) {
                if (accepted) {
                    Apm.start()
                }
            }

            AiAwarenessDiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(navController = navController)
                    if (!accepted) {
                        PrivacyConsentDialog(
                            onAgree = {
                                scope.launch {
                                    userPreferences.setPrivacyPolicyAccepted(true)
                                }
                            },
                            onDisagree = { finish() },
                            onOpenPrivacyPolicy = {
                                navController.navigate(Screen.PrivacyPolicy.route)
                            }
                        )
                    }
                }
            }
        }
    }
}
```

If `Apm.start()` can be re-entered repeatedly on recomposition, add a local remembered guard or move the call to a lifecycle-safe effect keyed on a one-way transition.

- [ ] **Step 3: Run focused verification**

Run:

```bash
/bin/zsh -lc "PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew compileDebugKotlin testDebugUnitTest --tests com.aiawareness.diary.data.local.UserPreferencesTest --tests com.aiawareness.diary.ui.screens.PrivacyPolicyContentTest"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 4: Manual-check the startup flow on device/emulator**

Verify:

```text
Fresh install -> dialog shown
Tap privacy link -> in-app privacy page
Tap disagree -> app exits
Relaunch -> dialog shown again
Tap agree -> app remains open and future launches skip dialog
```

- [ ] **Step 5: Commit the startup consent flow**

Run:

```bash
git add app/src/main/java/com/aiawareness/diary/ui/screens/PrivacyConsentDialog.kt app/src/main/java/com/aiawareness/diary/ui/MainActivity.kt app/src/main/java/com/aiawareness/diary/ui/navigation/NavGraph.kt
git commit -m "Gate APM startup on privacy consent"
```

### Task 7: Final Verification And Integration Cleanup

**Files:**
- Modify: any touched files from prior tasks if verification finds issues

- [ ] **Step 1: Run the main verification suite**

Run:

```bash
/bin/zsh -lc "PATH=/opt/homebrew/opt/openjdk@17/bin:$PATH ./gradlew testDebugUnitTest compileDebugKotlin"
```

Expected:

```text
BUILD SUCCESSFUL
```

- [ ] **Step 2: Verify APM debug logging on a debug run**

Expected log tags after consent and startup:

```text
Apm-CrashAnalysis
Apm-RemoteLog
Apm-Performance
Apm-MemMonitor
Apm-NetworkMonitor
```

- [ ] **Step 3: Review changed files for scope discipline**

Confirm:

```text
- About screen has privacy entry
- Settings screen did not gain a privacy entry
- Privacy policy has both hosted and fallback access
- APM start remains gated on consent
```

- [ ] **Step 4: Commit any final verification fixes**

Run:

```bash
git add <updated-files>
git commit -m "Polish privacy consent integration"
```

## Self-Review

- Spec coverage check:
  - APM build setup: Task 1
  - `Apm.preStart(...)`: Task 3
  - consent persistence: Task 2
  - blocking startup dialog: Task 6
  - in-app privacy page with WebView and fallback: Task 5
  - About screen entry: Task 5
  - formal privacy-policy source: Task 4
  - testing and verification: Tasks 2, 4, 5, 6, 7
- Placeholder scan:
  - No `TODO`, `TBD`, or deferred “write tests later” steps remain.
- Type consistency:
  - Consent API names are `isPrivacyPolicyAccepted` and `setPrivacyPolicyAccepted`.
  - Navigation route name is `Screen.PrivacyPolicy`.
  - Fallback helper name is `privacyPolicyFallbackText()`.
