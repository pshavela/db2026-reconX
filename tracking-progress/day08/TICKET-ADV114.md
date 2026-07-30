# Ticket ADV114

Assignee: Lavinia31

## Problem
- Need one reusable table component (sortable header, body, pagination) instead of building a separate one-off table for trades, breaks, audit log, etc.
- The parts (Header/Body/Pagination) should share state without the caller having to pass 14 props by hand — a "compound component", the same pattern as a native `<select>` sharing state with its `<option>` children

## Approach
- `frontend/src/components/DataTable.jsx` already had the shape: a `DataTableContext` plus `DataTable.Header` / `DataTable.Body` / `DataTable.Pagination` attached as static properties on the `DataTable` function, and the root `DataTable` already wrapped `children` in the context provider. Filled in the 3 sub-components:
  - `Header({ columns })`: reads `sort` + `onSortChange` from context, renders one button per column; the currently-sorted column gets an `active` class, the rest get `idle`; clicking a button calls `onSortChange(columnKey)`
  - `Body({ rows, render })`: maps `rows` and calls the caller-supplied `render(row)` for each one, wrapped in a keyed row `div`
  - `Pagination({ page, totalPages, onChange })`: prev/next buttons, disabled at the first/last page, calling `onChange(page ± 1)`
- Note: this is a "controlled" compound component — `DataTable` itself doesn't own sort/page state internally, it just provides whatever `sort`/`page`/`onSortChange` the caller passes down through context. The caller (e.g. a future `Trades` page) is expected to own the actual state and pass it in

## Notes
- Kept the scope to `DataTable.jsx` only. The page that will actually use it, `Trades.jsx`, still has its own unfinished pieces (fetching data, rendering `<DataTable.Body>` with real rows) — some of that is jointly tagged with TICKET-ADV117 (debounced search), which isn't part of this ticket
- The existing RTL test (`DataTable.test.jsx`) passes, but its assertions were still TODO stubs (empty) — so a passing test alone didn't prove the behaviour. Wrote a throwaway test file with real assertions to actually check: columns/rows render, clicking a header calls `onSortChange` with the right key, the active column gets the `active` class (not `idle`), the table still works with no `<DataTable.Pagination />`, and `Pagination` disables prev/next correctly at the first/last page. All 5 passed, then the scratch file was deleted (it isn't part of this commit — writing the real RTL assertions is TICKET-ADV125's job)
- Lint clean, production build (`npm run build`) succeeds
