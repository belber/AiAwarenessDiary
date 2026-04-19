# Review AI Diary Pattern Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a subtle sage-toned background pattern to the Review page AI diary card without changing the summary or raw record cards.

**Architecture:** Keep the existing `ReviewInsightBlock` component and gate the decorative layer behind a small tone-based helper in `ReviewScreenModels`. The UI change stays local to `CalendarScreen.kt`, while the behavior contract is covered by a focused unit test.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit4, Gradle

---

### Task 1: Lock the pattern rule in tests

**Files:**
- Modify: `app/src/test/java/com/aiawareness/diary/ui/screens/ReviewScreenModelsTest.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/ReviewScreenModels.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun reviewInsightPattern_isEnabledOnlyForAiDiaryTone() {
    assertTrue(reviewInsightHasPattern(ReviewAccentTone.Sage))
    assertFalse(reviewInsightHasPattern(ReviewAccentTone.Clay))
    assertFalse(reviewInsightHasPattern(ReviewAccentTone.Stone))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.ReviewScreenModelsTest`
Expected: FAIL because `reviewInsightHasPattern` does not exist yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
fun reviewInsightHasPattern(tone: ReviewAccentTone): Boolean =
    tone == ReviewAccentTone.Sage
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.ReviewScreenModelsTest`
Expected: PASS

### Task 2: Render the AI diary card pattern

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/CalendarScreen.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/ReviewScreenModelsTest.kt`

- [ ] **Step 1: Add the decorative background layer**

```kotlin
Box(modifier = Modifier.fillMaxWidth()) {
    if (reviewInsightHasPattern(tone)) {
        ReviewInsightPattern(
            modifier = Modifier.matchParentSize()
        )
    }

    Column(modifier = Modifier.padding(14.dp)) {
        Text(...)
    }
}
```

- [ ] **Step 2: Implement a subtle sage gradient and fine diagonal lines**

```kotlin
private fun Modifier.reviewInsightPattern(): Modifier = drawWithCache {
    onDrawBehind {
        drawRect(...)
        repeat(12) { ... drawLine(...) }
    }
}
```

- [ ] **Step 3: Run the targeted test again**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.ReviewScreenModelsTest`
Expected: PASS
