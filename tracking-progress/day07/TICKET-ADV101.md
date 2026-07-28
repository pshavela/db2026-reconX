# Ticket ADV101

Assignee: FlorinDLF

## Problem
- Carve out a dedicated feed area in the dashboard for live trade cards
- Style each `.trade-card` with status-coloured left borders and an entrance animation
- Ensure newly inserted cards animate on DOM insertion without extra JS

## Approach
- The `#trade-feed` section and `.trade-card` base styles were already wired in ADV098 using Flexbox column layout with `gap`, `max-height: 50vh`, and `overflow-y: auto`
- `.trade-card--matched` (green/success) and `.trade-card--break` (red/danger) modifiers were already present
- Added missing `.trade-card--pending` modifier with `border-left: 4px solid var(--color-warning)` — the demo `sse.js` generates this class but it had no corresponding CSS rule
- `@keyframes slide-in` already defined: `translateX(-10%) + opacity: 0` → resting position
- `animation: slide-in 0.3s ease-out` on `.trade-card` ensures any card inserted into the DOM plays the entrance animation automatically

## Technologies
- **CSS Flexbox** — vertical column layout for the feed container
- **CSS Animations** — `@keyframes slide-in` with GPU-compositable properties (`transform`, `opacity`)
- **CSS Custom Properties** — all colours, spacing, and radius via `var()` tokens from ADV099
- **BEM Modifiers** — `.trade-card--matched`, `.trade-card--break`, `.trade-card--pending` for status borders
- **ARIA** — `role="status"` and `aria-live="polite"` on feed container for screen reader announcements

## Files Modified
- `static-dashboard/css/style.css` — added `.trade-card--pending` modifier

## Verification
- Three demo cards appear with staggered slide-in animation via `sse.js`
- BREAK card has red left border, PENDING has yellow, MATCHED has green
- Manually inserting a `.trade-card` via DevTools triggers the slide-in animation
- All values resolve through `var()` tokens — no hardcoded hex or px
