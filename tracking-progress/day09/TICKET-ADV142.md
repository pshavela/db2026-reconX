# Ticket ADV142

Assignee: Lavinia31

## Problem
- Needed a Stat panel showing total DLQ message count, plus an alerting rule that fires on DLQ activity with severity critical and an annotation pointing operators at the replay endpoint
- A `KafkaDlqGrowing` alert already existed from an earlier merge, but pointed at a metric name (`kafka_consumer_records_consumed_total`) that doesn't exist on this project's Kafka client version, and only had a `summary` annotation, not the `description` the ticket also requires

## Approach
- Added a "DLQ message count" Stat panel: `sum(kafka_consumer_fetch_manager_records_consumed_total{topic="trade-events-dlq"})`
- Renamed the alert to `KafkaDlqMessages`, retargeted it at the real metric name, added the missing `description` annotation pointing at `POST /v1/admin/dlq/replay`
- Switched the expression from the original `increase(...)[5m]) > 0` to a raw `> 0` threshold, matching the ticket's literal wording — documented the tradeoff inline: since the underlying counter is monotonic (never decreases, even after a replay), the alert stays `Firing` once any message has ever landed on the DLQ, instead of self-resolving after 5 quiet minutes

## Notes
- Verified fully live: forced a real DLQ message (duplicate `eventId`), waited past the 1-minute `for:` threshold, confirmed via Prometheus's API that the Stat panel query returns `1` and the alert transitioned to `state=firing` with the description correctly interpolated ("1 message(s) on the DLQ...")
- Revisited during ADV145's AI-assisted config review (finding #4, accepted): decided to switch back to the self-resolving `increase(...)[5m]) > 0` form, since an alert that clears on its own once DLQ activity actually stops is more operationally useful than one that requires a manual dismiss forever after a single message. Decision recorded but not yet applied to `alerts.yml` — deferred to ADV145's own branch, done after this branch merges (see `TICKET-ADV145.md`, once written)
