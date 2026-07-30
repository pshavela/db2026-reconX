# ADV103 — Responsive breakpoints: desktop, tablet, mobile

## What was the task?
Make the dashboard survive three screen widths: desktop (sidebar left, 4 cards across), tablet (sidebar as horizontal strip, 2 cards across), mobile (sidebar hidden, 1 card per row, no horizontal scroll at 375px).

## How was it solved?
- Desktop (>=1025px): default layout — sidebar 240px left, 4 stat cards across
- Tablet (<=1024px): `.layout__body` flips to `flex-direction: column`, sidebar becomes a horizontal strip with links flowing in a row, stat cards go 2-across via `calc()`
- Mobile (<=640px): sidebar hidden, header shrinks to 56px, padding tightens, cards go full-width
- Styled sidebar links: removed bullets, added padding/hover states, horizontal flow on tablet
- Added `min-width: 0` on mobile cards to prevent overflow

## Technologies used
- CSS Media Queries (`max-width: 1024px`, `max-width: 640px`)
- CSS Flexbox (`flex-direction` toggle, `flex-basis` for column count)
- CSS Custom Properties for all spacing/colour values

## Files changed
- `static-dashboard/css/style.css`
