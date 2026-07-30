# Ticket ADV112

Assignee: Lavinia31

## Problem
- Need a way to block a page from rendering if the user isn't logged in, and send them to `/login` instead — before the protected page ever shows up on screen

## Approach
- `withAuth(Component)` in `frontend/src/components/withAuth.jsx`: it's a function that takes a page component and returns a wrapped version. The wrapped version checks `user` from `useAuth()` — if there's no user, it renders `<Navigate to="/login" replace />` instead of the page; if there is a user, it renders the real page normally
- Pages opt in by exporting `withAuth(Dashboard)` / `withAuth(Trades)` / `withAuth(AddTrade)` instead of the plain component — this was already wired in `Dashboard.jsx`, `Trades.jsx`, `AddTrade.jsx`
- `withAuth` on its own has nothing to check unless `user` actually exists somewhere, so also finished two other stub files that were tagged for this same ticket:
  - `AuthContext.jsx`: keeps `user` in React state, backed by the browser's `sessionStorage` under the keys `reconx-token` and `reconx-role`. On page load it reads those keys back so a refresh doesn't log you out. `login(token, role)` saves both and updates the state; `logout()` clears both and resets the state to `null`
  - `apiService.js`: `authHeaders()` reads the token from `sessionStorage` and returns the `Authorization: Bearer <token>` header (or nothing, if there's no token); `request()` is the shared fetch helper every API call will use later (sets JSON headers, throws a clear error when the response isn't ok, returns `null` for empty 204 responses)

## Notes
- Confirmed this works in a real browser, not just that it compiles: with `sessionStorage` empty, visiting the app always lands on `/login`, and none of Dashboard/Trades/Add trade can be reached from the nav links. Manually setting `reconx-token` + `reconx-role` in the browser console and reloading lets the pages through
- The login form itself (`Login.jsx`) doesn't call `AuthContext.login(...)` yet — wiring the "Sign in" button to an actual API call is a separate ticket (ADV072), so typing something into the form and clicking "Sign in" doesn't grant access on its own
- `/dashboard` isn't a real route in `App.jsx` — unknown paths fall through to a wildcard rule that redirects to `/`, which is where the (protected) Dashboard actually lives. So testing `/dashboard` still exercises `withAuth` correctly, just through that redirect
