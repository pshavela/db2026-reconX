# Ticket ADV123

Assignee: Lavinia31

## Problem
- The `/trades/new` page needs a real form (trade ref, instrument, counterparty, asset class, side, quantity, price, trade date) that validates client-side before ever hitting the network, and sends correctly-typed data (numbers, not number-strings) on success

## Approach
- `frontend/src/pages/AddTrade.jsx`: filled in the Yup schema (the shell + `useForm`/`yupResolver` wiring already existed) covering every field — `tradeRef` (regex `AAA-YYYYMMDD-NNNN`), `instrumentId`/`counterpartyId` (positive integers), `assetClass`/`side` (`oneOf` enums), `quantity`/`price` (positive numbers), `tradeDate`
- One deliberate deviation from the ticket's generic reference: kept `tradeDate` as `yup.string()`, not `yup.date()`. The `<input type="date">` already produces `"YYYY-MM-DD"`, exactly what the backend's `LocalDate` field expects. Casting it to a JS `Date` and `JSON.stringify`-ing that would send a full ISO datetime instead, which Spring can't parse as a `LocalDate`
- Added every remaining input (`register()`'d, uncontrolled), a `role="alert"` message under each one that has a Yup error, `mode: 'onBlur'` so errors don't scream on the first keystroke, and a generic submit-error message if the API call itself fails
- `api.createTrade(req)` in `apiService.js`: was a stub, now just `request('POST', '/v1/trades', req)` using the shared fetch wrapper from ADV112

## Notes
- Verified against the real backend (dev/H2 profile), not just that it compiles:
  - submitting the empty form fired **zero** network requests and rendered 6 distinct `role="alert"` messages, one per required field
  - filling in a valid form and submitting sent a real `POST /api/v1/trades`, got back `201`, and the request body had `quantity: 1000` and `price: 245.5` as actual JSON numbers (not `"1000"`/`"245.5"` strings) — confirmed by parsing the captured request body in the test, not just eyeballing the Network tab
  - added a temporary render counter (removed after) and typed ~30 characters into a field with no blur in between: **zero** re-renders, confirming RHF stays uncontrolled. Only blurring the field (which is when `mode: 'onBlur'` validation actually runs) produced a re-render — exactly the expected split between "typing" and "validating"
- Also confirmed by hand in a real browser: same empty/valid form behaviour, submit succeeded and the form cleared
- Known limitation, not something to fix here: after a successful create, the new trade won't show up on `/trades` because `GET /v1/trades` hits the pre-existing `LazyInitializationException` bug (tracked separately in `day05/TICKET-ADV063.md`). The trade genuinely gets persisted (verified via a direct backend request), the frontend list just can't render it yet
