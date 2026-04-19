# Home Brand Marks Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give the home title and input bar separate journal-shaped brand marks so both are more legible than the launcher icon reuse.

**Architecture:** Replace the single `homeBrandIconRes()` accessor with two accessors: a heavier brand mark for the home title and a lighter notebook mark for the input bar. Keep the rest of the home screen structure intact and implement the visual distinction with two dedicated vector drawables.

**Tech Stack:** Kotlin, Android VectorDrawable XML, JUnit4, Gradle

---

### Task 1: Lock the home brand asset split in tests

**Files:**
- Modify: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
@Test
fun homeBranding_usesDedicatedAssetsForTitleAndInput() {
    assertEquals(R.drawable.ic_home_brand_mark, homeTitleBrandIconRes())
    assertEquals(R.drawable.ic_home_input_mark, homeInputBrandIconRes())
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest`
Expected: FAIL because the new accessors do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```kotlin
internal fun homeTitleBrandIconRes(): Int = R.drawable.ic_home_brand_mark

internal fun homeInputBrandIconRes(): Int = R.drawable.ic_home_input_mark
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.aiawareness.diary.ui.screens.HomeScreenModelsTest`
Expected: PASS

### Task 2: Apply dedicated notebook marks to the home UI

**Files:**
- Create: `app/src/main/res/drawable/ic_home_brand_mark.xml`
- Create: `app/src/main/res/drawable/ic_home_input_mark.xml`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`

- [ ] **Step 1: Add a heavier title brand mark**

Create a vector drawable with:
- clear folded notebook silhouette
- one small spark
- bolder content lines

- [ ] **Step 2: Add a lighter input mark**

Create a vector drawable with:
- folded notebook silhouette
- two content lines
- no spark

- [ ] **Step 3: Wire each mark into the correct home UI slot**

Use the title mark in `HomeTopBar`, the input mark in `HomeInputBar`, and keep the empty state aligned with the title mark.

- [ ] **Step 4: Verify the build**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`
