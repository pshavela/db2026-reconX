# Ticket ADV117

Assignee: Lavinia31

## Problem
- The search box on the trades page (and the instrument lookup on `/trades/new`, ADV123) shouldn't fire an API call on every single keystroke — need a hook that only "settles" on a value once typing has paused for a bit

## Approach
- `frontend/src/hooks/useDebouncedSearch.js`: one `useState` holding the debounced value (seeded from the incoming `query` so the first render already has something usable), and one `useEffect` with deps `[query, delay]` that schedules `setTimeout(() => setDebounced(query), delay)`
- The effect's cleanup calls `clearTimeout` on that timer. Since React runs the cleanup before every re-run of the effect, this means: every new keystroke cancels the previous pending update before scheduling its own — only the last keystroke in a burst actually survives long enough to fire

## Notes
- Verified with a real test (Vitest + `@testing-library/react`'s `renderHook`, fake timers), not just that it compiles — written as a throwaway test file, run, then deleted (not part of this commit):
  - simulated typing "A" -> "AA" -> "AAP" -> "AAPL" with ~50ms between keystrokes: the debounced value stayed at the old value the whole time, and only updated to "AAPL" once the full delay had passed since the *last* keystroke
  - changed the `delay` argument mid-flight (from 300ms to 1000ms) while a timer was already pending: confirmed the old 300ms timer never fires (value stays old even after 300ms pass) and the new 1000ms window is what actually applies — proves the cleanup really cancels the stale timer instead of leaking it
- Found (while doing this ticket) that `Trades.jsx` already calls `useDebouncedSearch(search, 300)`, but the `useEffect` that's supposed to actually fetch trades using the debounced value was still a TODO jointly tagged `TICKET-ADV114 + TICKET-ADV117` — nothing in any later Day 8 ticket picks it up either. Decision: finish it now as part of this ticket (see the follow-up commit) rather than leave a fetch pipeline half-wired with no owner
