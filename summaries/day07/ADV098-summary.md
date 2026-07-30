# ADV098 — Flexbox page shell

## What was the task?
Build the main dashboard layout using Flexbox with nested containers: header on top, sidebar + main content in the middle, footer at the bottom. Stat cards must use Flexbox wrap, not CSS Grid.

## How was it solved?
- `body` is a vertical flex column (`flex-direction: column`)
- A `.layout__body` wrapper creates the horizontal row for sidebar + main
- Sidebar is locked at 240px with `flex: 0 0 240px`
- Main area grows to fill remaining space with `flex: 1 1 auto`
- Stat cards use `flex-wrap: wrap` with a calculated `flex-basis` for 4-across layout
- Added an inline script in `<head>` to prevent theme flash on page load (FOUC)

## Technologies used
- HTML5 semantic elements (`header`, `nav`, `aside`, `main`, `footer`)
- CSS Flexbox (nested containers)
- ARIA roles (`banner`, `complementary`, `main`, `contentinfo`)
- `aria-live="polite"` for the trade feed

## Files changed
- `static-dashboard/dashboard.html`
- `static-dashboard/css/style.css`
