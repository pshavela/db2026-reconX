# How to populate the Grafana dashboard with demo data

By default the dashboard ("ReconX Overview") shows "No data" on most panels
because the database is empty — nothing seeds trades automatically (see
`tracking-progress/day05/TICKET-ADV079.md`: `db/seed_data.sql` exists but is
never wired into Liquibase). This note is the checklist for making the
panels show something.

Prerequisite: the full stack is up (`docker compose up -d --build`), all
containers healthy (`docker compose ps`).

## 1. Seed the database directly (trades_by_status, recon_break_count)

`db/seed_data.sql` inserts 10 counterparties, 50 instruments, 500 trades
(mixed statuses), and ~30 OPEN recon_breaks. Run it against the running
Postgres container:

```powershell
docker exec -i reconx-postgres psql -U reconx_user -d reconx < db/seed_data.sql
```

This immediately populates:
- **ADV092** — trades_by_status pie chart (real counts per status)
- **ADV091** — recon_break_count stat panel

It does **not** populate `trade_created_total` (ADV089) or the API
request-rate panels (ADV087/088) — those are application-level Micrometer
counters that only increment when a request actually goes through
`TradeController`, not on a direct SQL insert.

## 2. Generate API traffic (trade_created_total, request rate, latency)

Security is still `permitAll()` (ADV072-074 not implemented yet), so this
works without a JWT for now. Loop a few dozen creates against
`POST /api/v1/trades`:

```powershell
1..30 | ForEach-Object {
  $body = @{
    tradeRef       = "TRD-DEMO-$(Get-Date -Format yyyyMMdd)-$('{0:D4}' -f $_)"
    instrumentId   = (Get-Random -Minimum 1 -Maximum 51)
    counterpartyId = (Get-Random -Minimum 1 -Maximum 11)
    assetClass     = "EQUITY"
    side           = "BUY"
    quantity       = 100.0
    price          = 245.50
    tradeDate      = (Get-Date -Format yyyy-MM-dd)
  } | ConvertTo-Json
  Invoke-RestMethod -Uri http://localhost:8080/api/v1/trades -Method Post -Body $body -ContentType "application/json"
  Start-Sleep -Milliseconds 200
}
```

Watch the request-rate (ADV087) and P95 latency (ADV088) panels spike
during the loop, and `trade_created_total` climb.

## 3. reconciliation_duration_seconds (ADV090) — no real trigger exists yet

This one is a genuine gap, not just missing demo data. The metric is
`@Timed` on `ReconciliationEngine.reconcile()`, but the only place in
production code meant to call it — `POST /v1/recon/run` — is still a stub
(`throw new UnsupportedOperationException("TICKET-ADV068")`). There is
currently no way to exercise this metric through the running application.

Two honest options, not attempted yet:

- **Real fix**: implement TICKET-ADV068 (generate a jobId, fetch internal +
  external trades, call `ReconciliationService.runRecon(...)`, persist to
  `recon_jobs`). This is the actual missing piece, not a demo-data problem.
- **Throwaway workaround for a demo only**: a one-off local script/test
  (never committed) that autowires `ReconciliationEngine` directly and
  calls `reconcile(...)` a few times against seeded trades, purely to put
  a few data points on the histogram. Does not reflect a real user flow —
  don't mistake the resulting graph for evidence the feature works
  end-to-end.

Until one of those happens, the ADV090 panel will legitimately keep
showing "No data" — that's accurate, not broken.
