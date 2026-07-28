# Ticket ADV111

Assignee: Lavinia31

## Problem
- The `frontend/` app is built with Vite (the dev-server + build tool for the React project, `npm run dev` / `npm run build`), not Create React App or plain webpack
- The project needs short import paths like `@components/Foo` instead of long relative ones like `../../../components/Foo`
- These paths need to work both when the app runs (Vite) and inside the code editor (no red underlines)

## Approach
- `vite.config.js` already had the alias map for the app itself: `@`, `@components`, `@hooks`, `@services`, `@context`, `@pages`, each pointing to a folder under `src/`
- Added the missing `frontend/jsconfig.json`, which mirrors the same aliases so the editor's IntelliSense/autocomplete understands them too. Without this file, imports work at runtime but the editor shows them as errors
- Added a proxy rule for `/stream` next to the existing `/api` one in `vite.config.js`, so requests to the future live-trade-feed endpoint get forwarded to the backend on port 8080 (needed later by ADV116)

## Notes
- The ticket guide suggested `baseUrl: "."` in `jsconfig.json`, but current TypeScript tooling marks that as deprecated. Used relative paths (`./src/*`) in the `paths` map instead — same result, no warning
- Verified it actually works, not just that it compiles: ran `npm run dev`, the app loaded at `http://localhost:5173`, and checked the browser's dev tools that a `@components/...` import was resolved to the real file with no console errors
