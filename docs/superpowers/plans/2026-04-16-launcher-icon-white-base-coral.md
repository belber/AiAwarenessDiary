# Launcher Icon White Base Coral Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Update the Android launcher icon to a white base with a larger blue notebook and a coral spark.

**Architecture:** Keep the adaptive icon entrypoints unchanged and revise only the background and foreground drawables. The background becomes a clean white base, while the foreground grows slightly and shifts to the approved blue notebook plus coral spark palette.

**Tech Stack:** Android resource XML, VectorDrawable, Gradle

---

### Task 1: Update launcher icon resources

**Files:**
- Modify: `app/src/main/res/drawable/ic_launcher_background.xml`
- Modify: `app/src/main/res/drawable/ic_launcher_foreground.xml`

- [ ] **Step 1: Set the adaptive icon background to white**
- [ ] **Step 2: Enlarge the notebook and spark while preserving white breathing room**
- [ ] **Step 3: Change the spark accent to coral**
- [ ] **Step 4: Verify with `./gradlew assembleDebug`**
