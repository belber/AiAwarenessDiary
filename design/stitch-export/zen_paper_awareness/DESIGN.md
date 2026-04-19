```markdown
# Design System Strategy: The Breath of Silence

## 1. Overview & Creative North Star
The Creative North Star for this system is **"The Digital Sanctuary."** 

Unlike standard productivity apps that demand attention through high-contrast alerts and rigid grids, this system is designed to "recede." It prioritizes the user's inner state over the interface itself. We achieve this through **Asymmetric Editorial Layouts** and **Tonal Recession**. By breaking the "template" look with varying margin widths and overlapping type, we create a digital experience that feels as intentional and tactile as a high-end linen journal. 

The goal is to evoke a sense of *Yutori*—the Japanese concept of "spaciousness"—where the white space isn't just empty; it is a functional element that allows the mind to breathe.

---

## 2. Colors: Tonal Atmosphere
The palette is rooted in nature and desaturation. We avoid "pure" colors to prevent eye strain and maintain a meditative state.

### Surface & Neutral Logic
- **Background (`#fbf9f4`):** Our "Paper" base. It is a warm, light cream that feels organic rather than synthetic.
- **The "No-Line" Rule:** Sectioning must **never** use 1px solid borders. Boundaries are defined solely through background shifts. For example, a diary entry card should be `surface-container-lowest` (`#ffffff`) sitting on the `background` (`#fbf9f4`). The contrast is felt, not seen.
- **Surface Hierarchy:** 
    - Base Layer: `surface`
    - Inset/Secondary Content: `surface-container-low`
    - Elevated/Interactive Cards: `surface-container-lowest`

### Emotional Spectrum
- **Relaxation (Sage):** Use `primary` (`#546356`) and `primary_container` (`#d6e7d7`). Use these for moments of growth and peace.
- **Tension (Warm Clay):** Use `secondary` (`#855245`) and `secondary_container` (`#ffdad2`). These are not "errors," but signals of emotional weight that need attention.
- **Ambivalence (Stone):** Use `tertiary` (`#635e5b`) for mixed or neutral awareness entries.

### The "Glass & Gradient" Rule
To add "soul," use subtle radial gradients on hero backgrounds (e.g., a 15% opacity `primary` fading into `background`). For floating navigation elements, use **Glassmorphism**: a semi-transparent `surface_bright` with a 20px backdrop-blur to create a "frosted lens" effect.

---

## 3. Typography: The Editorial Voice
We use a high-contrast typographic scale to create a literary, "diary-first" feel.

- **Display & Headlines (`Newsreader`):** This transitional serif is our emotional anchor. Use `display-lg` for date displays and `headline-md` for daily prompts. The serif adds a human, poetic touch that feels "written" rather than "typed."
- **Body & Labels (`Plus Jakarta Sans`):** A modern, geometric sans-serif that ensures legibility. Its wide apertures provide the "airy" feel required for long-form reflection.
- **The Date Statement:** Dates should be prominent—use `display-md` or `display-sm`—but styled in `on_surface_variant` to keep them from being visually aggressive.

---

## 4. Elevation & Depth: Tonal Layering
Traditional drop shadows are too "digital." We utilize **Ambient Depth.**

- **The Layering Principle:** Stack `surface-container` tiers to create depth. A `surface-container-highest` navigation bar over a `surface` background provides enough distinction without a single line of code dedicated to a border.
- **Soft Shadows:** If an element must float (like a "New Entry" button), use an extra-diffused shadow: `box-shadow: 0 12px 40px rgba(49, 51, 46, 0.05)`. The color is a tinted version of our `on_surface`, mimicking a soft shadow on thick paper.
- **The "Ghost Border" Fallback:** For accessibility in input fields, use `outline_variant` at **15% opacity**. It should appear as a faint suggestion of a container.

---

## 5. Components: Soft & Integrated

### Buttons (Capsule/Pill)
- **Primary:** `primary` background with `on_primary` text. No sharp corners; use `rounded-full`.
- **Secondary/Ghost:** `surface-container-high` background with `primary` text.
- **Interaction:** On press, shift the color slightly to `primary_dim` rather than adding a heavy shadow.

### Awareness Cards
- **Construction:** Wide cards with `md` or `lg` corner radius. 
- **Rule:** **No divider lines.** Separate content using `body-md` for metadata and `headline-sm` for the entry title, with a generous 24px vertical gap.

### Input Areas (Non-Intrusive)
- **Styling:** Input fields should not have a background fill. They should sit on the `surface` with a `Ghost Border` bottom-only line or a very subtle `surface-container-low` rounded box.
- **State:** When focused, the label should transition using `newsreader` italic to feel like a gentle invitation to write.

### Navigation (Top-Right Capsule)
- Abandon the bottom nav bar. Use a pill-shaped container in the top-right corner.
- **Icons:** Use 1.5pt thin-line icons. They should be delicate, almost ethereal, using `on_surface_variant`.

### Awareness Timeline (Specific Component)
- Instead of a vertical line, use a series of soft-edged `surface-container-highest` dots. The "current" day uses a `primary` sage-green glow.

---

## 6. Do's and Don'ts

### Do
- **Embrace Asymmetry:** Align the date to the left and the entry content with a wider right margin to mimic a physical notebook.
- **Use Micro-interactions:** Use slow, ease-in-out transitions (300ms+) for page entries to mimic the turning of a page.
- **Prioritize Legibility:** Keep line-heights for body text at 1.6 to 1.8 to ensure the "breathing room" extends to the reading experience.

### Don't
- **No Harsh Grids:** Avoid perfectly symmetrical 2-column layouts. It feels too "corporate."
- **No Pure Black:** Never use `#000000`. Use `on_surface` (`#31332e`) for a softer, charcoal-ink look.
- **No Notification Badges:** Avoid bright red "dot" notifications. If something needs attention, use a soft `secondary_container` tint on the element itself.
- **No Solid Dividers:** Never use `<hr>` tags or 1px borders to separate content. Use whitespace. If it looks too disconnected, you haven't used enough whitespace.```