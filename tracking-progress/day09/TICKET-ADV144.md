# Ticket ADV144

Assignee: Lavinia31

## Problem
- Needed a Testcontainers test proving a failing listener actually routes to `trade-events-dlq` after retries exhaust — no such test existed
- The guide's reference forces the failure by mocking `ReconciliationEngine.scheduleRecon(...)` — that method doesn't exist anywhere in this codebase's real `ReconciliationConsumer` (ADV131 only logs the trigger, per its own ticket scope, and never calls `ReconciliationEngine` at all), so the mock-based approach doesn't apply here
- The guide's raw assertion consumer sets `JsonDeserializer.TRUSTED_PACKAGES` but no default type; since this app's producer sends no type headers (`spring.json.add.type.headers: false`), that consumer throws "No type information in headers and no default type provided" on every poll

## Approach
- Wrote `DlqRoutingIT`: instead of mocking a dependency that isn't wired into the Kafka path, forces a **real** failure — publishes the exact same `TradeEvent` (identical `eventId`) twice. The second delivery hits `audit_log`'s real unique constraint inside `AuditEventConsumer`, throws for real, exhausts retries, and gets routed to the DLQ by the real `DeadLetterPublishingRecoverer`. A throwaway raw `KafkaConsumer` (unique groupId per run) asserts the poisoned record arrives within 30s
- Fixed the deserializer gap by explicitly setting `JsonDeserializer.VALUE_DEFAULT_TYPE` on the raw assertion consumer

## Notes
- Verified live: full backend suite green, 28/28 tests, 0 failures, 0 errors, including all three Testcontainers-based tests (this one, plus the two pre-existing ones, unblocked by the same Testcontainers version bump noted in ADV143)
