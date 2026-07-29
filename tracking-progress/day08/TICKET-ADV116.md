# Ticket ADV116

Assignee: Lavinia31

## Problem
- Need a hook that subscribes to the live trade feed (Server-Sent Events, not WebSocket) and exposes `{ trades, isConnected }`, without the trade list growing forever as new events keep arriving

## Approach
- `frontend/src/hooks/useTradeStream.js` opens a browser-native `EventSource` against `/api/v1/trades/stream` (the SSE endpoint the Day 7 dashboard already points at)
- `onopen` sets `isConnected` to `true`; `onerror` sets it back to `false`
- `onmessage` parses each event's JSON payload and prepends it to `trades` with `setTrades(prev => [trade, ...prev].slice(0, 200))` — a brand new array each time (never mutating the old one, otherwise React won't notice the update), capped at 200 entries so the oldest ones fall off as new ones arrive
- Cleanup (`useEffect` return) calls `sse.close()` so navigating away actually tears down the connection instead of leaking it

## Notes
- There's no real backend SSE endpoint yet (checked: no `SseEmitter`/`text/event-stream` anywhere in `backend/src/main`), so verified this with a small local test server instead (Node's built-in `http`, serving `text/event-stream` at `/stream`) plus a temporary test page in the frontend, driven with Playwright. Deleted both afterwards — not part of this commit
- Had to add an `Access-Control-Allow-Origin: *` header on the test server — without it the browser silently refused to read the SSE response (cross-origin: page on :5173, test server on :8092) and `isConnected` never became `true`. Worth remembering if the real backend endpoint ever shows the same symptom in a cross-origin dev setup
- Confirmed all three "Done when" points with real data, not just that it compiles:
  - the hook connects and `isConnected` flips to `true` once events start arriving
  - sent 210 events in a burst plus 2 more a second apart — the array capped at exactly 200 and the newest event (`T-212`) was always at index 0
  - unmounting the consumer closed the connection server-side immediately, with no reconnect attempts afterwards
