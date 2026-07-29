# Ticket ADV092

Assignee: alexandraelenadumitrescu
Status: Code done and verified; Grafana panel itself not added yet

## Problem
- No way to see the current breakdown of trades across statuses (PENDING,
  MATCHED, UNMATCHED, DISPUTED, CANCELLED) on the Grafana dashboard.

## Approach
- Added `backend/src/main/java/com/dbtraining/reconx/observability/TradesByStatusGauge.java`
  — a `@Component` that registers one Micrometer `Gauge` per status, each
  tagged `status=<name>`, backed by the already-existing
  `TradeRepository.countByStatus(String)`.
- Did not need to touch `TradeRepository` — `countByStatus` already existed.

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

## Still open
- The actual pie chart **panel** in Grafana (`monitoring/grafana/provisioning/dashboards/reconx-overview.json`)
  has not been added yet — the metric exists and is scraped, but nobody has
  clicked through Grafana's panel editor to build the visual panel and export
  its JSON back into the dashboard file. See `DASHBOARD-DEMO-DATA.md` for how
  to get non-empty data to look at while building the panel.
