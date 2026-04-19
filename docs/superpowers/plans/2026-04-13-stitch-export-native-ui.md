# Stitch Export Native UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the Android Compose UI so every page exported from `design/stitch-export` is implemented in native Compose while preserving the app's real navigation, persistence, settings, and AI data flow.

**Architecture:** Keep the current Room, DataStore, Hilt, Retrofit, and navigation structure intact. Introduce a shared editorial design token layer and a small set of reusable Compose components, then rebuild each screen family on top of the existing `MainViewModel` and `SettingsViewModel` data contracts.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Navigation Compose, Hilt, Room, DataStore, JUnit4

---

## File Structure

### Shared theme and reusable UI foundation

**Create:**

- `app/src/main/java/com/aiawareness/diary/ui/theme/JournalTokens.kt`
- `app/src/main/java/com/aiawareness/diary/ui/components/EditorialTopBar.kt`
- `app/src/main/java/com/aiawareness/diary/ui/components/EditorialSurfaceCard.kt`
- `app/src/main/java/com/aiawareness/diary/ui/components/SettingsSection.kt`
- `app/src/main/java/com/aiawareness/diary/ui/components/DecorativeBackground.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/SettingsUiFormatters.kt`
- `app/src/test/java/com/aiawareness/diary/ui/screens/SettingsUiFormattersTest.kt`

**Modify:**

- `app/src/main/java/com/aiawareness/diary/ui/theme/Color.kt`
- `app/src/main/java/com/aiawareness/diary/ui/theme/Theme.kt`
- `app/src/main/java/com/aiawareness/diary/ui/theme/Type.kt`

### Home and review surfaces

**Create:**

- `app/src/main/java/com/aiawareness/diary/ui/screens/HomeScreenModels.kt`
- `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/ReviewScreenModels.kt`
- `app/src/test/java/com/aiawareness/diary/ui/screens/ReviewScreenModelsTest.kt`

**Modify:**

- `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/CalendarScreen.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/DiaryDetailScreen.kt`
- `app/src/main/java/com/aiawareness/diary/ui/components/CalendarView.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/HomeTimeline.kt`

### Settings family

**Modify:**

- `app/src/main/java/com/aiawareness/diary/ui/screens/SettingsScreen.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/PersonalInfoScreen.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/AiConfigScreen.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/DataManagementScreen.kt`
- `app/src/main/java/com/aiawareness/diary/ui/screens/AboutScreen.kt`

### App shell verification

**Modify:**

- `app/src/main/java/com/aiawareness/diary/ui/MainActivity.kt`
- `app/src/main/java/com/aiawareness/diary/ui/navigation/NavGraph.kt`

## Task 1: Build Editorial Theme Foundation

**Files:**

- Create: `app/src/main/java/com/aiawareness/diary/ui/theme/JournalTokens.kt`
- Create: `app/src/main/java/com/aiawareness/diary/ui/components/EditorialTopBar.kt`
- Create: `app/src/main/java/com/aiawareness/diary/ui/components/EditorialSurfaceCard.kt`
- Create: `app/src/main/java/com/aiawareness/diary/ui/components/DecorativeBackground.kt`
- Create: `app/src/main/java/com/aiawareness/diary/ui/screens/SettingsUiFormatters.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/SettingsUiFormattersTest.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/theme/Type.kt`

- [ ] **Step 1: Write the failing formatter test for reusable settings labels**

```kotlin
package com.aiawareness.diary.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsUiFormattersTest {

    @Test
    fun formatGenerationTime_padsHourAndMinute() {
        assertEquals("每日 07:05 自动汇总结语", formatGenerationTimeSummary(7, 5))
    }

    @Test
    fun profileQuote_usesFallbackWhenBlank() {
        assertEquals("“回到呼吸，也回到自己”", profileQuoteOrFallback(""))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.SettingsUiFormattersTest`
Expected: FAIL with unresolved references for `formatGenerationTimeSummary` and `profileQuoteOrFallback`

- [ ] **Step 3: Implement the formatter helpers and editorial design tokens**

```kotlin
package com.aiawareness.diary.ui.screens

fun formatGenerationTimeSummary(hour: Int, minute: Int): String =
    "每日 %02d:%02d 自动汇总结语".format(hour, minute)

fun profileQuoteOrFallback(value: String): String =
    if (value.isBlank()) "“回到呼吸，也回到自己”" else value
```

```kotlin
package com.aiawareness.diary.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object JournalTokens {
    val Paper = Color(0xFFFBF9F4)
    val Ink = Color(0xFF31332E)
    val MutedInk = Color(0xFF5E6059)
    val Sage = Color(0xFF546356)
    val SageDim = Color(0xFF48574B)
    val SageContainer = Color(0xFFD6E7D7)
    val Clay = Color(0xFF855245)
    val ClayContainer = Color(0xFFFFDAD2)
    val Stone = Color(0xFF635E5B)
    val SurfaceLow = Color(0xFFF5F4ED)
    val SurfaceHigh = Color(0xFFE9E8E1)
    val SurfaceHighest = Color(0xFFE3E3DB)
    val CardRadius = 28.dp
    val PillRadius = 999.dp
    val ScreenPadding = 24.dp
}
```

- [ ] **Step 4: Update the app theme to use the Stitch palette and typography**

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = JournalTokens.Sage,
    onPrimary = Color(0xFFECFDED),
    primaryContainer = JournalTokens.SageContainer,
    onPrimaryContainer = Color(0xFF465549),
    secondary = JournalTokens.Clay,
    onSecondary = Color(0xFFFFF7F6),
    secondaryContainer = JournalTokens.ClayContainer,
    background = JournalTokens.Paper,
    surface = Color.White,
    onBackground = JournalTokens.Ink,
    onSurface = JournalTokens.Ink
)
```

```kotlin
val AppTypography = Typography(
    displayMedium = TextStyle(fontFamily = HeadlineFontFamily, fontSize = 36.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = HeadlineFontFamily, fontSize = 30.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontFamily = HeadlineFontFamily, fontSize = 24.sp, lineHeight = 30.sp),
    bodyLarge = TextStyle(fontFamily = BodyFontFamily, fontSize = 16.sp, lineHeight = 28.sp),
    labelSmall = TextStyle(fontFamily = BodyFontFamily, fontSize = 11.sp, letterSpacing = 1.8.sp)
)
```

- [ ] **Step 5: Add shared top bar and tonal card components**

```kotlin
@Composable
fun EditorialSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(JournalTokens.CardRadius),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}
```

```kotlin
@Composable
fun EditorialTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                FilledTonalIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontStyle = FontStyle.Italic)
        }
        Row(content = actions)
    }
}
```

- [ ] **Step 6: Run tests to verify the foundation passes**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.SettingsUiFormattersTest`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/theme/Color.kt \
  app/src/main/java/com/aiawareness/diary/ui/theme/Theme.kt \
  app/src/main/java/com/aiawareness/diary/ui/theme/Type.kt \
  app/src/main/java/com/aiawareness/diary/ui/theme/JournalTokens.kt \
  app/src/main/java/com/aiawareness/diary/ui/components/EditorialTopBar.kt \
  app/src/main/java/com/aiawareness/diary/ui/components/EditorialSurfaceCard.kt \
  app/src/main/java/com/aiawareness/diary/ui/components/DecorativeBackground.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/SettingsUiFormatters.kt \
  app/src/test/java/com/aiawareness/diary/ui/screens/SettingsUiFormattersTest.kt
git commit -m "feat: add editorial theme foundation"
```

## Task 2: Rebuild the Home Screen Around Real Records

**Files:**

- Create: `app/src/main/java/com/aiawareness/diary/ui/screens/HomeScreenModels.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/HomeTimeline.kt`

- [ ] **Step 1: Write the failing home mapping tests**

```kotlin
package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Record
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeScreenModelsTest {

    @Test
    fun progressDots_capsAtFour() {
        assertEquals(listOf(true, true, true, true), progressDots(recordCount = 7))
    }

    @Test
    fun iconForRecordContent_mapsWindTextToAir() {
        val record = Record(id = 1, date = "2026-04-13", time = "18:30", content = "窗外的风吹动了窗帘")
        assertEquals(HomeRecordIcon.Air, iconForHomeRecord(record))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest`
Expected: FAIL with unresolved references for `progressDots`, `HomeRecordIcon`, and `iconForHomeRecord`

- [ ] **Step 3: Implement reusable home screen mapping helpers**

```kotlin
package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Record

enum class HomeRecordIcon { Breath, Air, Rest, Heart, Reflection }

fun progressDots(recordCount: Int): List<Boolean> =
    List(4) { index -> index < recordCount.coerceAtMost(4) }

fun iconForHomeRecord(record: Record): HomeRecordIcon {
    val content = record.content
    return when {
        "风" in content || "窗" in content -> HomeRecordIcon.Air
        "疲惫" in content || "困" in content -> HomeRecordIcon.Rest
        "呼吸" in content -> HomeRecordIcon.Breath
        "心跳" in content || "胸" in content -> HomeRecordIcon.Heart
        else -> HomeRecordIcon.Reflection
    }
}
```

- [ ] **Step 4: Rebuild `InputScreen` to match `home/code.html`**

```kotlin
Scaffold(
    containerColor = JournalTokens.Paper,
    topBar = {
        Surface(color = JournalTokens.Paper.copy(alpha = 0.88f)) {
            HomeTopBar(
                nickname = uiState.userSettings.nickname.ifBlank { "用户" },
                avatarPath = uiState.userSettings.avatarPath,
                dateText = DateUtil.getCurrentDisplayDateWithWeekday(),
                onNavigateToReview = onNavigateToCalendar,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    },
    bottomBar = {
        HomeInputBar(
            value = inputText,
            expanded = isInputExpanded,
            focusRequester = focusRequester,
            onValueChange = { inputText = it },
            onFocusChanged = { isInputExpanded = it },
            onMicClick = { focusRequester.requestFocus() },
            onSendClick = {
                val trimmed = inputText.trim()
                if (trimmed.isNotBlank()) {
                    viewModel.saveRecord(trimmed)
                    inputText = ""
                    isInputExpanded = false
                }
            }
        )
    }
) { paddingValues ->
    LazyColumn(
        contentPadding = PaddingValues(
            start = JournalTokens.ScreenPadding,
            end = JournalTokens.ScreenPadding,
            top = paddingValues.calculateTopPadding() + 12.dp,
            bottom = paddingValues.calculateBottomPadding() + 28.dp
        )
    ) {
        item { DailyProgress(recordCount = uiState.records.size) }
        timelineSections.forEach { section ->
            item { TimelineSectionHeader(title = section.title) }
            items(section.records, key = { it.id }) { record ->
                JournalEntryCard(
                    record = record,
                    icon = iconForHomeRecord(record),
                    expanded = menuRecordId == record.id,
                    onLongPress = { menuRecordId = record.id },
                    onDismissMenu = { menuRecordId = -1L },
                    onEdit = {
                        editingRecord = record
                        editingText = record.content
                        menuRecordId = -1L
                    },
                    onDelete = {
                        menuRecordId = -1L
                        viewModel.deleteRecord(record.id)
                    }
                )
            }
        }
        item {
            AiDiaryActionCard(
                hasApiKey = uiState.hasApiKey,
                hasDiary = !uiState.diary?.aiDiary.isNullOrBlank(),
                isGenerating = uiState.isGeneratingDiary,
                onGenerate = viewModel::generateDiary,
                onNavigateToAiConfig = onNavigateToAiConfig
            )
        }
    }
}
```

- [ ] **Step 5: Keep edit/delete/generate behavior intact while swapping visuals**

```kotlin
JournalEntryCard(
    record = record,
    icon = iconForHomeRecord(record),
    expanded = menuRecordId == record.id,
    onLongPress = { menuRecordId = record.id },
    onDismissMenu = { menuRecordId = -1L },
    onEdit = {
        editingRecord = record
        editingText = record.content
        menuRecordId = -1L
    },
    onDelete = {
        menuRecordId = -1L
        viewModel.deleteRecord(record.id)
    }
)
```

- [ ] **Step 6: Run tests to verify home mappings pass**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest --tests com.aiawareness.diary.ui.screens.HomeTimelineTest`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/screens/HomeScreenModels.kt \
  app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/HomeTimeline.kt
git commit -m "feat: restyle home capture screen"
```

## Task 3: Rebuild Review and Diary Detail

**Files:**

- Create: `app/src/main/java/com/aiawareness/diary/ui/screens/ReviewScreenModels.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/ReviewScreenModelsTest.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/components/CalendarView.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/CalendarScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/DiaryDetailScreen.kt`

- [ ] **Step 1: Write the failing review mapping tests**

```kotlin
package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Diary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewScreenModelsTest {

    @Test
    fun reviewHeader_formatsMonthTitle() {
        assertEquals("April 2026", reviewMonthTitle(2026, 4))
    }

    @Test
    fun hasDiarySummary_isFalseWhenDiaryEmpty() {
        assertTrue(!hasDiarySummary(Diary(date = "2026-04-13", aiDiary = "", aiInsight = "")))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.ReviewScreenModelsTest`
Expected: FAIL with unresolved references for `reviewMonthTitle` and `hasDiarySummary`

- [ ] **Step 3: Implement review helper functions**

```kotlin
package com.aiawareness.diary.ui.screens

import com.aiawareness.diary.data.model.Diary
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

fun reviewMonthTitle(year: Int, month: Int): String =
    "${Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH)} $year"

fun hasDiarySummary(diary: Diary?): Boolean =
    diary != null && (diary.aiDiary.isNotBlank() || diary.aiInsight.isNotBlank())
```

- [ ] **Step 4: Rebuild the calendar screen around the Stitch `review` structure**

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .background(JournalTokens.Paper)
        .padding(horizontal = JournalTokens.ScreenPadding)
) {
    EditorialTopBar(title = "回顾", onBack = onNavigateBack)
    EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = reviewMonthTitle(uiState.currentYear, uiState.currentMonth), style = MaterialTheme.typography.titleLarge)
        CalendarView(
            year = uiState.currentYear,
            month = uiState.currentMonth,
            datesWithRecords = uiState.datesWithRecords,
            onDateSelected = onDateSelected
        )
    }
    EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Text("原始记录", style = MaterialTheme.typography.titleLarge)
        uiState.records.take(3).forEach { record ->
            Text(record.time, style = MaterialTheme.typography.labelSmall, color = JournalTokens.MutedInk)
            Text(record.content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))
        }
    }
    EditorialSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Text("AI日记", style = MaterialTheme.typography.titleLarge)
        Text(
            text = uiState.diary?.aiDiary?.takeIf { it.isNotBlank() } ?: "这一天还没有生成 AI 日记。",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

- [ ] **Step 5: Rebuild the diary detail screen using the same review language**

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize().background(JournalTokens.Paper),
    contentPadding = PaddingValues(24.dp)
) {
    item {
        EditorialTopBar(title = displayDate, onBack = onNavigateBack)
    }
    item {
        EditorialSurfaceCard {
            Text("原始记录", style = MaterialTheme.typography.titleLarge)
            records.forEach { record ->
                Text(record.time, style = MaterialTheme.typography.labelSmall, color = JournalTokens.MutedInk)
                Text(record.content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 16.dp))
            }
        }
    }
    item {
        EditorialSurfaceCard {
            Text("AI日记", style = MaterialTheme.typography.titleLarge)
            Text(
                diary?.aiDiary?.takeIf { it.isNotBlank() } ?: "这一天还没有生成 AI 日记。",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("AI摘要", style = MaterialTheme.typography.titleMedium)
            Text(
                diary?.aiInsight?.takeIf { it.isNotBlank() } ?: "生成后会在这里显示摘要与觉察提示。",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

- [ ] **Step 6: Run tests to verify review mappings pass**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.ReviewScreenModelsTest`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/screens/ReviewScreenModels.kt \
  app/src/test/java/com/aiawareness/diary/ui/screens/ReviewScreenModelsTest.kt \
  app/src/main/java/com/aiawareness/diary/ui/components/CalendarView.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/CalendarScreen.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/DiaryDetailScreen.kt
git commit -m "feat: rebuild review and diary detail screens"
```

## Task 4: Rebuild Settings Hub and Detail Pages

**Files:**

- Create: `app/src/main/java/com/aiawareness/diary/ui/components/SettingsSection.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/SettingsScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/PersonalInfoScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/AiConfigScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/DataManagementScreen.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/AboutScreen.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/SettingsUiFormattersTest.kt`

- [ ] **Step 1: Extend the settings formatter test with hub copy expectations**

```kotlin
@Test
fun formatGenerationTimeSummary_usesTwoDigitStyle() {
    assertEquals("每日 22:00 自动汇总结语", formatGenerationTimeSummary(22, 0))
}

@Test
fun profileQuoteOrFallback_keepsUserValueWhenPresent() {
    assertEquals("愿今日仍有余白", profileQuoteOrFallback("愿今日仍有余白"))
}
```

- [ ] **Step 2: Run test to verify it fails if formatters are incomplete**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.SettingsUiFormattersTest`
Expected: PASS if Task 1 is complete; otherwise FAIL and fix before proceeding

- [ ] **Step 3: Rebuild `SettingsScreen` using grouped editorial sections**

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize().background(JournalTokens.Paper),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
) {
    item { EditorialTopBar(title = "设置", onBack = onNavigateBack) }
    item {
        ProfileHero(
            nickname = uiState.settings.nickname.ifBlank { "用户" },
            avatarPath = uiState.settings.avatarPath,
            quote = profileQuoteOrFallback("")
        )
    }
    item {
        SettingsSection(
            title = "智慧陪伴",
            items = listOf(
                SettingsRowModel("AI 配置", "自定义您的 AI 语气与模型", onNavigateToAiConfig),
                SettingsRowModel(
                    "AI日记生成时间",
                    formatGenerationTimeSummary(
                        uiState.settings.diaryGenerationHour,
                        uiState.settings.diaryGenerationMinute
                    ),
                    onClick = {}
                )
            )
        )
    }
}
```

- [ ] **Step 4: Rebuild personal info, AI config, data management, and about pages with the shared components**

```kotlin
@Composable
fun PersonalInfoScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(containerColor = JournalTokens.Paper) { paddingValues ->
        LazyColumn(contentPadding = PaddingValues(24.dp)) {
            item { EditorialTopBar(title = "个人信息", onBack = onNavigateBack) }
            item {
                EditorialSurfaceCard {
                    AsyncImage(model = avatarUri ?: uiState.settings.avatarPath, contentDescription = "头像")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("昵称") })
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { imagePicker.launch("image/*") }) { Text("选择头像") }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.savePersonalInfo(nickname, avatarUri?.toString()) }) { Text("保存") }
                }
            }
        }
    }
}
```

```kotlin
@Composable
fun AiConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(JournalTokens.Paper),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { EditorialTopBar(title = "AI 配置", onBack = onNavigateBack) }
        item {
            EditorialSurfaceCard {
                OutlinedTextField(value = apiEndpoint, onValueChange = { apiEndpoint = it }, label = { Text("API URL") })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key") })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = modelName, onValueChange = { modelName = it }, label = { Text("模型名称") })
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.saveApiConfig(apiEndpoint, apiKey, modelName) }) { Text("保存") }
            }
        }
    }
}
```

```kotlin
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(JournalTokens.Paper),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { EditorialTopBar(title = "数据管理", onBack = onNavigateBack) }
        item { EditorialSurfaceCard { Text("你的数据保存在本地") } }
        item {
            EditorialSurfaceCard {
                OutlinedTextField(value = s3Endpoint, onValueChange = { s3Endpoint = it }, label = { Text("S3 Endpoint") })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = s3Bucket, onValueChange = { s3Bucket = it }, label = { Text("Bucket 名称") })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = s3AccessKey, onValueChange = { s3AccessKey = it }, label = { Text("Access Key") })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = s3SecretKey, onValueChange = { s3SecretKey = it }, label = { Text("Secret Key") })
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        viewModel.saveS3Config(s3Endpoint, s3Bucket, s3AccessKey, s3SecretKey, autoSync)
                    }) { Text("保存") }
                    OutlinedButton(onClick = {}) { Text("导出") }
                }
            }
        }
    }
}
```

```kotlin
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(JournalTokens.Paper),
        contentPadding = PaddingValues(24.dp)
    ) {
        item { EditorialTopBar(title = "关于", onBack = onNavigateBack) }
        item {
            EditorialSurfaceCard {
                Text("呼吸之间，自见本心", style = MaterialTheme.typography.headlineLarge, fontStyle = FontStyle.Italic)
                Spacer(modifier = Modifier.height(12.dp))
                Text("AI Awareness Journal 的诞生，并非为了增加你的数字负担，而是希望在数字荒原中开辟一处数字避风港。", style = MaterialTheme.typography.bodyLarge)
            }
        }
        item {
            EditorialSurfaceCard {
                Text("隐私承诺", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text("所有文字与情感数据默认仅保存在本地设备。除非用户主动配置 AI API 或 S3 同步服务，否则数据不会离开这台设备。", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelSmall, color = JournalTokens.MutedInk)
            }
        }
    }
}
```

- [ ] **Step 5: Run settings-related unit tests**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.SettingsUiFormattersTest --tests com.aiawareness.diary.ui.screens.SettingsViewModelTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/components/SettingsSection.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/SettingsScreen.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/PersonalInfoScreen.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/AiConfigScreen.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/DataManagementScreen.kt \
  app/src/main/java/com/aiawareness/diary/ui/screens/AboutScreen.kt \
  app/src/test/java/com/aiawareness/diary/ui/screens/SettingsUiFormattersTest.kt
git commit -m "feat: rebuild settings family screens"
```

## Task 5: Full Verification and Shell Cleanup

**Files:**

- Modify: `app/src/main/java/com/aiawareness/diary/ui/MainActivity.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/navigation/NavGraph.kt`

- [ ] **Step 1: Run the full unit test suite**

Run: `./gradlew testDebugUnitTest`
Expected: PASS

- [ ] **Step 2: Build the debug app**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run the focused verification bundle for final evidence**

Run: `./gradlew testDebugUnitTest assembleDebug`
Expected: BUILD SUCCESSFUL with all tests passing

- [ ] **Step 4: Fix any navigation or shell regressions discovered during verification**

```kotlin
NavHost(
    navController = navController,
    startDestination = Screen.Input.route,
        modifier = modifier
) {
    composable(Screen.Input.route) {
        InputScreen(
            onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            onNavigateToAiConfig = { navController.navigate(Screen.AiConfig.route) }
        )
    }
    composable(Screen.Calendar.route) {
        CalendarScreen(
            onNavigateBack = { navController.popBackStack() },
            onDateSelected = { date -> navController.navigate(Screen.DiaryDetail.createRoute(date)) }
        )
    }
    composable(Screen.Settings.route) {
        SettingsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToPersonalInfo = { navController.navigate(Screen.PersonalInfo.route) },
            onNavigateToAiConfig = { navController.navigate(Screen.AiConfig.route) },
            onNavigateToDataManagement = { navController.navigate(Screen.DataManagement.route) },
            onNavigateToAbout = { navController.navigate(Screen.About.route) }
        )
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/aiawareness/diary/ui/MainActivity.kt \
  app/src/main/java/com/aiawareness/diary/ui/navigation/NavGraph.kt
git commit -m "chore: verify stitch native UI rebuild"
```
