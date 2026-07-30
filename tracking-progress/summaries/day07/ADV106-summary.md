# ADV106 — Advanced data table (sortable, resizable, frozen header)

## What was the task?
Build a trades table on `trades.html` with three behaviours: click-to-sort columns with ascending/descending toggle, drag-to-resize columns from a handle, and a header row that stays visible while the body scrolls.

## How was it solved?
- Created `trades.html` with the same page shell as `dashboard.html` (header, sidebar, main, footer)
- Table uses `<th data-col="..." data-type="...">` attributes to drive sorting; `data-type="number"` triggers numeric sort (subtraction), default is string (`localeCompare`)
- Clicking a `<th>` toggles `aria-sort` between `ascending` and `descending`, clears it on all other headers
- Sort indicators rendered via CSS `::after` pseudo-elements: `th[aria-sort="ascending"]::after { content: " ▲" }`
- Resize handles are `<span class="resize-handle">` inside each `<th>`; `mousedown` captures `startX` and `startWidth`, then `mousemove`/`mouseup` listeners on `document` (not the handle) allow smooth dragging even when cursor leaves the handle
- Frozen header via `position: sticky; top: 0; z-index: 1` on `thead th`, scroll container uses `overflow: auto; max-height: 60vh` with no `overflow: hidden` ancestors
- Row hover highlight via `tbody tr:hover { background: var(--color-border) }`
- Status coloring: MATCHED (green), BREAK (red), PENDING (yellow) via `.status--*` classes
- Demo data (12 trades) loads immediately; API fetch upgrades data when backend is available
- `escapeHtml()` sanitizes all server-supplied strings; `Intl.NumberFormat` formats quantity and price

## Technologies used
- DOM API — `querySelectorAll`, `createElement`, `closest`, event delegation
- `Array.prototype.sort` — in-memory sort on canonical `rows` array, then full `tbody.innerHTML` re-render
- CSS `position: sticky` — frozen header without JS scroll listeners
- CSS `::after` pseudo-elements — sort direction indicators driven by `aria-sort` attribute
- `Intl.NumberFormat` — locale-aware number formatting
- ARIA — `aria-sort`, `aria-current="page"` for accessibility

## Files changed
- `static-dashboard/trades.html` — new page with table markup and page shell
- `static-dashboard/js/trades.js` — new file with sort, resize, and data loading logic
- `static-dashboard/css/style.css` — added `.table-scroll`, `.data-table`, `.resize-handle`, `.status--*`, sort indicator styles
