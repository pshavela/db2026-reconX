# ADV105 — SSE handler with prepend-and-animate

## What was the task?
Render each incoming SSE trade as a `.trade-card` at the top of the feed, replay the slide-in animation on insert, and cap the feed at 50 entries so the DOM stays bounded.

## How was it solved?
- Implemented `prependTradeRow(trade)` inside the `sse.js` IIFE (bundled with ADV104)
- Maps `trade.status` to CSS modifier: MATCHED → `trade-card--matched`, BREAK/UNMATCHED → `trade-card--break`, PENDING → `trade-card--pending`
- Builds `<article>` via `document.createElement` with class `trade-card <statusModifier> trade-card--new`
- Uses `feed.prepend(el)` so newest trades appear at the top
- `trade-card--new` modifier triggers combined `slide-in 0.4s + fade-in 0.4s` entrance animation (CSS from ADV102)
- `setTimeout` removes `trade-card--new` after 500ms so re-insertion can re-trigger animation
- DOM cap: `while (feed.children.length > 50) feed.lastElementChild.remove()` keeps the feed bounded
- `escapeHtml()` sanitizes all server-supplied strings (tradeRef, symbol, status) to prevent XSS
- `Intl.NumberFormat` formats quantity (plain) and price (2-4 decimal places)

## Technologies used
- DOM API — `createElement`, `prepend`, `classList`, `lastElementChild.remove()`
- `Intl.NumberFormat` — locale-aware number formatting
- CSS animations — `.trade-card--new` triggers `slide-in` + `fade-in` keyframes from ADV102
- XSS prevention — `escapeHtml` via `createTextNode` + `innerHTML` extraction

## Files changed
- `static-dashboard/js/sse.js` — `prependTradeRow()` function (same file as ADV104, no separate changes needed)
