# Launcher Icon Small Card Refinement Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refine the Android launcher icon so the dark background leads visually, the journal card is smaller, and only one spark remains.

**Architecture:** Keep the adaptive icon entrypoints unchanged and revise only the background and foreground drawables. Since this is drawable-resource work rather than app logic, verification is build-based instead of test-driven.

**Tech Stack:** Android resource XML, VectorDrawable, Gradle

---

### Task 1: Rebalance icon proportions and background tone

**Files:**
- Modify: `app/src/main/res/drawable/ic_launcher_background.xml`
- Modify: `app/src/main/res/drawable/ic_launcher_foreground.xml`

- [ ] **Step 1: Deepen the background palette**

Use a darker ink-blue gradient so the background reads first and supports the white card.

- [ ] **Step 2: Shrink and simplify the foreground**

Update the foreground to:
- reduce card footprint
- keep one folded corner
- keep two content lines
- remove the secondary spark and keep only one small spark

- [ ] **Step 3: Verify the APK still builds**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`
