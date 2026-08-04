# Ticket ADV136

Assignee: Lavinia31

## Problem
- Needed a consumer that persists every message landing on `trade-events-dlq`, plus an ADMIN-only endpoint to inspect and replay a single DLQ message by `eventId` — neither existed
- The guide's own reference solution reads the failure reason from `KafkaHeaders.EXCEPTION_MESSAGE` — that header doesn't exist on a `DeadLetterPublishingRecoverer` record. The real header (confirmed by dumping the raw Kafka message headers) is `KafkaHeaders.DLT_EXCEPTION_MESSAGE`

## Approach
- Added `DlqMessage` entity + `dlq_messages` table (Liquibase `009-dlq.xml`), `DlqMessageRepository`, `DlqConsumer` (persists eventId/tradeRef/original topic/partition/offset/payload/reason/firstSeen), and `DlqAdminController` with `GET /v1/admin/dlq` (list) and `POST /v1/admin/dlq/replay?eventId=...&dryRun=...`, class-level `@PreAuthorize("hasRole('ADMIN')")`
- Fixed the wrong header constant (`DLT_EXCEPTION_MESSAGE`, not `EXCEPTION_MESSAGE`), made it `required = false` as a defensive safety net
- Found and fixed a second, unrelated bug while testing this: `@PreAuthorize` denials threw `AccessDeniedException` with no handler in `GlobalExceptionHandler`, falling through to the generic 500 handler instead of 403 — added a dedicated handler, fixing this for any `@PreAuthorize` use in the codebase, not just this endpoint

## Notes
- Verified live against a real broker: forced a genuine listener failure (published a duplicate `eventId`, hitting `audit_log`'s real unique constraint), watched it retry on the configured 1s/3s/7s schedule and land on the DLQ, confirmed the persisted row via the admin GET, then replayed it (dryRun first, then for real) and confirmed the row was removed and `ReconciliationConsumer` picked up the republished event
- Confirmed RBAC end-to-end: ADMIN gets 200, TRADER gets 403 (after the AccessDeniedException fix — was 500 before), unauthenticated gets 403 (matches this app's existing convention on every other protected endpoint)
- Before this fix, the wrong header caused `DlqConsumer` to retry the same message forever and eventually double-DLQ it to a stray `trade-events-dlq-dlq` topic — cleaned up as part of ADV128
