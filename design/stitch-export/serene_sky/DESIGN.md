```markdown
# Design System Document: Atmospheric Weightlessness

## 1. Overview & Creative North Star
The Creative North Star for this design system is **"Atmospheric Weightlessness."** 

Unlike traditional utility apps that rely on rigid borders and heavy containers, this system is designed to feel like an editorial layout suspended in a clear sky. We are moving away from the "app-as-a-tool" aesthetic toward "app-as-a-sanctuary." By leveraging intentional asymmetry, expansive negative space, and the sophisticated interplay between the Newsreader serif and Manrope sans-serif, we create a high-end experience that encourages the user to breathe. 

This is not a flat interface; it is a series of layered, translucent planes that mimic the depth of the horizon.

---

## 2. Colors & Surface Philosophy
The palette is a study in tonal recession. We use varying shades of cool blues and off-whites to define space rather than lines.

### The "No-Line" Rule
**Strict Mandate:** 1px solid borders are prohibited for sectioning or containment. 
Boundaries must be defined through background color shifts. For example, a `surface_container_low` (#f0f4f6) section should sit on a `background` (#f8fafb) to create a soft edge. This mimics natural light falling on paper rather than a digital stroke.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers—like stacked sheets of fine vellum.
- **Base Layer:** `background` (#f8fafb) for the overall canvas.
- **The "Floating" Layer:** `surface_container_lowest` (#ffffff) for primary interactive cards. This creates the highest contrast against the background without using shadows.
- **The "Recessed" Layer:** `surface_container_high` (#e3e9eb) for secondary utility areas or inactive states.

### The Glass & Gradient Rule
To achieve a premium, custom feel:
- **Glassmorphism:** Use `surface` colors at 70-80% opacity with a `backdrop-filter: blur(20px)` for floating navigation bars or overlay modals.
- **Signature Textures:** For Hero CTAs, use a subtle linear gradient from `primary` (#336579) to `primary_container` (#b2e4fb) at a 135-degree angle. This adds "soul" and prevents the interface from feeling sterile.

---

## 3. Typography: Editorial Authority
The typography scale is designed to create a rhythmic, high-end magazine feel.

- **Display & Headlines (Newsreader):** Use these for moments of reflection and impact. The Newsreader serif provides an authoritative, human touch. Use `display-lg` (3.5rem) with generous leading to let the words breathe.
- **Titles & Body (Manrope):** Manrope is our functional workhorse. Use `title-lg` for card headers to provide a modern, clean contrast to the serif headlines.
- **The Hierarchy Rule:** Never pair a Newsreader headline with a Newsreader body. The tension between the organic serif and the geometric sans-serif is what gives this system its "bespoke" edge.

---

## 4. Elevation & Depth
We reject the standard Material Design "shadow-heavy" look in favor of **Tonal Layering**.

- **The Layering Principle:** Depth is achieved by "stacking." Place a `surface_container_lowest` card on a `surface_container_low` section to create a soft, natural lift.
- **Ambient Shadows:** If a floating element (like a FAB) requires a shadow, it must be an "Ambient Shadow": 
    - **Color:** A tinted version of `on_surface` (#2c3436) at 5% opacity.
    - **Blur:** 40px to 60px.
    - **Spread:** -10px to keep the shadow "tucked" under the element.
- **The "Ghost Border" Fallback:** If a border is essential for accessibility (e.g., in high-glare environments), use `outline_variant` (#acb3b6) at **15% opacity**. Never 100%.

---

## 5. Components

### Buttons
- **Primary:** High-pill shape (`rounded-full`). Background: `primary` (#336579). Text: `on_primary` (#f2faff). Use `title-sm` (Manrope) for button labels to maintain a crisp, modern look.
- **Secondary:** `surface_container_highest` background with `on_surface` text. No border.
- **Tertiary:** Text-only in `primary` with 0.5px letter spacing.

### Cards & Content Blocks
- **The Organic Card:** Use the `xl` (1.5rem) corner radius. 
- **Separation:** Forbid the use of divider lines. Separate content using `spacing-lg` (vertical white space) or a background shift from `surface` to `surface_container`.

### Inputs & Selection
- **Input Fields:** Use `surface_container_low` as the fill. On focus, transition the background to `surface_container_lowest` and apply a "Ghost Border" using `primary`.
- **Chips:** Selection chips should use the `full` (9999px) radius. Unselected states should be `surface_variant`. Selected states should transition to `primary_container` with `on_primary_container` text.

### Mindfulness Specifics: The "Breathing" Component
- **Pulse:** Interactive elements for meditation should use a slow, eased-in-out scale animation (1.0 to 1.05) combined with a soft `surface_tint` (#336579) glow.

---

## 6. Do's and Don'ts

### Do:
- **Do** use asymmetrical margins (e.g., a larger left margin for a headline) to create an editorial, custom feel.
- **Do** prioritize `surface` shifts over lines for all UI sectioning.
- **Do** use `Newsreader` for all "moment of calm" messaging.
- **Do** allow content to bleed off the edges slightly in carousels to suggest a continuous sky.

### Don't:
- **Don't** use pure black (#000000) for text. Always use `on_surface` (#2c3436) to maintain the soft, tranquil vibe.
- **Don't** use standard "drop shadows" with 20%+ opacity. They feel "dirty" against the pale blues.
- **Don't** use sharp corners (`none` or `sm`). Everything in the mindfulness space must feel approachable and soft.
- **Don't** crowd the layout. If you think there is enough white space, add 20% more.