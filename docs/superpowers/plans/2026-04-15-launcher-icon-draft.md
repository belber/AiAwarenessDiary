# Launcher Icon Draft Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a first Android adaptive launcher icon draft for Innote using the approved folded-card concept.

**Architecture:** Keep the existing adaptive icon entrypoints and replace only the drawable resources behind them. The background becomes a warm gray-blue gradient, while the foreground becomes a simple folded white card with two content lines and a small spark accent.

**Tech Stack:** Android resource XML, VectorDrawable, Gradle

---

### Task 1: Replace icon drawables with the approved concept

**Files:**
- Modify: `app/src/main/res/drawable/ic_launcher_background.xml`
- Modify: `app/src/main/res/drawable/ic_launcher_foreground.xml`

- [ ] **Step 1: Update the adaptive icon background**

Use a linear gradient in `ic_launcher_background.xml` to shift from lighter warm blue to deeper editorial blue.

- [ ] **Step 2: Update the adaptive icon foreground**

Replace the current symbol with:
- one soft shadow
- one rounded white folded card
- two short content lines
- one small four-point spark

- [ ] **Step 3: Verify the resources compile**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`
