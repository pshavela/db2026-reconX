# Ticket ADV070

Assignee: pshavela

## Problem
- Implement PUT endpoint for `api/v1/recon/results/{breakId}/resolve` to manually resolve a break

## Approach
- Use Spring's `@PostMapping`
- Simply add the note to the `ReconBreak` entity via the corresponding repository and mark it as `RESOLVED`