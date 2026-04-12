# Stitch Export Native UI Design

## Overview

This spec defines how to rebuild the Android app UI so it matches the exported Stitch prototypes in `design/stitch-export` while preserving the existing real data flow, navigation, Room storage, DataStore-backed settings, and AI generation behavior.

The goal is not to ship static mock screens. The goal is to make the current Compose app look and feel like the Stitch prototype set, with mobile-appropriate adaptation where the exported HTML was clearly designed for wider screens.

## Source Material

### Prototype directories

- `design/stitch-export/home`
- `design/stitch-export/review`
- `design/stitch-export/settings`
- `design/stitch-export/personal_info`
- `design/stitch-export/ai_ai_config`
- `design/stitch-export/data_management`
- `design/stitch-export/about`

Each prototype directory contains:

- `screen.png`: visual reference screenshot
- `code.html`: Stitch-generated layout and styling reference

### Design system references

- `design/stitch-export/zen_paper_awareness/DESIGN.md`
- `design/stitch-export/serene_sky/DESIGN.md`
- `design/stitch-export/lavender_sanctuary/DESIGN.md`

Implementation should primarily follow `zen_paper_awareness/DESIGN.md`. The other two references may inform polish decisions, but they must not override the warm paper, editorial, low-contrast visual language used by the selected page prototypes.

## Scope

### In scope

- Rework the app theme tokens to match the Stitch export palette and typography
- Rebuild all exported pages in native Compose
- Preserve current navigation graph and route structure
- Preserve real data binding for records, diary content, settings, and profile info
- Adapt wide desktop-like layouts into strong mobile-first single-column Android layouts
- Keep existing feature behavior where it already works

### Out of scope

- Replacing Room, DataStore, Hilt, or Retrofit architecture
- Building new backend capabilities not already present in the app
- Introducing fake persistence for prototype-only settings
- Pixel-identical parity with HTML rendering on every device
- Large unrelated refactors in data or domain layers

## Current App Mapping

The Stitch prototypes map onto the existing Compose screens as follows:

- `home/code.html` -> `InputScreen`
- `review/code.html` -> `CalendarScreen`
- selected date review flow -> `DiaryDetailScreen`
- `settings/code.html` -> `SettingsScreen`
- `personal_info/code.html` -> `PersonalInfoScreen`
- `ai_ai_config/code.html` -> `AiConfigScreen`
- `data_management/code.html` -> `DataManagementScreen`
- `about/code.html` -> `AboutScreen`

The navigation graph remains the same:

- `input`
- `calendar`
- `diary_detail/{date}`
- `settings`
- `personal_info`
- `ai_config`
- `data_management`
- `about`

## Visual Direction

### Creative direction

The app should feel like a calm editorial journal rather than a utility dashboard:

- warm paper background
- tonal separation instead of hard borders
- soft, pill-shaped controls
- serif headlines paired with clean sans body text
- spacious vertical rhythm
- subtle decorative glows and layered surfaces

### Theme rules

- Prefer tonal layering over visible outlines
- Avoid high-contrast Material default surfaces
- Use Newsreader for reflective and editorial headings
- Use Plus Jakarta Sans for controls, labels, body, and metadata
- Use rounded corners generously
- Keep shadows soft, broad, and low-opacity
- Preserve calmness under interaction; no loud ripple-heavy visual language

### Mobile adaptation rule

When a prototype uses desktop-like width or multi-column composition:

- preserve content hierarchy
- preserve emphasis and spacing rhythm
- preserve card ordering and semantic grouping
- collapse into a single-column mobile layout where needed

Do not mechanically copy web width assumptions into Android phone screens.

## Theme System Changes

The current theme is too close to generic Material defaults. It must be replaced with a dedicated design token layer based on the Stitch export.

### Color system

Primary colors should come from the prototype set:

- background / paper: `#FBF9F4`
- primary sage: `#546356`
- primary dim: `#48574B`
- primary container: `#D6E7D7`
- secondary clay: `#855245`
- secondary container: `#FFDAD2`
- tertiary stone: `#635E5B`
- low surface: `#F5F4ED`
- high surface: `#E9E8E1`
- highest surface: `#E3E3DB`
- ink text: `#31332E`
- muted text: `#5E6059`

The Compose theme must expose these values consistently so screens stop hardcoding conflicting greens or default Material colors.

### Typography

Typography should formalize:

- Newsreader for editorial headers, section titles, and reflective content
- Plus Jakarta Sans for body text, labels, controls, metadata, and settings content

The type scale must support:

- large editorial screen titles
- compact uppercase metadata labels
- readable body copy with generous line height
- italic serif moments for reflective text

### Shape and spacing

Introduce shared values for:

- capsule controls
- large soft cards
- standard screen horizontal padding
- sectional spacing
- elevated paper panels

These tokens should be reused across all page implementations to keep the exported prototype language coherent.

## Screen Specifications

### 1. Home / InputScreen

#### Prototype reference

- `design/stitch-export/home/code.html`

#### Purpose

This is the main daily capture screen. It combines profile context, daily record progress, a time-grouped record stream, AI diary generation entry, and a persistent bottom input composer.

#### Required structure

- translucent top bar with avatar, nickname, current date, review entry, and settings entry
- daily progress summary with dots showing recorded count
- record stream grouped by time-of-day sections
- soft editorial section headers such as `清晨`, `上午`, `下午`, `傍晚`, `夜晚`
- floating AI diary generation card near the end of the stream
- fixed bottom input bar with mic affordance, text field, and send action

#### Data binding

- nickname and avatar from `userSettings`
- current date from existing date utilities
- records from `MainViewModel.uiState.records`
- time grouping from existing `buildHomeTimelineSections()`
- AI action card from `hasApiKey`, `isGeneratingDiary`, and current `diary`

#### Behavior

- send action must still call `saveRecord()`
- edit and delete flows must remain functional
- AI generation button must still call `generateDiary()`
- when API config is missing, AI section should still route users to AI config instead of pretending the feature is ready

#### Visual adaptation notes

- keep the fixed bottom composer
- preserve the centered progress region and calm timeline rhythm
- use icon accents per record without turning cards into noisy utility rows

### 2. Review / CalendarScreen

#### Prototype reference

- `design/stitch-export/review/code.html`

#### Purpose

This screen is the review entry point: first a rich monthly calendar, then record and AI content previews for the currently relevant date context.

#### Required structure

- editorial top bar with back navigation and title
- large monthly calendar card
- day cells with subtle markers for dates that have records
- selected day state with tonal highlight
- below the calendar, content zones for original records and AI-generated reflection

#### Data binding

- current month and year from `MainUiState`
- `datesWithRecords` for markers
- month navigation through existing `updateMonth()` and `loadDatesWithRecords()`

#### Mobile adaptation

The web prototype shows a wider two-column composition after the calendar. On mobile:

- keep the calendar first
- stack “original records” and “AI diary/summary” vertically
- maintain their relative importance

#### Navigation

- selecting a date with records continues to open `DiaryDetailScreen`

### 3. Diary Detail / DiaryDetailScreen

#### Prototype basis

There is no separate Stitch export for this page. It must inherit the `review` visual language and act as the full-day detail page.

#### Purpose

Show the selected day’s raw records, AI diary, and AI summary in a page consistent with the review prototype.

#### Required structure

- back navigation and date title
- original records section
- AI diary section
- AI summary / insight section
- empty states where no diary exists yet

#### Data binding

- load records by selected date
- load saved diary by selected date
- do not invent AI content if it has not been generated

#### Behavior

- if records exist but AI content does not, the screen may show a generation prompt or empty-state messaging
- it must remain visually consistent with the prototype family

### 4. Settings / SettingsScreen

#### Prototype reference

- `design/stitch-export/settings/code.html`

#### Purpose

This is a grouped entry page for account, AI, and data/privacy settings.

#### Required structure

- editorial top bar
- large personal summary area with avatar, nickname, and signature-like line
- grouped settings sections with uppercase metadata headers
- list items for personal info, AI config, diary generation time, data management, and about
- footer note matching the quiet branded tone of the prototype

#### Behavior

- items must still navigate to the existing detail routes
- diary generation time row should reflect actual stored values where possible

#### Data binding

- nickname and avatar from `SettingsViewModel`
- generation time from stored user settings

#### Constraint

Do not claim support for settings fields that do not actually exist in persistence. If a prototype shows richer copy than current storage supports, keep the look but keep behavior honest.

### 5. Personal Info / PersonalInfoScreen

#### Prototype reference

- `design/stitch-export/personal_info/code.html`

#### Purpose

Allow users to view and edit their identity-facing profile details.

#### Required structure

- editorial top bar
- large portrait treatment
- profile name and signature presentation
- editable content blocks styled like the prototype
- calm save feedback

#### Data binding

- nickname from settings
- avatar path from settings
- image picker remains real

#### Constraint

If the prototype shows fields not currently persisted, prefer a visually accurate but behaviorally honest representation. Do not silently fabricate storage support.

### 6. AI Config / AiConfigScreen

#### Prototype reference

- `design/stitch-export/ai_ai_config/code.html`

#### Purpose

Present AI model configuration as a polished editorial settings form rather than a default Material form.

#### Required structure

- editorial top bar
- grouped setting fields
- explanatory copy
- save affordance integrated into the visual system
- persistent feedback that changes are saved

#### Data binding

- endpoint, API key, and model name remain bound to `SettingsViewModel`
- saving still uses `saveApiConfig()`

#### Constraint

If the prototype suggests additional tuning controls such as empathy or tone depth that do not exist in the data layer, those controls must not be misleadingly interactive unless storage support is added deliberately.

### 7. Data Management / DataManagementScreen

#### Prototype reference

- `design/stitch-export/data_management/code.html`

#### Purpose

Present local-first storage and optional S3 sync as a polished privacy-focused settings page.

#### Required structure

- editorial top bar
- local-first storage explanation card
- grouped S3 configuration fields
- auto-sync toggle row
- action row for save and export

#### Data binding

- endpoint, bucket, access key, secret key, and auto-sync from `SettingsViewModel`
- save action remains real

#### Constraint

The export action must remain honest. If true export behavior is not implemented, the UI may present the action visually but must not falsely imply a finished export pipeline.

### 8. About / AboutScreen

#### Prototype reference

- `design/stitch-export/about/code.html`

#### Purpose

Provide a brand and philosophy page consistent with the prototype’s sanctuary framing.

#### Required structure

- editorial top bar
- large visual anchor area
- philosophy section
- privacy section
- soft closing footer with version information

#### Data binding

- version display should use actual app version data where practical, or a clearly maintained constant

## Shared Component Strategy

To avoid seven isolated page rewrites, implementation should introduce a focused reusable UI layer.

### Candidate shared components

- editorial top app bar
- capsule action button
- soft tonal card
- grouped settings section
- settings list row
- diary / insight panel
- home timeline card
- monthly calendar day cell
- decorative background glow or ambient shape

### Non-goal

Do not over-abstract everything into a design system framework. Extract only what is genuinely reused by multiple screens.

## Data and State Strategy

### Preserve existing architecture

Keep using:

- `MainViewModel` for home, calendar, and review-related flows
- `SettingsViewModel` for settings and profile-related flows
- repositories and persistence exactly as they are unless a UI need reveals a small missing field that is cheap and justified to add

### Acceptable small data-layer additions

Small additions are acceptable only if they directly support already-agreed UI behavior and remain low risk. Example candidates:

- storing a profile tagline if the user-facing profile screen clearly needs an editable field and the current settings model is missing it
- exposing diary generation time in a more UI-friendly format if already persisted

Such additions must be minimal and justified by actual screen behavior, not by speculative future settings.

## Testing Strategy

### Required verification

- app compiles
- existing unit tests continue to pass or are updated intentionally
- any new pure mapping logic receives unit tests
- navigation between all implemented pages works

### Reasonable test additions

- tests for any new date or UI mapping helpers
- tests for any new state formatting functions

### Not required in this phase

- screenshot-test infrastructure
- broad instrumentation expansion unrelated to this redesign

## Acceptance Criteria

Implementation is acceptable when all of the following are true:

- every exported Stitch page has a corresponding native Compose page in the app
- the app uses a consistent paper-like editorial theme matching the chosen prototype direction
- phone layouts preserve the Stitch hierarchy and mood without forcing desktop width patterns
- home, review, settings, profile, AI config, data management, and about screens all reflect the prototype family
- records can still be created, edited, and deleted
- AI config still saves real settings
- S3 config still saves real settings
- date review flow still works with real stored data
- UI never misrepresents unfinished backend capabilities as complete

## Risks

- The existing UI already contains some page-specific colors and inline styling, so redesign work may require consolidating scattered visual constants before parity is achieved.
- The current review prototype assumes richer diary presentation than some dates may actually have, so empty states must be handled carefully.
- Some prototype details imply settings depth that may exceed current persistence; these must be handled without deceptive interactions.

## Implementation Direction

The redesign should be implemented as a UI-focused rewrite over the existing architecture:

1. rebuild theme tokens
2. extract minimal shared visual components
3. rework `InputScreen`
4. rework `CalendarScreen` and `DiaryDetailScreen`
5. rework `SettingsScreen` and its detail pages
6. verify data flows remain intact

This keeps the work aligned with the approved requirement: full prototype-based page implementation on Android without discarding the current working data and navigation foundation.
