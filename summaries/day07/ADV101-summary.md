# ADV101 — Trade feed area with slide-in animation

## What was the task?
Create a dedicated feed area for live trade cards with status-coloured left borders and a slide-in entrance animation on DOM insertion.

## How was it solved?
- `#trade-feed` is a vertical flex column with `gap`, `max-height: 50vh`, and scroll overflow
- `.trade-card` gets `animation: slide-in 0.3s ease-out` so any card inserted into the DOM animates automatically
- Three BEM modifiers for status: `--matched` (green), `--break` (red), `--pending` (yellow)
- All colours come from design tokens (`--color-success`, `--color-danger`, `--color-warning`)
- Demo trades from `sse.js` prepend into the feed with staggered timing

## Technologies used
- CSS Flexbox (vertical column layout)
- CSS `@keyframes` animation (`transform` + `opacity` — GPU compositable)
- BEM modifiers for status variants
- `aria-live="polite"` for screen reader announcements

## Files changed
- `static-dashboard/css/style.css`
