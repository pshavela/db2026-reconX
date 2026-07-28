# Ticket ADV068

Assignee: pshavela

## Problem
- Implement a POST endpoint for `/api/v1/recon/run` which triggers a reconciliation job run
- Timeframe `(from, to)` and `counterpartyId` must be specified in the request body as JSON

## Approach
- Use Spring's `@PostMapping` for the `ReconController` method
- implement a `ReconJob` entity, `ReconJobRepository` repository and a `ReconJobService` which generates a random job ID and persists it to the `recon_jobs` database via the repository
- implement a small `ReconJobResponse` DTO mapper with two fields, `jobId` and
`status`