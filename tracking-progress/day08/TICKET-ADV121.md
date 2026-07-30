# Ticket ADV121

Assignee: Lavinia31

## Problem
- TICKET-ADV119's `React.memo` on `<TradeRow>` compares `onClick` by reference. As long as the parent (`Trades.jsx`) passes a fresh inline arrow function on every render, that comparison always fails and the memo never actually holds — every parent re-render busts every row's memo
- `Trades.jsx` also didn't use `<TradeRow>` at all yet — rows were still rendered as inline `<span>`s passed to `DataTable.Body`'s `render` prop, left that way intentionally when ADV114/ADV117 were done

## Approach
- `Trades.jsx`: added `const [selectedId, setSelectedId] = useState(null)` and `const handleSelect = useCallback((id) => setSelectedId(id), [])` — a stable reference across renders — then swapped the inline row markup for `<TradeRow trade={t} onClick={handleSelect} />`. A small `Selected trade id: {selectedId}` line shows the result
- Bug found and fixed along the way in `TradeRow.jsx` (from ADV119): it rendered a literal `<tr>`/`<td>`, but `DataTable`'s row wrapper is a `<div className="data-table__row">` (CSS `display: grid`, 5 columns), not a `<table>`. A `<tr>` inside a `<div>` is invalid HTML — confirmed by React itself logging "In HTML, `<tr>` cannot be a child of `<div>`. This will cause a hydration error." Fixed by rendering `<span>`s instead, with the click handler on a wrapping `<span style={{ display: 'contents' }}>` — `display: contents` means that wrapper doesn't participate in the grid layout at all (its children become the direct grid items), so it doesn't break the 5-column alignment while still giving `onClick` somewhere valid to live

## Notes
- Verified with Playwright against a mocked `/api/v1/trades` response (the real backend can't list trades yet — pre-existing `LazyInitializationException` bug, tracked separately in day05/TICKET-ADV063.md):
  - confirmed zero console warnings after the markup fix (there were exactly the hydration warnings described above before it)
  - screenshotted the rendered table — columns aligned correctly, `display: contents` didn't break anything visually
  - added a temporary per-row render counter (removed after) and clicked row 1, then row 1 again, then row 2: render counts for **both** rows stayed completely flat across all three clicks, proving the memo now genuinely holds — clicking a row updates `selectedId` in the parent (forcing `Trades` to re-render) without forcing either `<TradeRow>` to re-render, since neither `trade` nor `onClick` changed
- Also tested manually in a real browser with a small two-row harness (temporary, removed): render counters for both rows next to the rows themselves, clicked back and forth between them and mashed an "unrelated state" button — counters never moved
