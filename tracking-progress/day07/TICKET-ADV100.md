# Ticket ADV100

Assignee: FlorinDLF

## Problem
- Add a toggle button that flips the dashboard between light and dark themes by mutating `data-theme` on `<html>`
- Persist the user's choice across reloads via `localStorage`
- Ensure zero flash of unstyled content (FOUC) — dark theme must apply before first paint

## Approach
- FOUC prevention was already wired in ADV098: an inline IIFE in `<head>` before the stylesheet reads `localStorage.getItem('reconx-theme')` and sets `data-theme` on `<html>` before CSS loads
- Dark theme CSS overrides were already defined in ADV099: `[data-theme="dark"]` block overrides surface, text, shadow tokens
- Fixed the trainer-provided `theme.js` comment (referenced wrong ticket ADV102 → ADV100)
- Added `aria-pressed` attribute toggle on the `#theme-toggle` button for accessibility — reflects current dark-mode state
- Added null-guard (`if (!btn) return`) for robustness

## Technologies
- **CSS Custom Properties** — `[data-theme="dark"]` selector overrides `:root` tokens; cascade handles the theme flip
- **localStorage** — `reconx-theme` key persists user preference across sessions
- **Inline IIFE** — blocking script in `<head>` before stylesheet prevents FOUC
- **ARIA** — `aria-pressed` on toggle button communicates state to screen readers
- **dataset API** — `document.documentElement.dataset.theme` for reading/writing `data-theme`

## Files Modified
- `static-dashboard/js/theme.js` — fixed ticket reference, added `aria-pressed` toggle, added null-guard

## Verification
- Click toggle: background/text/cards invert between light and dark immediately
- DevTools → Application → Local Storage: `reconx-theme` key updates to `dark`/`light`
- Reload with dark saved: dark background paints immediately, no white flash
- `aria-pressed` reflects current state (`true` when dark, `false` when light)
