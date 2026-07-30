# Ticket ADV103

Assignee: FlorinDLF

## Problem
- Make the dashboard layout responsive across three breakpoints: desktop, tablet, mobile
- Sidebar should adapt at each breakpoint
- No horizontal scrolling at 375px mobile width

## Approach
- Rewrote tablet breakpoint (`max-width: 1024px`): sidebar becomes horizontal strip via `flex-direction: column` on `.layout__body`, sidebar items flow horizontally
- Rewrote mobile breakpoint (`max-width: 640px`): sidebar hidden, header shrinks, cards go full-width, padding tightens
- Added sidebar styling: removed default list bullets, styled links as padded hover-able items, uppercase muted heading
- Added `min-width: 0` on mobile stat cards to prevent overflow

## Technologies
- **CSS Media Queries** — `@media (max-width: 1024px)` and `@media (max-width: 640px)`
- **CSS Flexbox** — `flex-direction` toggle for layout reflow, `flex-basis` for card columns
- **CSS Custom Properties** — all values via `var()` tokens

## Files Modified
- `static-dashboard/css/style.css` — rewrote responsive breakpoints, added sidebar link styling

## Verification
- Desktop (1280px): sidebar left, 4 cards across
- Tablet (768px): sidebar horizontal strip on top, cards 2-across
- Mobile (375px): sidebar hidden, cards full-width, no horizontal scroll
