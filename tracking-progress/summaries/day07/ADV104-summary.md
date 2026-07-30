# ADV104 — EventSource SSE subscription

## What was the task?
Open a live SSE subscription from the browser to `/api/v1/trades/stream` using the `EventSource` API, display a connection status badge ("Live" / "Reconnecting..."), and let the browser handle automatic reconnection.

## How was it solved?
- Rewrote `sse.js` to construct `new EventSource('/api/v1/trades/stream')` on page load
- Wired `onopen` (badge → "Live", green), `onerror` (badge → "Reconnecting...", yellow) — no manual reconnection logic, letting the browser handle it natively
- `onmessage` parses `event.data` as JSON inside `try/catch` and calls `prependTradeRow()`
- Added `beforeunload` listener calling `sse?.close()` for clean shutdown
- Kept demo fallback: 3 hardcoded trade events still render via `setTimeout` so the page works without a backend
- Added `escapeHtml()` to sanitize server-supplied strings (XSS protection)
- Added `Intl.NumberFormat` for quantity and price display formatting
- Added `#sse-status` badge element in the header with three CSS states (connecting/live/reconnecting)
- Implemented 50-entry DOM cap and `trade-card--new` modifier (ADV105 logic bundled here since both live in `sse.js`)

## Technologies used
- `EventSource` API — native browser SSE with built-in reconnection
- `Intl.NumberFormat` — locale-aware number formatting
- DOM manipulation — `document.createElement`, `prepend()`, `classList`
- CSS custom properties — badge colors via design tokens from ADV099

## Files changed
- `static-dashboard/js/sse.js` — full rewrite with SSE connection + prepend logic
- `static-dashboard/dashboard.html` — added `#sse-status` badge, removed footer day label
- `static-dashboard/css/style.css` — added `.sse-badge` styles (live/reconnecting/connecting variants)
