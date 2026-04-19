# Launcher Icon Refinement Design

## Goal

Refine the Android launcher icon so the notebook-plus-spark foreground feels more precise and premium without changing the existing visual language.

## Current Context

- The launcher icon uses an adaptive icon setup.
- Background is a solid white shape in `ic_launcher_background.xml`.
- Foreground artwork is a single vector in `ic_launcher_foreground.xml`.
- The current foreground composition reads slightly large within the safe area.

## Decision

- Keep the white background unchanged.
- Keep the white background and spark language unchanged.
- Scale the entire foreground composition down to 80% of its current size.
- Scale around the adaptive icon center point `(54, 54)` so the foreground remains centered.
- Narrow the notebook body so it reads as a taller, more diary-like rectangle instead of a near-square card.
- Keep the notebook height roughly unchanged while reducing its width and shortening the inner white lines to match.

## Implementation

- Update `app/src/main/res/drawable/ic_launcher_foreground.xml`.
- Wrap the existing foreground paths in a vector `<group>`.
- Set `android:scaleX="0.8"` and `android:scaleY="0.8"`.
- Set `android:pivotX="54"` and `android:pivotY="54"`.
- Tighten the notebook shadow/body/fold path widths while leaving the spark path in place.
- Shorten the notebook's inner white lines so they fit the narrower body.

## Non-Goals

- No color changes.
- No spark redraw.
- No background change.
- No raster asset regeneration unless resource compilation reveals a problem.

## Verification

- Build resources successfully through Android Gradle build.
- Confirm the foreground XML remains valid and referenced by the existing adaptive icon files.
