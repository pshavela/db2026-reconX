# Note — Day 8 backend blocker: temporary EAGER-fetch workaround (not the real fix)

Not tied to a single ADV ticket — this documents a cross-cutting workaround, not new Day 8 scope.

## Problem
- Several Day 8 tickets (ADV117, ADV119, ADV121) needed a real `GET /api/v1/trades`
  response to verify against, but the endpoint threw `LazyInitializationException`
  the moment `instrument`/`counterparty` were serialised, because
  `spring.jpa.open-in-view=false` closes the Hibernate session before the
  controller reads those `LAZY` relations.
- Root cause already documented by `pshavela` in
  [`tracking-progress/day05/TICKET-ADV063.md`](../day05/TICKET-ADV063.md).
- Worked around it during Day 8 itself by testing against a mocked
  `/api/v1/trades` response (see the ADV121 note) instead of the real backend.

## Temporary workaround (not merged, not the fix)
- Tried on a separate branch/PR: `fix/trade-eager-fetch` —
  [PR #24](https://github.com/pshavela/db2026-reconX/pull/24), switching
  `Trade.instrument` / `Trade.counterparty` from `FetchType.LAZY` to
  `FetchType.EAGER` so both are loaded up front.
- This got the endpoint returning 200 locally, which was enough to unblock
  manual/Playwright verification of the Day 8 tickets above — but it's a
  blanket fetch-strategy change with real cost (every trade query now always
  joins both tables, even when the caller never needed them), not a
  considered fix.
- `pshavela` closed PR #24 without merging, correctly calling it out as
  a quick hack for testing rather than the right change:
  > "So this is just a small hack for testing :) What we should do is move
  > the serialization to the service layer, which I will push shortly."
- **`main` still has `FetchType.LAZY` today** — the real fix (reading the
  lazy relations inside the transactional service layer, before the session
  closes, instead of changing the fetch strategy) is still pending from
  `pshavela`.

## Verification (of the workaround, while it existed on its own branch)
- Authenticated `GET /api/v1/trades` returned 200 with `instrumentSymbol` /
  `counterpartyName` populated (previously a 500).
- `./mvnw test`: 20/22 pass. The 2 failures (`LiquibaseMigrationsIntegrationTest`,
  `ReconciliationIntegrationTest`) are the pre-existing Testcontainers / Docker
  Desktop npipe incompatibility, unrelated to this change.
- Manually verified end-to-end in the browser against that branch's frontend
  (Trades list, Add Trade, status change via `PATCH /v1/trades/{id}/status`).
