# Privacy Consent And APM Integration Design

## Goal

Add a privacy-consent gate before enabling Aliyun APM, expose the privacy policy from the app, and keep the policy content maintainable through a hosted GitHub Pages document with an in-app fallback.

## Current Context

- The app is a single-activity Android app with Compose entry from `MainActivity`.
- `DiaryApplication` already exists and is the correct place for process-wide SDK configuration.
- The app already uses DataStore-backed preferences and has an About screen that can host long-term legal links.
- The app can keep user content locally, and can optionally send user-selected content to third-party OpenAI-compatible model services and S3-compatible object storage.
- Aliyun APM will be integrated for full monitoring capability, but should only be started after privacy consent.

## Requirements

### Functional

- Configure Aliyun APM during app startup with `Apm.preStart(...)`.
- Do not call `Apm.start()` before the user accepts the privacy policy.
- Show a startup privacy dialog on first launch.
- The dialog must provide:
  - `Agree and continue`
  - `Disagree and exit`
  - A clickable `Privacy Policy` entry
- Tapping `Privacy Policy` must open an in-app page.
- The in-app page should load a hosted GitHub Pages privacy policy URL through WebView.
- If the hosted page cannot be loaded, show bundled local fallback policy text in the app.
- Persist consent locally so the dialog is not shown again after acceptance.
- Add a `Privacy Policy` entry to the About screen.
- Keep the Settings screen unchanged.

### Compliance Content

The policy text must describe:

- Local storage of diary content and related files
- Optional user-configured transmission to OpenAI-compatible model services
- Optional user-configured transmission to S3-compatible object storage
- The fact that the app has no developer-operated backend, but the client app can still transmit user-selected data to third-party services configured by the user
- Aliyun APM collection for crash, performance, network, remote log, and memory monitoring
- The kinds of user data that may be involved:
  - diary text
  - selected images
  - prompts and model request content
  - AI-generated content
  - configured third-party endpoint and key material
  - import/export backup files

### Technical

- Add the Aliyun repository to Gradle repository resolution.
- Apply the Aliyun APM Gradle plugin version `3.1.0`.
- Add the SDK dependency `com.aliyun.ams:alicloud-apm:2.8.0`.
- Enable `android.enableJetifier=true`.
- Add required ProGuard keep rules for APM classes.
- Leave `Apm.setUserId()` and `Apm.setUserNick()` unimplemented for now because the app has no login system.

## Design

### APM Lifecycle

`DiaryApplication.onCreate()` will call `Apm.preStart(...)` with:

- application instance
- `AppKey`
- `AppSecret`
- `AppRsaSecret`
- all monitoring components:
  - crash
  - memory
  - remote log
  - performance

`Apm.start()` will be gated behind a persisted privacy-consent flag.

Behavior:

- On cold start, `Apm.preStart(...)` always runs.
- If consent is already granted, `Apm.start()` runs during the startup flow.
- If consent is not granted, APM remains configured but not started until acceptance.

Debug logging for APM will be enabled only for debug builds.

### Consent State

Add a new DataStore preference key:

- `privacy_policy_accepted: Boolean`

This state is the single source of truth for:

- whether the startup dialog appears
- whether `Apm.start()` may be invoked automatically

The write flow must be:

1. Persist `privacy_policy_accepted = true`
2. Call `Apm.start()`
3. Continue normal app usage

If the user chooses not to accept, the app finishes immediately.

### Startup Flow

The app startup flow will be:

1. `DiaryApplication` executes `Apm.preStart(...)`
2. `MainActivity` or the root Compose host reads consent state
3. If not accepted, show a blocking startup privacy dialog
4. If accepted, continue to main content and start APM

The dialog must block the normal experience until the user chooses one of the two actions.

### Privacy Policy UI

Create one reusable privacy policy screen reachable from:

- startup dialog
- About screen

This screen will:

- show a top app bar
- host a WebView for the GitHub Pages URL
- detect page-load failure
- switch to a local bundled fallback text when remote content fails

The fallback text is not just an error note. It is a usable privacy-policy copy that preserves minimum access to the policy even when offline or blocked.

### About Screen Entry

Add a `Privacy Policy` entry to the About screen.

Selection behavior:

- navigate to the same in-app privacy policy screen used by the startup dialog

No privacy entry will be added to Settings.

### Policy Source Strategy

Maintain two versions of the policy:

1. Repository source document for editing and version control
2. GitHub Pages published URL for primary runtime display

The repository source document should live under:

- `docs/legal/privacy-policy.md`

The app should also bundle a fallback text resource derived from the same source so the legal text does not drift across versions.

## File-Level Plan

- `settings.gradle.kts`
  - add Aliyun Maven repository in `pluginManagement` and `dependencyResolutionManagement`
- `app/build.gradle.kts`
  - apply APM plugin
  - add APM dependency
  - add WebView-related dependencies only if currently missing
- `gradle.properties`
  - add `android.enableJetifier=true`
- `app/proguard-rules.pro`
  - add APM keep rule
- `app/src/main/java/.../DiaryApplication.kt`
  - add `Apm.preStart(...)`
- existing DataStore preference files
  - add consent flag read/write support
- startup UI files
  - add first-launch privacy dialog behavior
- navigation files
  - add privacy policy destination
- About screen
  - add privacy-policy entry
- new privacy policy screen
  - WebView + fallback text handling
- app resources
  - privacy-policy fallback text and strings
- `docs/legal/privacy-policy.md`
  - formal policy text source

## Error Handling

- If WebView fails to load the GitHub Pages URL, display bundled fallback content immediately.
- If APM startup is attempted before `preStart`, treat that as a programming error and avoid introducing any alternate path that could trigger it.
- If consent persistence fails, do not start APM and keep the user in the consent flow.

## Testing Strategy

### Manual

- Fresh install shows the startup privacy dialog.
- Choosing `Disagree and exit` closes the app.
- Choosing `Privacy Policy` opens the in-app policy screen.
- With network available, the policy screen loads the GitHub Pages page.
- With network unavailable, the policy screen shows bundled fallback content.
- After acceptance, relaunching the app does not show the dialog again.
- After acceptance, APM debug logs appear in debug builds.
- The About screen opens the same privacy policy screen.

### Automated

- Unit test consent preference read/write logic if that logic is extracted behind an existing repository or settings abstraction.
- UI or model-level tests for startup state decisions if the current architecture already supports those tests without excessive harness work.

## Decisions

- Use a blocking startup dialog instead of a dedicated onboarding page.
- Put the long-term policy entry in About, not Settings.
- Use in-app WebView for the hosted policy, not an external browser.
- Bundle local fallback policy text to avoid network-only access.
- Defer user identity binding to APM until the app gains a real user-account concept.

## Out Of Scope

- Drafting a separate standalone user agreement
- Introducing a developer-operated backend
- Multi-environment APM configuration
- User account identity mapping into APM
- Any consent flow for third-party model/storage usage beyond the privacy-policy disclosure already described here
