# Note — Temporary SSE bridge for the live trade feed (not tied to a ticket)

## Problem
- `frontend/src/hooks/useTradeStream.js` (TICKET-ADV116) opens an `EventSource`
  against `/api/v1/trades/stream`, and the Dashboard's live stats depend on it —
  but no ticket, in any day's guide, actually assigns building that endpoint on
  the backend. Checked day5–day10: `TICKET-ADV104` (day7) and `TICKET-ADV116`
  (day8) both *consume* `/api/v1/trades/stream` and explicitly assume it
  already exists; nothing implements it.
- The real, intended architecture (per `TICKET-ADV128`/`TICKET-ADV129`, Day 9)
  is: `TradeService.create()` publishes a `TradeEvent` to a Kafka topic
  (`trade-events`), which other consumers (recon, audit, and presumably a
  stream bridge) subscribe to. As of this note, neither is implemented:
  `KafkaTopicsConfig` is an empty stub and `TradeEventProducer.publish()`
  still throws `UnsupportedOperationException`.

## What was built instead
- `backend/src/main/java/com/dbtraining/reconx/service/TradeStreamBroadcaster.java`
  (new) — an in-process, in-memory substitute. Holds a list of open
  `SseEmitter`s; `TradeController.stream()` (new `GET /v1/trades/stream`
  endpoint) subscribes a browser to it, and `TradeController.create()`
  broadcasts the newly created `TradeResponse` to every open connection
  right after saving — no Kafka involved.
- `SecurityConfig`: `GET /v1/trades/stream` is `permitAll`, ahead of the
  general `/v1/trades/**` rule. This is required, not just convenient — the
  browser's native `EventSource` API cannot attach an `Authorization` header,
  so the JWT-gated rule that covers every other trade endpoint would reject
  every SSE connection outright.

## Known limitations (by design, for a quick bridge)
- Only reflects trades created after a browser tab connects — no backlog/replay
  for tabs that open later, since there's no persistent broadcast history.
- The stream endpoint is unauthenticated. Fine for this training app's scope,
  but would need the query-param-token workaround (or similar) before this
  pattern belongs anywhere with real data.
- Bypasses Kafka entirely, so nothing published here reaches the
  `trade-events` topic or any future consumer (recon/audit) that expects to
  read from it.

## Replacing this
Once `TICKET-ADV128` (topic) and `TICKET-ADV129` (producer) are implemented,
the natural next step is a Kafka consumer that forwards `trade-events`
messages into `TradeStreamBroadcaster.broadcast(...)` (or replaces it
entirely) — at which point this bridge's direct call from
`TradeController.create()` should be removed so trades created through any
path (not just the REST endpoint) still reach the live feed.

## Verification
- `curl -N http://localhost:8080/api/v1/trades/stream` (no auth) stays open;
  a concurrent authenticated `POST /v1/trades` sends the created trade down
  that connection immediately.
- Manually verified in the browser: opened the Dashboard, created a trade via
  a separate authenticated request (simulating Add Trade), and watched
  "SSE: connected", "Trades streamed", and "Portfolio value" update live with
  no page reload.
- `./mvnw test`: 20/22 pass — same 2 pre-existing Testcontainers/Docker
  failures as before, unrelated to this change. Had to add a
  `@MockBean TradeStreamBroadcaster` to `TradeControllerWebMvcTest` since the
  controller's constructor gained a third dependency.
