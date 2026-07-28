# ADV100 — Dark/light theme toggle

## What was the task?
Add a toggle that switches between light and dark themes by flipping a `data-theme` attribute on `<html>`. The choice must persist across reloads with no flash of the wrong theme.

## How was it solved?
- An inline IIFE in `<head>` (before the stylesheet) reads `localStorage.getItem('reconx-theme')` and sets `data-theme` on `<html>` before the page paints — this prevents FOUC
- `[data-theme="dark"]` in CSS overrides the surface/text/shadow tokens from ADV099
- `theme.js` attaches a click handler to `#theme-toggle` that flips the attribute, writes to `localStorage`, and updates `aria-pressed`

## Technologies used
- `localStorage` for persistence
- `data-theme` attribute + CSS cascade for theme switching
- Inline IIFE for zero-FOUC
- `aria-pressed` for accessibility

## Files changed
- `static-dashboard/js/theme.js`
