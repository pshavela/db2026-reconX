# Ticket ADV115

Assignee: Lavinia31

## Problem
- Need a reusable hook that opens a WebSocket, exposes `{ data, status, send }`, and reconnects on its own if the connection drops — instead of every consumer (this ticket's hook, ADV116's live feed, Day 9's break feed) reimplementing the same connect/retry/cleanup logic

## Approach
- `frontend/src/hooks/useWebSocket.js`: the socket, the retry counter, the reconnect timer, and a "stop reconnecting" flag all live in `useRef`s (not state), so they don't trigger re-renders and don't get recreated on every render
- `connect()` (wrapped in `useCallback`, deps `[url, reconnect, maxRetries]`) opens the socket and wires 4 handlers:
  - `onopen`: sets `status` to `'open'` and resets the retry counter to 0
  - `onmessage`: tries to `JSON.parse` the payload, falls back to the raw string if it isn't valid JSON
  - `onerror`: sets `status` to `'error'`
  - `onclose`: sets `status` to `'closed'`, and if `reconnect` is on and we haven't hit `maxRetries`, schedules another `connect()` after `min(30000, 500 * 2^retries)` ms
- The single `useEffect` calls `connect()` once on mount. Its cleanup sets the "stop" flag (so any in-flight handlers become no-ops), clears the pending reconnect timer, and closes the socket
- `send(payload)` is a no-op unless the socket's `readyState` is `OPEN`; strings are sent as-is, anything else goes through `JSON.stringify`

## Notes
- Built a small throwaway test harness to verify this for real, since there's no backend WebSocket endpoint yet to test against: a local WS test server (Node + the `ws` package) that force-closes each connection ~1.2s after it opens (to trigger reconnects on demand), plus a temporary test page in the frontend with a button to mount/unmount the hook's consumer. Drove it with Playwright, using `page.on('websocket', ...)` to see every socket the browser actually opened/closed, cross-checked against the server's own open-connection counter. Deleted the test page and reverted `App.jsx`/`main.jsx` afterwards — none of that is part of this commit
- Confirmed: exactly one open connection at a time server-side, reconnect after each forced close, retry counter resets to 0 on every successful reopen (so the backoff only grows on repeated failures, not on repeated forced closes of otherwise-successful connections — matches the spec), and zero new connections after unmounting the consumer
- One thing worth knowing for later debugging: with React StrictMode + this page's `React.lazy`-loaded route both in play, the dev server briefly showed *two* concurrent connections during the mount phase. Re-tested with StrictMode temporarily removed and got a perfectly clean single-connection timeline — so that blip is a known dev-only side effect of StrictMode double-invoking effects on lazy-loaded routes, not a bug in the hook. Production builds don't run the double-invoke, so this doesn't show up there
