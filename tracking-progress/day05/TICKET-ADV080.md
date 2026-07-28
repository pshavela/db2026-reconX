# Ticket ADV080

Assignee: alexandraelenadumitrescu
Status: Completed

## Problem
- Establish `/api/v1/...` as the contract prefix and demonstrate deprecation of a retired endpoint

## Approach
- All controllers except `AuthController` already carried the `/v1/...` prefix
- Added `LegacyTradesController` mapped at `/v0/trades`, returning `410 Gone` with `Deprecation`, `Sunset`,
  and `Link` headers
- Short "API versioning" section added to the top-level README explaining the rule

## Notes
- Deliberately did NOT version `AuthController` (`/auth`, no `/v1`). It's the same file being worked on for
  ADV072-074 (JWT + RBAC), and changing its path would also break the documented `POST /api/auth/login`
  contract. Left as a flagged follow-up rather than touching shared in-progress code
- Verified live: `curl -i /api/v0/trades` → 410 with all three headers; `curl -i /api/v1/trades` → 200
