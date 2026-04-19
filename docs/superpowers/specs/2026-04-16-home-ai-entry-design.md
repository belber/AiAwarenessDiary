# Home AI Entry Design

## Goal

Expose AI diary generation on the home screen so first-time users can discover both manual generation and AI setup without needing to infer the review flow.

## Decision

- Add a lightweight AI entry card above the home input bar.
- Show it at all times.
- Keep review-page AI actions unchanged.

## States

- No records today: show a muted CTA that explains users need to record first.
- Has records but no AI config: CTA navigates to AI config.
- Has records and AI config, no diary yet: CTA generates today's AI diary.
- Has records and AI config, diary already exists: CTA regenerates today's AI diary.
- Generating: CTA shows loading copy and is not clickable.

## Interaction

- No records: tap shows the existing “先记录今天的内容，再生成 AI 日记” guidance.
- No AI config: tap navigates to AI config.
- Ready to generate: tap triggers `generateDiary()`.

## Supporting Copy

- Keep a small helper line under the CTA to explain that AI diaries can also be generated automatically at the configured daily time.
