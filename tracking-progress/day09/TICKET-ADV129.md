# Ticket ADV129

Assignee: Lavinia31

## Problem
- `TradeEventProducer` existed as a correctly-implemented, standalone `@Component` (matches the reference solution exactly: keyed by `tradeRef`, non-blocking `send`), but it was marked "Completed" in the tracker despite `TradeService.create/update/updateStatus/softDelete` never actually calling `events.publish(...)` — just a `// TODO — TICKET-ADV129` comment left in place
- Because of that, even with Kafka running and the producer bean active, creating/updating/deleting a trade published nothing at all

## Approach
- Wrote the actual `events.publish(new TradeEvent(...))` calls into all 4 `TradeService` methods, each with the correct event type (`TRADE_CREATED` / `TRADE_UPDATED` / `TRADE_CANCELLED`) and a real before/after JSON snapshot
- Added a `toJson(Trade)` helper that serializes through the existing `TradeMapper` → `TradeResponse` DTO (not the raw JPA entity), avoiding any risk of a `LazyInitializationException` on serialization and matching how the rest of the codebase already converts entities for the API layer
- Added `.whenComplete((result, ex) -> ...)` success/failure logging to `TradeEventProducer.publish()` — required by the ticket's own Done-when checklist, and previously completely absent, meaning publish failures were silently swallowed

## Notes
- Verified live end-to-end for all 3 event types: created a trade (before=null, after=snapshot), updated it (before=old snapshot, after=new snapshot — confirmed the quantity/price diff was captured correctly), then deleted it (before=last snapshot, after=null) — each one showed up correctly in `audit_log` via `AuditEventConsumer`, and `ReconciliationConsumer` logged the same `eventId` for each
- `TradeMetrics.incrementTradeCreated()`/`recordTradeValue()` (ADV083/ADV086) are also still empty stubs referenced by the same original TODO comment — left untouched, out of scope for this ticket, noted for whoever picks it up next
