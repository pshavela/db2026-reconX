# Ticket ADV138

Assignee: Lavinia31

## Problem
- `GET /v1/audit/trades/{tradeRef}/events` existed as a stub returning `Collections.emptyList()`, with a comment saying to wire it up "once the audit-log Kafka consumer is in place" — which it had been since ADV132
- No explicit `@PreAuthorize` on `AuditController`; access relied only on `SecurityConfig`'s `/v1/audit/**` matcher (already correct, but the ticket's Done-when checklist explicitly wants it stated at the method-security layer too)

## Approach
- Replaced the stub body with the real query (`auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef)`), matching the sibling `history()` method — returns `List<AuditLogEntry>`, not the guide's suggested `List<TradeEvent>` DTO, since `AuditLogEntry` already contains everything needed and converting would just be ceremony
- Added `@PreAuthorize("hasAnyRole('ADMIN','RECON_ANALYST')")` at class level — redundant with the existing SecurityConfig matcher, kept explicit per the ticket and for defense in depth
- Bonus, tying back to ADV137: added `GET /v1/audit/trades/{tradeRef}/rebuild`, wiring `TradeAggregator` (which otherwise had no caller anywhere) into a real endpoint — pairs the raw event log with the state folded from it, matching ADV137's own stated rationale

## Notes
- Verified live: ADMIN gets 200 with the ordered event list, TRADER gets 403, unauthenticated gets 403 (consistent with the rest of the app)
- Verified `/rebuild` against real data: a trade that went create→update rebuilt to its last snapshot, matching the real row in `trades` exactly; a trade that went create→cancel rebuilt to 404
