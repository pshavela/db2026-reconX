# Ticket ADV120

Assignee: Lavinia31

## Problem
- The Dashboard recomputes the portfolio value on every render, even when nothing trade-related changed (a theme toggle, opening a panel) — wasteful once ADV116's SSE feed is streaming trades in continuously

## Approach
- Only `portfolioValue` gets wrapped in `useMemo`, keyed on `[trades]` — it's a `reduce` over up to 200 streamed trades, genuinely worth caching
- `matched` and `breaks` stay as plain `trades.filter(...).length` calls, **without** memo — this matches both the shipped stub's own wording and the ticket's reference solution. A filter+length isn't expensive enough to be worth the extra complexity of a second `useMemo` (the ticket's own Hint 1 warns against reaching for `useMemo` on cheap calculations)

## Notes
- Verified two ways, not just that it compiles:
  - **Automated**: mocked `useTradeStream` to return a stable `trades` array reference, added a temporary counter inside the `useMemo` factory, rendered `Dashboard` (wrapped in `AuthProvider`, with a fake session in `sessionStorage`) inside a harness with its own unrelated re-render trigger. Confirmed the counter stayed flat across unrelated re-renders (same `trades` reference) and went up by exactly 1 when `trades` actually changed. Counter and harness removed afterwards, not part of this commit
  - **Manual**: temporarily let `Dashboard` accept a `streamUrl` override, pointed it at a local SSE test server (same one built for TICKET-ADV116) that emits one new trade per second, and showed the recompute count live on screen next to an "unrelated state" button. Watched it climb roughly once per second (matching real trade arrivals) and confirmed mashing the unrelated button didn't add extra recomputes beyond that. All of this reverted afterwards — `Dashboard.jsx` only carries the real `useMemo`, no test-only code
