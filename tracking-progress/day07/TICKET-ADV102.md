# Ticket ADV102

Assignee: FlorinDLF

## Problem
- Build out the animation library with three named keyframes: `slide-in`, `fade-in`, `pulse`
- Wire `pulse` to a danger-state alert banner visible on the dashboard
- Honour `prefers-reduced-motion` so animations disable for users who request it

## Approach
- `@keyframes slide-in` was already defined in ADV101; added `@keyframes fade-in` (opacity 0 → 1)
- Added `@keyframes pulse` as a three-stop animation: 0%/100% at `scale(1)` with a coloured `box-shadow` ring, 50% at `scale(1.02)` with the ring expanded to 8px and fully transparent — creates a breathing glow effect
- Added `.trade-card--new` modifier with combined entrance: `slide-in 0.4s ease-out, fade-in 0.4s ease-out` (to be used by ADV105 SSE prepend logic)
- Added `.alert--danger` class with `animation: pulse 2s ease-in-out infinite`, styled with danger-coloured border and design tokens
- Placed a sample alert banner in `dashboard.html` with `role="alert"` for accessibility
- Moved `@media (prefers-reduced-motion: reduce)` to the bottom of the stylesheet so it overrides all animation declarations above it

## Technologies
- **CSS Keyframe Animations** — GPU-compositable properties only (`transform`, `opacity`, `box-shadow`)
- **CSS Custom Properties** — all colours/spacing via `var()` tokens
- **prefers-reduced-motion** — media query blanket-resets `animation` and `transition` with `!important`
- **ARIA** — `role="alert"` on danger banner for screen reader announcements
- **BEM** — `.trade-card--new`, `.alert--danger` modifiers

## Files Modified
- `static-dashboard/css/style.css` — added `fade-in`/`pulse` keyframes, `.trade-card--new`, `.alert--danger`, moved reduced-motion to bottom
- `static-dashboard/dashboard.html` — added danger alert banner in main area

## Verification
- Alert banner pulses with a red glow on 2s loop
- Trade cards slide in from the left on insertion
- DevTools → Rendering → "Emulate prefers-reduced-motion: reduce" → all animations halt
- Switching back to `no-preference` resumes animations
