# Ticket ADV140

Assignee: Lavinia31

## Problem
- Needed a Grafana "Consumer lag by topic" panel with yellow/red thresholds, added to the existing Kafka observability dashboard from Day 6 (not a new, separate dashboard)

## Approach
- Added a new "Kafka health" row to `monitoring/grafana/provisioning/dashboards/reconx-overview.json` (the same dashboard Day 6 built), with a time series panel: `sum by (topic) (kafka_consumer_fetch_manager_records_lag)`, thresholds yellow at 100 / red at 1000
- Used the real metric name found while doing ADV139 (`kafka_consumer_fetch_manager_records_lag`), not the guide's `kafka_consumer_records_lag`, which doesn't exist on this project's Kafka client version

## Notes
- Verified live: brought up Prometheus + Grafana via `docker compose up -d prometheus grafana`, confirmed the scrape target was up, queried the exact PromQL directly against Prometheus's API (returned a `0` series per topic — healthy, matching real state), and screenshotted the actual rendered panel inside the dashboard
- Had to temporarily point `prometheus.yml`'s scrape target at `host.docker.internal:8080` to reach the locally-running (non-dockerized) backend for verification, then reverted it back to `backend:8080`, the correct target for the full docker-compose stack — no net diff left in that file
