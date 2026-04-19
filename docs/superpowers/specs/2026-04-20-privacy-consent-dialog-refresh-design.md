# Privacy Consent Dialog Refresh Design

## Goal

Refresh the startup privacy-consent dialog so it reads like a concise product explanation instead of a dense policy summary, while keeping the existing consent gate behavior intact.

## Context

The current dialog uses one long paragraph and a default `AlertDialog` layout. It communicates the legal requirement, but it does not clearly separate the three ideas users need to understand before consenting:

1. diary data stays local by default
2. AI diary generation only talks to the model service configured by the user
3. third-party monitoring is limited to app runtime diagnostics rather than diary content

The user wants the dialog to feel simpler, more structured, and more polished. Primary actions must remain only:

- agree and continue
- disagree and exit app

The full privacy policy should still be reachable from the dialog, but only as a secondary text link.

## Requirements

### Content

The dialog copy should emphasize:

- diary text, images, and settings are stored locally by default
- the app has no developer-operated content backend and does not collect or upload diary/user content to a developer server
- AI diary generation uses the user’s self-configured AI model service, and request data is not shared with the developer or unrelated third parties
- third-party monitoring such as Aliyun APM is used only after consent, and only for crash/performance/network/runtime diagnostics rather than diary content

The body copy should be broken into short sections rather than a single paragraph.

### Interaction

- the dialog remains mandatory on first launch until the user chooses agree or disagree
- tap outside and back press remain disabled
- primary button: `同意并继续`
- secondary button: `不同意并退出`
- keep a low-emphasis text link for opening the full privacy policy
- disagree continues to exit the app using the existing flow

### Visual Design

The dialog should stay aligned with the existing paper/editorial visual language, but feel more intentionally designed than the stock `AlertDialog` body.

The preferred structure is:

1. title
2. short intro line
3. three small grouped content sections
4. low-emphasis privacy-policy link
5. two action buttons

Each section should be short and scannable, with enough spacing to create hierarchy without making the dialog heavy.

## Proposed Copy Structure

### Title

Use a shorter product-facing title:

- `隐私说明`

This reads less like a legal document header while still remaining clear.

### Intro

One short sentence explaining why the dialog appears:

- `继续使用前，请先确认数据如何保存、AI 如何调用，以及哪些运行信息会用于改进 APP。`

### Section 1

Heading:

- `内容默认保存在本地`

Body:

- `你的日记、图片和设置默认只保存在本机。应用没有开发者自建服务端，不会收集或上传你的日记等用户内容。`

### Section 2

Heading:

- `AI 只使用你自己配置的服务`

Body:

- `生成 AI 日记时，内容只会发送到你自己配置的 AI 大模型服务，不会提供给开发者或其他无关方。`

### Section 3

Heading:

- `仅采集运行诊断信息`

Body:

- `在你同意后，应用才会启用阿里云移动监控等第三方能力，用于崩溃、性能、网络等运行情况分析，不用于收集你的日记内容。`

### Footer Hint

- `查看完整隐私政策`

This remains a small text link below the sections and above the action buttons.

## Implementation Approach

Keep the existing `PrivacyConsentDialog` entry point and callback contract, but replace the stock single-body layout with a more structured Compose body.

Implementation should:

- keep `AlertDialog` if the existing shell is sufficient
- replace the current single `Text` body with:
  - intro text
  - reusable section rows/cards for three privacy points
  - low-emphasis text link
- update `strings.xml` so the dialog copy is split into dedicated strings instead of one long message

No navigation flow changes are required.

## Testing

Update or add tests so they verify:

- the dialog exposes the new title and the three section headings/bodies
- the dialog keeps only the two main action buttons
- the policy link text is still present
- disagree semantics still map to the existing exit path

## Non-Goals

- rewriting the full privacy policy document
- changing the underlying consent persistence logic
- changing the app exit behavior after disagreement
- adding more buttons or extra decision states
