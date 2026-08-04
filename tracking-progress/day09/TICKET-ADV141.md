# Ticket ADV141

Assignee: Lavinia31

## Problem
- Needed a Grafana panel overlaying produce rate vs. consume rate, next to ADV140's lag panel in the same Kafka health row

## Approach
- Added "Throughput: produced vs consumed" time series panel with two series: `consumed = sum(rate(kafka_consumer_fetch_manager_records_consumed_total[1m]))`, `produced = sum(rate(kafka_producer_record_send_total[1m]))` — again using the real `fetch_manager`-prefixed metric name for the consumer side

## Notes
- Verified live by generating steady trade-creation traffic and querying both expressions directly against Prometheus: produced ~0.24/s, consumed ~0.48/s — exactly 2x, and that's correct, not a bug: `trade-events` has two independent consumer groups (`recon-service`, `audit-service`), so summing "consumed" across groups double-counts relative to "produced" by design
