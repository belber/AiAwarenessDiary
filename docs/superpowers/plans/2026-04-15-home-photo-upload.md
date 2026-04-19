# Home Photo Upload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose the existing record photo upload capability in the home input bar.

**Architecture:** Reuse the existing `MainViewModel.setPendingPhoto(...)` and `saveRecord(...)` flow. Add a gallery picker launcher in `InputScreen`, expose a small attach-photo button in `HomeInputBar`, and show a lightweight pending-photo state label before send.

**Tech Stack:** Kotlin, Jetpack Compose, Activity Result API, JUnit4, Gradle

---

### Task 1: Lock copy and state hooks with tests

**Files:**
- Modify: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`

- [ ] **Step 1: Write the failing test**
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Add minimal helper functions for attach button and pending state copy**
- [ ] **Step 4: Run test to verify it passes**

### Task 2: Wire the photo picker into the home input bar

**Files:**
- Modify: `app/src/main/java/com/aiawareness/diary/ui/screens/InputScreen.kt`
- Test: `app/src/test/java/com/aiawareness/diary/ui/screens/HomeScreenModelsTest.kt`

- [ ] **Step 1: Add `OpenDocument(image/*)` launcher in `InputScreen`**
- [ ] **Step 2: Add attach-photo icon button to `HomeInputBar`**
- [ ] **Step 3: Show a lightweight pending-photo status label when a photo is selected**
- [ ] **Step 4: Re-run targeted tests**
- [ ] **Step 5: Run `./gradlew assembleDebug`**
