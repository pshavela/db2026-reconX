# Ticket ADV122

Assignee: Lavinia31

## Problem
- All pages were imported eagerly in `App.jsx`, so the very first load shipped every page's JS in one bundle
- The `<Suspense>` fallback that already existed was plain text ("Loading…"), not a real skeleton

## Approach
- The `lazy()` imports and the single `<Suspense>` boundary (placed inside `<main>`, below the header/nav) already existed in the shipped `App.jsx` — that part matched the ticket's own reference solution exactly, nothing to change there
- Added the missing piece: `frontend/src/components/PageSkeleton.jsx`, a small placeholder (a few animated gray bars, one wider "title" bar) shown while a route chunk is loading, replacing the plain "Loading…" text. Styled in `global.css` using the project's existing tokens (`--color-surface`, `--color-border`, etc.) and a `shimmer` keyframe animation — the project's existing global `prefers-reduced-motion` rule already disables all animations for users who ask for that, so no extra guard was needed
- Removed the now-unused `.loader` CSS class

## Notes
- Verified against a **production build** (`npm run build` + `npm run preview`), since that's where real per-route chunk splitting is observable (`dist/assets/Trades-*.js`, `Dashboard-*.js`, etc. — confirmed as separate files in the build output)
- Used Playwright to delay the `Trades-*.js` chunk response artificially and found something worth documenting: clicking the `<Link>` to Trades from an already-rendered page (e.g. Dashboard) does **not** show the skeleton at all, even with a 1-second artificial delay — the old page's content just stays on screen until the new chunk resolves, then swaps instantly. This isn't a bug: react-router-dom v7 wraps `<Link>` navigations in a way that avoids the Suspense fallback for regular navigations, only falling back to true blank/first-paint scenarios. Confirmed this by loading `/trades` directly (typing the URL, i.e. no prior rendered content to keep showing) with the same artificial delay — the skeleton appeared correctly there
- Also confirmed chunk caching works: revisiting a route after its chunk already loaded doesn't re-download it (same request count before/after a second visit in the automated check; manually confirmed in the browser too — the chunk request shows `304 Not Modified` with a small, constant response size (~0.2kB, just the "still valid" confirmation), not the full chunk content, on every revisit)
- Confirmed the layout (header/nav) never unmounts during any of this — only the `<main>` contents suspend
