# Ticket ADV127 (optional fast-finisher)

Assignee: Lavinia31

## Problem
- ADV119 (memoised `<TradeRow>`) and ADV121 (`useCallback`-stable `onClick`) were both implemented already, but nothing had actually *measured* that the combination pays off — this ticket asks for a before/after comparison, not just code review

## Approach
- No production code changed here — the fix already exists from TICKET-ADV121 (`handleSelect = useCallback((id) => setSelectedId(id), [])` passed to `<TradeRow onClick={handleSelect} />` in `Trades.jsx`)
- To get a real *before* measurement, temporarily reverted `Trades.jsx`'s `onClick` back to an inline arrow (`onClick={(id) => setSelectedId(id)}`, the pre-ADV121 shape) and added a temporary per-row render counter to `TradeRowImpl` (same technique used during ADV119/ADV121's own verification). Measured, then restored both files to their committed state — confirmed via `git status` showing zero diff afterwards

## Notes — the before/after measurement
Same scripted interaction both times (mocked `/api/v1/trades` response with 2 rows, click row 1, click row 1 again, click row 2), via Playwright rather than manually driving the React DevTools Profiler UI — same "measurement, not vibes" evidence, just captured as exact render counts instead of a flame-chart screenshot:

| | Row 1 renders | Row 2 renders |
|---|---|---|
| Initial mount | 2 | 2 |
| **Before** fix — after click row 1 | 4 | 4 |
| **Before** fix — after click row 1 again | 4 | 4 |
| **Before** fix — after click row 2 | 6 | 6 |
| **After** fix — after click row 1 | 2 | 2 |
| **After** fix — after click row 1 again | 2 | 2 |
| **After** fix — after click row 2 | 2 | 2 |

**Why this happens**: without `useCallback`, every click updates `selectedId` state in `Trades`, which re-renders `Trades`, which creates a **brand new** inline arrow function on every render. `React.memo`'s `areEqual` on `<TradeRow>` compares `prev.onClick === next.onClick` — a new function reference every time always fails that check, so **both** rows re-render on **every** click, not just the clicked one. With `useCallback`, the same function reference survives across renders, `areEqual` returns `true`, and neither row re-renders at all (their `trade` prop didn't change either).

**One-sentence summary**: hoisting the inline `onClick` arrow in `Trades.jsx` into a `useCallback` stopped both `<TradeRow>` instances from re-rendering on every click — before the fix, clicking either row re-rendered both rows every time (render count climbing every click); after the fix, render counts never move again.

- Also confirmed live in a real browser with the actual React DevTools Profiler extension (installed for this), using a small temporary harness (two `<TradeRow>`s plus a checkbox to flip between the inline-arrow and `useCallback` versions of `onClick`, removed afterwards):
  - fix **on**: recorded a click, `TradeRowImpl (Memo)` showed up hatched/greyed in the flamegraph with the right-hand panel explicitly stating "Did not render on the client during this profiling session"
  - fix **off** (checkbox unchecked): recording the same click showed `TradeRowImpl (Memo)` as an active, coloured bar with a real measured duration (~0.5ms) — confirming it *did* re-render, for a click that only concerned the other row
  - this is the exact "Highlight updates" / flame-chart evidence the ticket describes, independent of and consistent with the scripted render-counter numbers above
