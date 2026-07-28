# Ticket ADV066

Assignee: pshavela

## Problem
- Implement PATCH endpoint for `/api/v1/trades/{id}/status` to only modify status of a specific trade

## Approach
- Use Spring's `@PatchMapping`
- implement custom `record StatusUpdate( @NotBlank @Pattern(regexp = "PENDING|MATCHED|...") String status)` as a JSON request body with status constraints
- Add `@Valid @RequestBody StatusUpdate statusUpdate` as a parameter to the `TradeController`'s patch method -> throws 400 if JSON request body is invalid or does not contain a `status` key
- Delegate to `TradeService` for business logic