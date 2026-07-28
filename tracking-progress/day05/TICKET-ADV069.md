# Ticket ADV069

Assignee: pshavela

## Problem
- Implement GET endpoint for `/api/v1/recon/jobs/{jobId}/results` to view all `ReconBreak`s for a specific job

## Approach
- Implementation deferred for now, `recon_jobs` must be wired with `recon_breaks` and jobs should be runnable
- Simple stub that returns all breaks in a paginated fashion using `PagedResponse.from(breaks.findAll(pageable), it-> it)`