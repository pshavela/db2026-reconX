# Ticket ADV113

Assignee: Lavinia31

## Problem
- If any component throws while rendering, React unmounts the whole tree and the user sees a blank white screen — no way to recover without a full page reload
- Need a reusable wrapper (an HOC, same pattern as `withAuth`) that catches render errors in a page, shows a friendly fallback instead of the white screen, and lets the user retry without reloading

## Approach
- `frontend/src/components/withErrorBoundary.jsx` already had the shell (an `ErrorBoundary` class + the `withErrorBoundary(Component)` factory); filled in the 3 missing pieces:
  - `static getDerivedStateFromError(error)` — returns `{ error }`, so React re-renders the boundary with the caught error in state instead of unmounting everything
  - `componentDidCatch(error, info)` — logs the error (`console.error`, with an eslint-disable comment since the project's lint rules normally forbid `console.*`; in a real prod app this is where you'd forward to Sentry or similar)
  - `render()` — if `state.error` is set, renders a `role="alert"` box ("Something went wrong" + the error message + a "Try again" button); otherwise renders `this.props.children` normally
- "Try again" just calls `this.setState({ error: null })`, which clears the error and makes React attempt to render the children again from scratch

## Notes
- Confirmed with a headless-Chromium Playwright script (not just lint/build): temporarily made `Dashboard` throw on a `?boom=1` query flag, loaded the page, and saw the fallback box render (`role="alert"`, "Something went wrong", "boom", "Try again") instead of a blank screen. Console showed the expected `ErrorBoundary caught Error: boom ...` log
- Clicking "Try again" while the error condition still applies immediately shows the fallback again — this is actually good evidence for the "reusable, not single-shot" requirement: the boundary isn't a one-time trap, it keeps catching
- Also tested manually in a real browser: added a temporary unconditional `throw new Error(...)` at the top of `Dashboard`, confirmed the fallback UI shows instead of a white screen, then removed the temporary line
- No behaviour change for normal use — the boundary only ever does something if a wrapped component actually throws while rendering, so browsing the app normally looks identical to before this ticket
