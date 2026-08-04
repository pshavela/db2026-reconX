# Ticket ADV128

Assignee: Lavinia31

## Problem
- `KafkaTopicsConfig` was left as an empty stub (`@Profile("!dev & !test")`, no `NewTopic` beans), so none of the 4 required topics (`trade-events`, `trade-events-dlq`, `recon-results`, `system-alerts`) were declared with their intended partition counts
- With `KAFKA_AUTO_CREATE_TOPICS_ENABLE=true` on the dev broker and no beans running in dev, the broker silently auto-created all 4 topics with the default partition count (1) the moment anything first touched them — not the 3/2/1/3 the ticket actually asks for
- Confirmed in Kafdrop and via `kafka-topics --describe`: every topic showed `PartitionCount: 1`

## Approach
- Filled in the 4 `NewTopic` beans: `trade-events`(3 partitions), `trade-events-dlq`(3 — must match `trade-events` so the DLQ recoverer's same-partition-number mapping holds), `recon-results`(2), `system-alerts`(1), all `replicas(1)` for the single-broker dev setup
- Changed `@Profile("!dev & !test")` to `@Profile("!test")` — the original scoping assumed dev already had correctly-provisioned topics "from the compose stack", but that was never true here. Since this session's dev docker-compose Kafka is the real environment every other Day 9 ticket was verified against, it should also get the correct partition counts. Still excluded from `test` so Testcontainers-managed Kafka in the integration tests (ADV143/144) keeps auto-creating its own fresh topics per run, unaffected
- `recon-results` is declared per the ticket but left unwired — nothing in this codebase publishes or consumes it yet (`ReconciliationConsumer`, ADV131, only logs the trigger); provisioned infrastructure, not a fake feature

## Notes
- Verified live, not just by reading code: stopped the backend (so no live consumer would auto-recreate a deleted topic before the app's own beans ran), deleted the 4 wrongly-partitioned topics plus a stray `trade-events-dlq-dlq` left over from earlier DLQ-header debugging, restarted, and confirmed via `kafka-topics --describe` that all four now show the correct partition counts
- Created a fresh trade afterward to confirm the pipeline still works end-to-end on the recreated topics, and reran the full backend test suite (28/28 green)
