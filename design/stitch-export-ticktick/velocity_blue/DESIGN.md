# Design System Document: The Precision Architect

## 1. Overview & Creative North Star

### Creative North Star: "The Digital Curator"
This design system is engineered for high-velocity productivity through the lens of editorial precision. Unlike standard "utility" apps that feel cluttered and industrial, this system treats tasks and data as curated content. It balances a **vibrant, dependable primary blue** with an expansive, breathable **light gray landscape**.

The "Digital Curator" breaks the traditional grid-block template by utilizing intentional white space, layered "glass" surfaces, and a hierarchy driven by tonal shifts rather than rigid lines. It is fast, reliable, and highly organized, designed for users who require clarity in moments of high cognitive load.

---

## 2. Colors

### Palette Strategy
The color system moves beyond simple hex codes to define a "tonal atmosphere." It uses a logic of **Low-Contrast Neutrals** paired with an **Authoritative Primary Blue**.

| Token | Hex | Role |
| :--- | :--- | :--- |
| `primary` | `#1e52cc` | Action driver, brand authority, high-priority states. |
| `surface` | `#f8f9fb` | The base canvas; clean, expansive, and calming. |
| `surface-container-low` | `#f3f4f6` | Secondary grouping; subtle background differentiation. |
| `surface-container-highest` | `#e1e2e4` | Active states or nested information blocks. |
| `on-surface` | `#191c1e` | High-readability content and primary headings. |
| `on-surface-variant` | `#434654` | Secondary metadata and helper text. |

### The "No-Line" Rule
To achieve a premium, custom feel, **1px solid borders are prohibited for sectioning.** Boundaries must be defined solely through background color shifts. For example, a card (using `surface_container_lowest`) should sit on a background (using `surface`) to create a natural edge. This mimics physical stationery and prevents the UI from looking "boxed in."

### The Glass & Gradient Rule
Floating elements (such as bottom sheets or navigation bars) should utilize **Glassmorphism**. Apply a semi-transparent `surface` color with a 20px-30px backdrop blur. For primary CTAs, use a subtle linear gradient from `primary` to `primary_container` (135° angle) to give the button a tactile, "light-filled" presence.

---

## 3. Typography

The system utilizes **Inter** for its neutral, highly legible geometric properties. The hierarchy is designed to feel like a high-end magazine: large, clear headers paired with tight, functional labels.

*   **Display/Headline Scale:** Used for page titles and large data points (e.g., countdowns). These should have a slight negative letter-spacing (-0.02em) to feel "tighter" and more custom.
*   **Title/Body Scale:** Optimized for task names. `title-md` is the workhorse for list items, providing a strong anchor for the user's eye.
*   **Label Scale:** Used for metadata (e.g., "Days Remaining"). These should always be paired with `on-surface-variant` to maintain a clear information hierarchy.

| Level | Size | Weight | Usage |
| :--- | :--- | :--- | :--- |
| `headline-sm` | 1.5rem | 600 | Page headers and primary views. |
| `title-md` | 1.125rem | 500 | Main task titles / List items. |
| `body-md` | 0.875rem | 400 | Descriptions and secondary info. |
| `label-md` | 0.75rem | 600 | Tags, metadata, and tiny action text. |

---

## 4. Elevation & Depth

### The Layering Principle
Depth is achieved via **Tonal Layering** rather than drop shadows.
1.  **Level 0 (Base):** `surface` (#f8f9fb).
2.  **Level 1 (Sections):** `surface-container-low` (#f3f4f6).
3.  **Level 2 (Cards):** `surface-container-lowest` (#ffffff).

### Ambient Shadows
Shadows should only be used for "floating" interactive components (like the Floating Action Button).
*   **Values:** `Y: 8px, Blur: 24px, Opacity: 6%`.
*   **Shadow Color:** Use a tinted version of `primary` or `on-surface` (e.g., `#1e52cc10`) to ensure the shadow feels like part of the environment, not a black smudge.

### The "Ghost Border"
If a container requires an edge for accessibility (e.g., on very bright displays), use a **Ghost Border**: `outline-variant` at 15% opacity. Never use a 100% opaque border.

---

## 5. Components

### Buttons & Inputs
*   **Primary FAB:** A circular container using the `primary` to `primary_container` gradient. Ensure the "+" icon is `on_primary` (#ffffff).
*   **Inputs:** Forgo the traditional box. Use a clear `surface-container-low` background with a `primary` focus indicator—a vertical 3px bar on the left side of the input field.

### Cards & Lists
*   **Radius:** Standardized at `md` (0.75rem / 12px) for a soft but professional look.
*   **List Items:** Never use horizontal divider lines. Separate items using 8px of vertical white space or a subtle hover state shift to `surface-container-high`.
*   **Selection State:** When an item is selected (e.g., a sidebar menu item), use a semi-transparent `primary_fixed` background with rounded corners.

### Chips & Tags
*   **Action Chips:** Small, pill-shaped (`full` roundedness) using `surface-container-highest` for the background and `on-surface` for the text.

---

## 6. Do’s and Don’ts

### Do
*   **DO** use whitespace as a functional tool to group related items.
*   **DO** use `surface-tint` to subtly color-code different categories of tasks.
*   **DO** ensure that the most important action on any screen uses the `primary` blue.
*   **DO** use backdrop blurs on overlays to maintain the user's context of the screen below.

### Don't
*   **DON'T** use black (#000000) for text; use `on-surface` (#191c1e) to keep the look premium and soft.
*   **DON'T** use 1px solid dividers between list items; use spacing or tonal shifts.
*   **DON'T** use sharp 90-degree corners; everything must adhere to the `md` (12px) or `lg` (16px) radius scale.
*   **DON'T** clutter the UI with icons; only use them when they provide immediate cognitive recognition.