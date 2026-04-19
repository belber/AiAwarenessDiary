# Design System Document: The Ethereal Editorial

## 1. Overview & Creative North Star
**Creative North Star: "The Digital Sanctuary"**
This design system is a departure from the rigid, boxy constraints of standard SaaS interfaces. It is built on the philosophy of "Quiet Luxury"—where the interface recedes to let content breathe. By combining the high-end editorial feel of serif typography with a soft, monochromatic purple palette, we create an environment that feels less like a tool and more like a curated experience.

To break the "template" look, designers must embrace **intentional asymmetry** and **expansive white space**. Elements should not always align to a predictable grid; instead, use overlapping layers and staggered content blocks to create a sense of organic movement and rhythm.

---

## 2. Colors & Surface Philosophy
The palette is rooted in a serene lavender spectrum, designed to minimize cognitive load and evoke a sense of stillness.

*   **Primary (#5e5b81):** Used for grounding elements and key actions.
*   **Tertiary (#e6e6fa):** The heart of the system. This light lavender provides the "air" and signature glow.
*   **Surface Hierarchy:** We utilize a "Low-Contrast" approach to depth. 
    *   **The "No-Line" Rule:** 1px solid borders are strictly prohibited for sectioning. To separate content, use background color shifts (e.g., a `surface_container_low` card sitting on a `surface` background).
    *   **Surface Nesting:** Treat the UI as physical layers. An inner container should always be one tier higher or lower than its parent (e.g., a `surface_container_lowest` search bar nested within a `surface_container_high` header).
*   **The "Glass & Gradient" Rule:** For floating elements or primary CTAs, use a subtle linear gradient from `primary` to `primary_container` at a 135-degree angle. This adds a "soul" to the UI that flat hex codes cannot provide.
*   **Glassmorphism:** Use semi-transparent `surface_container_lowest` with a `20px` backdrop-blur for navigation bars or floating menus to maintain a sense of lightness and depth.

---

## 3. Typography
The typographic system creates a tension between the traditional and the modern.

*   **Display & Headlines (Newsreader):** Our "Editorial Voice." This serif font should be used for all storytelling elements. It should feel oversized and authoritative. Use `display-lg` (3.5rem) with tighter letter-spacing for a high-fashion, masthead feel.
*   **Body & Labels (Manrope):** Our "Functional Voice." A clean, geometric sans-serif that ensures maximum readability. 
*   **Hierarchy Note:** Always pair a large `display-md` headline with a `body-md` description that has generous line-height (1.6) to create that "premium magazine" aesthetic.

---

## 4. Elevation & Depth
In this design system, shadows are light, and structure is felt rather than seen.

*   **The Layering Principle:** Depth is achieved by stacking surface tokens. 
    *   Base: `surface`
    *   Secondary Content: `surface_container_low`
    *   Elevated Cards: `surface_container_lowest`
*   **Ambient Shadows:** If a floating effect is required (e.g., for a modal), use a very large blur (32px-64px) at 6% opacity. The shadow color should be a tinted purple (`#47456a`) rather than pure black to keep the light airy feel.
*   **The "Ghost Border" Fallback:** If accessibility requires a container boundary, use the `outline_variant` token at **15% opacity**. It should be a whisper of a line, not a hard stop.
*   **Roundedness:** Maintain a consistent `DEFAULT` (0.5rem) for functional elements, but use `xl` (1.5rem) or `full` for containers and buttons to reinforce the "soft and serene" brand promise.

---

## 5. Components

### Buttons
*   **Primary:** Pill-shaped (`full` roundedness), using a soft lavender gradient. Text is `on_primary`. No shadow in the rest state; a subtle "glow" (ambient shadow) on hover.
*   **Secondary:** No background. Use a "Ghost Border" and `primary` colored text.
*   **Tertiary:** Text-only with an underline that appears only on hover.

### Cards & Containers
*   **Rule:** Never use dividers. 
*   Use `surface_container_low` for the card body and `surface_container_highest` for a header or footer strip to create internal hierarchy.
*   Apply `xl` (1.5rem) corner radius to large content cards to make them feel inviting.

### Input Fields
*   **Style:** Minimalist. Use a `surface_container_lowest` background with a subtle bottom-only "Ghost Border."
*   **Focus State:** Transition the border to `primary` and add a very soft `primary_container` outer glow.

### Signature Component: The "Editorial Spread"
*   For hero sections, overlap a `display-lg` headline across two different surface containers. This breaking of the "container box" is what makes the design feel bespoke and high-end.

---

## 6. Do's and Don'ts

### Do
*   **Do** use extreme whitespace. If you think there is enough space, add 16px more.
*   **Do** use tonal shifts to define areas.
*   **Do** use `Newsreader` for any text that is meant to be read as a "story."
*   **Do** ensure all interactive elements have a `full` or `xl` roundedness to maintain the "Soft Sanctuary" vibe.

### Don't
*   **Don't** use 1px solid, high-contrast borders.
*   **Don't** use pure black (#000000) for text; use `on_surface` (#2f3337) to keep the contrast soft.
*   **Don't** use standard "drop shadows." Only use ambient, tinted blurs.
*   **Don't** crowd the layout. If a screen feels busy, remove secondary elements or hide them behind a glassmorphic overflow menu.