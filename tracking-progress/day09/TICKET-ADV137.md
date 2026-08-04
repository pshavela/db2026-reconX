# Ticket ADV137

Assignee: Lavinia31

## Problem
- Needed a `TradeAggregator.rebuild(tradeRef)` that folds a trade's `audit_log` event history into its current state — proving the event log persisted by ADV132 is a real source of truth, not just a side log
- The guide's reference assumes a different shape of `AuditLogEntry` than what actually exists in this codebase (`JsonNode`-typed `getAfterData()`/`getOperation()` vs. this project's real `String`-typed `getAfterState()`/`getEventType()`, and `findByTradeRefOrderByOccurredAtAsc` vs. the real, already-existing `findByTradeRefOrderByEventTimestampAsc`)

## Approach
- Implemented `TradeAggregator.rebuild(String tradeRef) -> Optional<String>` using the actual `AuditLogRepository`/`AuditLogEntry` API: folds `TRADE_CREATED`/`TRADE_UPDATED` events to `state = entry.getAfterState()`, `TRADE_CANCELLED` to `state = null`, ordered by `event_timestamp` (not `eventId` — UUIDs aren't monotonic)
- No repository changes needed — `findByTradeRefOrderByEventTimestampAsc` already existed and is already used by `AuditController`

## Notes
- Covered with 3 unit tests mirroring the ticket's own acceptance scenarios (mocked repository): no events → empty, created+updated → last after-snapshot, created+updated+cancelled → empty. All pass
- The aggregator had no caller anywhere in the codebase after this ticket alone — wired it into a real endpoint as part of ADV138 (`GET /v1/audit/trades/{tradeRef}/rebuild`) so it could actually be exercised against live data, not just unit-tested against mocks
