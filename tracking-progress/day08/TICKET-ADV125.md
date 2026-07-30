# Ticket ADV125

Assignee: Lavinia31

## Problem
- No test proves the Dashboard's summary cards actually render with the right values, or that a refactor somewhere in the provider chain (Auth/Theme/Router) doesn't silently break it
- `render(<Dashboard />)` in isolation throws immediately — none of the app's providers exist in a bare test tree

## Approach
- `frontend/src/pages/Dashboard.test.jsx`: a `renderWithProviders(ui)` helper wraps the UI in `AuthProvider` -> `ThemeProvider` -> `MemoryRouter`
- Adapted from the ticket's generic reference in two ways that matter for this actual codebase:
  - `AuthContext` itself isn't exported from `AuthContext.jsx` (only `AuthProvider`/`useAuth` are), so the helper seeds `sessionStorage` (`reconx-token`/`reconx-role`) before rendering and wraps in the real `AuthProvider`, instead of importing a context object that doesn't exist
  - `Dashboard`'s default export is `withAuth(Dashboard)` and it reads trades via `useTradeStream()` internally (SSE), not a `trades` prop — so the hook is mocked with `vi.mock('@hooks/useTradeStream.js', ...)` to return seeded data synchronously, and the seeded session is what lets `withAuth` render the real page instead of redirecting to `/login`
- Assertions use `getByRole('heading', ...)` for all four `<StatCard>` headings (matching this app's actual labels — "Portfolio value", "Trades streamed", "Matched", "Open breaks" — not the generic reference's "Matched trades"/"Unmatched trades"), plus one `getByText` regex check on the computed portfolio value (100*250 + 50*251 = 37,550)

## Notes
- Hit a real environment bug along the way: jsdom (Vitest's test DOM) doesn't implement `window.matchMedia`, so anything wrapped in `<ThemeProvider>` (which calls `matchMedia` to check `prefers-color-scheme` on first load) crashed instantly under test. Fixed with a small polyfill in the shared `frontend/src/test-setup.js` (already tagged for this ticket) — benefits any future test that renders `ThemeProvider`, not just this one
- Confirmed the test is fully synchronous (no `await`/`waitFor` anywhere) and passes with `npx vitest run` alongside the existing `DataTable.test.jsx` (3 tests total, all green)
- Tried reproducing the ticket's stated failure signal ("removing MemoryRouter reproduces `useNavigate() may be used only in the context of a <Router>`") and it did **not** reproduce here — because `withAuth` uses the `<Navigate>` *component* (only rendered on the logged-out branch), and this test authenticates first, so that branch never executes. Kept `MemoryRouter` in the helper anyway since it's still the correct defensive setup (and would matter for a future test of the logged-out redirect path) — just noting the mismatch with the generic ticket description rather than pretending it reproduced
