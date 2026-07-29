# Ticket ADV119

Assignee: Lavinia31

## Problem
- The trades page can render hundreds of rows and gets a new SSE push roughly every second; without memoisation, any unrelated state change anywhere in the parent (a filter dropdown, a side panel) re-renders every single row for no reason

## Approach
- `frontend/src/components/TradeRow.jsx` (new file, no existing scaffold): a plain `TradeRowImpl({ trade, onClick })` rendering one `<tr>` with the fields actually shown (ref, symbol, quantity, price, a status pill), wrapped as `export const TradeRow = React.memo(TradeRowImpl, areEqual)`
- `areEqual(prev, next)` deliberately checks only `trade.id`, `trade.status`, `trade.price`, and `onClick` by reference — not the whole `trade` object and not a `JSON.stringify` shortcut. Any other field on `trade` (e.g. `quantity`) changing does **not** force a re-render, because the row doesn't display anything that depends on it changing independently
- `onClick === onClick` being part of the equality check is intentional per the ticket spec, even though it means the memo won't fully "hold" yet — until TICKET-ADV121 wraps the parent's handler in `useCallback`, a fresh inline arrow on every parent render will still bust the memo. That's expected, documented pre-fix behaviour, not a bug here

## Notes
- Couldn't rely on React's own `<Profiler onRender>` to prove "did this re-render or not" — empirically, `onRender` fired on every parent commit regardless of whether `TradeRow` itself bailed out via memo, so it wasn't a reliable signal here. Used a direct ground-truth instead: a temporary render counter inside `TradeRowImpl` (`window.__tradeRowRenderCount++`), asserted against in a throwaway Vitest test, then removed both — not part of this commit
- With that ground truth, confirmed all 3 "Done when" points precisely:
  - bumping unrelated parent state twice: render count didn't move at all
  - changing `trade.status` or `trade.price`: render count went up by exactly 1 each time
  - changing `trade.quantity` (not part of `areEqual`): render count didn't move, even though the field genuinely changed
  - changing `onClick`'s identity: render count went up by 1 — the expected pre-ADV121 behaviour
- Also tested by hand in a real browser with the same temporary counter visible on screen: same pattern held. One thing worth knowing — under React StrictMode (dev only), each "should re-render" click bumped the counter by 2 instead of 1, because StrictMode intentionally invokes a component's render body twice to catch impure renders; the *ratio* (flat vs. incrementing) is what matters, not the exact number, and this doesn't happen in production builds
