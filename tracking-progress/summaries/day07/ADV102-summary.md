# ADV102 — CSS animations: fade-in, slide-in, pulse

## What was the task?
Build three named keyframe animations (`slide-in`, `fade-in`, `pulse`), wire `pulse` to a danger alert banner, and honour `prefers-reduced-motion` so animations disable for users who request it.

## How was it solved?
- Added `@keyframes fade-in` (opacity 0 to 1)
- Added `@keyframes pulse` (3-stop: scale + box-shadow ring that expands and fades)
- Added `.trade-card--new` modifier with combined `slide-in + fade-in` (used later by SSE prepend)
- Added `.alert--danger` with `animation: pulse 2s ease-in-out infinite` and a red border
- Placed a sample alert banner ("15 unmatched trades require attention") with `role="alert"`
- `@media (prefers-reduced-motion: reduce)` at the bottom of the stylesheet blanket-resets all animations

## Technologies used
- CSS `@keyframes` (GPU-compositable: `transform`, `opacity`, `box-shadow`)
- `prefers-reduced-motion` media query for accessibility
- `role="alert"` ARIA for screen readers

## Files changed
- `static-dashboard/css/style.css`
- `static-dashboard/dashboard.html`
