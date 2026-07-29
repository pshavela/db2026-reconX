# Ticket ADV124

Assignee: Lavinia31

## Problem
- Need a light/dark theme switch where React only owns *which* theme is active, not the actual colour values — the CSS (already shipped from Day 7, `[data-theme="dark"]` overrides) owns the colours
- Should remember the choice across reloads, and guess a sensible default (the OS/browser's `prefers-color-scheme`) for first-time visitors instead of always defaulting to light

## Approach
- `frontend/src/context/ThemeContext.jsx`: `ThemeProvider` owns one piece of state, `theme` (`'light'` | `'dark'`), lazily initialised by `initialTheme()` — reads `localStorage['reconx-theme']` first, and only if that's empty falls back to `window.matchMedia('(prefers-color-scheme: dark)').matches`. The ticket's own stub comment only mentioned falling back to `'light'`, but the actual "Done when" criteria explicitly require respecting the OS preference, so implemented that too
- A single `useEffect([theme])` writes `document.documentElement.dataset.theme = theme` (which is all the CSS needs to flip) and persists the value to `localStorage` on every change
- `toggle()` flips `light` <-> `dark`; `useTheme()` is just `useContext(ThemeContext)`. `ThemeProvider` was already correctly lifted above `BrowserRouter`/`AuthProvider` in `main.jsx`
- Added a small toggle button (🌙/☀️) in `App.jsx`'s header nav — nothing in the codebase called `toggle()` yet, and the ticket's own verification steps say to "click the theme toggle", so something had to exist to click

## Notes
- Verified with Playwright across separate browser contexts (to control `prefers-color-scheme` cleanly), not just that it compiles:
  - fresh context, OS set to light, empty storage: starts `light`
  - fresh context, OS set to dark, empty storage: starts `dark` — confirms the `matchMedia` fallback actually works, not just the storage path
  - clicking the toggle flips `data-theme` immediately and writes the new value to `localStorage`
  - reloading the page (same context) keeps the toggled theme — persistence confirmed
- Screenshotted before/after the toggle — dark mode visibly restyles the whole page (background, text, inputs) using only the existing CSS, exactly as intended (React flips one attribute, CSS does the rest)
- Also confirmed by hand in the browser: toggle button visible in the header, click flips instantly, survives a manual reload
