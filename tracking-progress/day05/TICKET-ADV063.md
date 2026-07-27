# Ticket ADV063

Assignee: pshavela

## Problem
- Implement GET endpoint for `/api/v1/trades`
- must be paginated, filterable and sortable

## Approach
- Use a simple `PagedResponse` wrapper to flatten Spring page content
- delegate request to `TradeService.list(..)` with search constraint parameters
- Use Spring's `Pageable` to limit result size and make trades list sortable

## Notes
- Fetching via `GET` requests fails due to closed hibernate session, this is due `instrument` and `counterparty` being loaded lazily indicated by the annotation `@ManyToOne(fetch = FetchType.LAZY, optional = false)` in entity [Trade.java](../../backend/src/main/java/com/dbtraining/reconx/repository/entity/Trade.java)
- In order to test this must be set to `FetchType.EAGER`