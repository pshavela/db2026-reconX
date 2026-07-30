# Ticket ADV092

Assignee: alexandraelenadumitrescu
Status: Completed

## Problem
- No way to see the current breakdown of trades across statuses (PENDING,
  MATCHED, UNMATCHED, DISPUTED, CANCELLED) on the Grafana dashboard.

## Approach
- Added `backend/src/main/java/com/dbtraining/reconx/observability/TradesByStatusGauge.java`
  — a `@Component` that registers one Micrometer `Gauge` per status, each
  tagged `status=<name>`, backed by the already-existing
  `TradeRepository.countByStatus(String)`.
- Did not need to touch `TradeRepository` — `countByStatus` already existed.
- Added the pie chart panel itself to
  `monitoring/grafana/provisioning/dashboards/reconx-overview.json` — built
  through Grafana's panel editor (query `sum by (status) (trades_by_status)`,
  type `piechart`, legend `{{status}}`), then exported via Dashboard settings
  → JSON Model and merged back into the provisioning file.

## How to demonstrate it works

1. Start the app locally:
   ```powershell
   cd backend
   ./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```
2. Query the metrics endpoint and confirm all 5 series are present:
   ```powershell
   curl.exe -s http://localhost:8080/api/actuator/prometheus | Select-String "trades_by_status"
   ```
   Expected output — five lines, one per status:
   ```
   trades_by_status{application="reconx",status="CANCELLED"} 0.0
   trades_by_status{application="reconx",status="DISPUTED"} 0.0
   trades_by_status{application="reconx",status="MATCHED"} 0.0
   trades_by_status{application="reconx",status="PENDING"} 0.0
   trades_by_status{application="reconx",status="UNMATCHED"} 0.0
   ```
   (All `0.0` is expected on an empty dev DB — trades aren't seeded automatically,
   see TICKET-ADV079. Values would reflect real counts once trades exist.)
3. Confirmed the same series are visible through Prometheus directly, once the
   full stack is up (`docker compose up -d --build`), proving the containerized
   backend is scraped correctly:
   ```powershell
   curl.exe -s http://localhost:9090/api/v1/query --data-urlencode "query=trades_by_status"
   ```
   Returns a JSON vector with one result per status (verified — see chat log
   2026-07-29).
4. Confirmed the panel itself via the Grafana API after a full container
   restart (`docker compose restart grafana`), proving it's sourced from the
   provisioning file, not just Grafana's session state:
   ```powershell
   curl.exe -s -u admin:admin http://localhost:3000/api/dashboards/uid/reconx-overview
   ```
   `"Trades by status (ADV092)"` is the first entry in the returned
   `dashboard.panels` array — verified 2026-07-29.

## Known caveat
- All series read `0.0` on a fresh dev database — trades aren't seeded
  automatically (see TICKET-ADV079). See `DASHBOARD-DEMO-DATA.md` for how to
  safely populate non-zero demo data without corrupting existing
  counterparties/instruments.
