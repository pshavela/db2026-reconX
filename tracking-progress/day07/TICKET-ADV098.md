# Ticket ADV098

Assignee: FlorinDLF

## Problem
- Build the Flexbox-based page shell for the static dashboard with nested containers
- Outer vertical column: header → body → footer
- Inner horizontal row: sidebar + main content area
- Stat cards must use Flexbox wrap, not CSS Grid
- Use semantic HTML5 landmarks with ARIA roles

## Approach
- Converted the trainer-provided CSS Grid layout to pure Flexbox throughout
- `body` uses `display: flex; flex-direction: column` as the outer vertical shell
- Added a `.layout__body` wrapper (`display: flex`) for the inner horizontal row containing sidebar + main
- `.layout__header` fixed at 64px, `.layout__footer` fixed at 48px, both using `flex: 0 0 <height>`
- `.layout__sidebar` set to `flex: 0 0 240px` (no-shrink fixed width)
- `.layout__main` set to `flex: 1 1 auto` with `min-width: 0` to prevent overflow
- `.stat-grid` uses `display: flex; flex-wrap: wrap` with each `.stat-card` using `flex: 1 1 calc((100% - gap) / 4)` and `min-width: 180px`
- Semantic HTML5: `<header>`, `<nav>`, `<aside>`, `<main>`, `<section>`, `<article>`, `<footer>`
- ARIA roles: `role="banner"`, `role="complementary"`, `role="main"`, `role="contentinfo"`
- `aria-live="polite"` on the trade feed container for screen reader announcements
- Added inline IIFE in `<head>` before stylesheet link to read theme from `localStorage` and set `data-theme` attribute, preventing FOUC (Flash of Unstyled Content)

## Technologies
- **HTML5** — semantic landmarks (`header`, `nav`, `aside`, `main`, `section`, `article`, `footer`)
- **CSS Flexbox** — nested flex containers for page shell layout and stat card wrapping
- **CSS Custom Properties** — design tokens on `:root` for colors, spacing, border-radius
- **BEM Naming** — `block__element--modifier` convention (`layout__header`, `stat-card--warn`)
- **ARIA** — roles and `aria-live` regions for accessibility
- **localStorage** — theme persistence with zero-FOUC inline script

## Files Modified
- `static-dashboard/dashboard.html` — added `.layout__body` wrapper, FOUC-prevention script, semantic structure
- `static-dashboard/css/style.css` — converted from CSS Grid to Flexbox, added design tokens, responsive breakpoints

## Verification
- Visual inspection in browser: header on top, sidebar on left, 4 stat cards in a row, footer pinned to bottom
- Flexbox layout confirmed — no CSS Grid (`display: grid`) used anywhere
- Theme toggle button renders in the header navigation bar
