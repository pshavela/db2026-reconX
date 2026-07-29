# Ticket ADV096

Assignee: alexandraelenadumitrescu
Status: Completed (with a documented scope limitation)

## Problem
- No way for an operator to tune the reconciliation price tolerance or clear
  caches at runtime, without a redeploy.

## Approach
- Enabled JMX export (off by default in Boot 3): added
  ```yaml
  spring:
    jmx:
      enabled: true
  ```
  to `backend/src/main/resources/application.yml`.
- Added `backend/src/main/java/com/dbtraining/reconx/observability/ReconConfigMBean.java`
  — `@Component @ManagedResource(objectName = "reconx:type=ReconConfig")` exposing:
  - `priceTolerance` (`double`, read/write, validated to `0..1`)
  - `cachingEnabled` (`boolean`, read/write)
  - `clearCache()` operation, iterating `CacheManager.getCacheNames()` and
    clearing each real Caffeine cache

## Scope limitation (deliberate, documented in code)
- `priceTolerance` / `cachingEnabled` are **demonstrative only** — not wired
  into `ReconciliationEngine`. The ticket's own hints assume the engine holds
  a single mutable tolerance it re-reads per run, but this codebase's
  `ReconciliationRule` (TICKET-ADV026, day2) is a fixed-value enum
  (`EXACT`, `PRICE_TOLERANCE_1PCT`, `PRICE_TOLERANCE_50BPS`, ...) chosen
  explicitly per `reconcile(...)` call — there's no single "current tolerance"
  for the engine to read from a bean. Forcing that wiring would mean changing
  the enum-based design day2 deliberately chose, and `ReconciliationEngine.java`
  is likely to be touched again anyway for TICKET-ADV131 (day9 Kafka consumer
  needs new `scheduleRecon`/`cancelPendingRecon` methods on it that don't
  exist yet). Decided not to force it now — see chat log 2026-07-29 for the
  full reasoning, confirmed with the ticket owner before proceeding.
- `clearCache()` is fully real and does clear the actual `CacheManager`, but
  has no visible effect yet since TICKET-ADV081 (the `@Cacheable` wiring on
  `InstrumentService.findBySymbol`) is itself still an unimplemented stub —
  nothing populates the cache yet for `clearCache()` to meaningfully empty.

## How to demonstrate it works

1. Make sure nothing else is holding port 8080 (e.g. stop the Docker
   `backend` container first: `docker compose stop backend`).
2. Start the app locally:
   ```powershell
   cd backend
   ./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```
3. Open JConsole (ships with the JDK):
   ```powershell
   jconsole
   ```
   Double-click the local `ReconxApplication` process in the connection list
   (accept the "insecure connection" prompt).
4. Go to the **MBeans** tab → expand **reconx** → select **ReconConfig**.
5. **Attributes** tab:
   - Double-click `PriceTolerance`'s value, type `0.05`, press Enter — updates
     without error.
   - Try setting it to `2` (out of range) — throws
     `IllegalArgumentException: tolerance must be 0..1`, surfaced by JConsole
     as an error dialog. **Verified working** — confirmed 2026-07-29.
6. **Operations** tab: click `clearCache` — invokes with no parameters,
   returns "successfully invoked". **Verified working** — confirmed 2026-07-29.
