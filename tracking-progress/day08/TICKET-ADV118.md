# Ticket ADV118

Assignee: Lavinia31

## Problem
- Need a hook that calls `loadMore()` automatically when the user scrolls near the bottom of a list — without polling `window.onscroll` by hand, and without recreating the whole observer every time the caller passes a new `loadMore` function

## Approach
- `frontend/src/hooks/useInfiniteScroll.js` returns a `sentinelRef` — the caller attaches it to an empty marker element at the bottom of the list
- Two separate effects, on purpose:
  - one effect (deps `[loadMore]`) just writes the latest `loadMore` into a `loadMoreRef` — cheap, runs on every render where the callback identity changes, no side effects beyond updating a ref
  - a second effect (deps `[rootMargin]`, which almost never changes) creates the actual `IntersectionObserver` **once**, observes the sentinel, and calls `loadMoreRef.current()` only when `entries[0].isIntersecting` is true
- Splitting it this way is the whole point of the ticket: the observer (an expensive, stateful browser API object) doesn't get torn down and recreated just because the parent re-rendered and made a new `loadMore` closure — but the callback that actually fires is still always the newest one, never a stale one captured back when the observer was first created
- Cleanup calls `observer.disconnect()`

## Notes
- `IntersectionObserver` doesn't work meaningfully in Vitest's default jsdom environment, so this was verified in a real headless-Chromium session instead (Playwright), using a temporary test page (deleted afterwards, not part of this commit) with a tall filler + a sentinel div
- Wrapped the browser's real `IntersectionObserver` constructor to count `create`/`disconnect` calls without changing its actual behaviour, then:
  - clicked a "bump" button 3 times (each click gives `loadMore` a new identity via a `gen` counter it closes over) *before* ever scrolling — confirmed the observer was still only created once (no churn from the `loadMore` identity changing)
  - scrolled the sentinel into view, then away, then back into view again — got exactly 2 `loadMore` calls (one per intersection, not one per scroll event), and **both** carried the latest `gen` value (3), proving the ref-based approach avoids the stale-closure trap that a naive `useEffect([loadMore])`-only version would fall into
  - unmounted the consumer — observer's `disconnect()` fired exactly once
- Also tested by hand in a real browser: scrolled a similar harness page and watched a visible counter increment once per pass over the sentinel, then confirmed nothing further happened after clicking "unmount consumer"
- Same caveat as ADV115/ADV116: with React StrictMode enabled (dev only), the observer briefly showed as "created twice" during the mount phase — re-confirmed clean single-creation behaviour with StrictMode temporarily removed. This is the same known StrictMode + lazy-route double-invoke artifact, not a bug, and doesn't happen in production builds
