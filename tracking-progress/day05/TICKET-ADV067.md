# Ticket ADV067

Assignee: pshavela

## Problem
- Implement DELETE endpoint for `/api/v1/trades/{id}` to soft-delete a trade, ie. removing it user-facing but preserve it in the database

## Approach
- Use Spring's `@DeleteMapping`
- Set `@SQLRestriction("deleted_at IS NULL")` on the `Trade` entity, so it is ignored when `deleted_at` is not NULL
- Provide a `void soft_delete()` method which sets `deleted_at` to the current time-stamp, effectively hiding the entity