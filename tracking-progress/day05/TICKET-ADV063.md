# Ticket ADV063

Assignee: pshavela

## Problem
- Implement GET endpoint for `/api/v1/trades`
- must be paginated, filterable and sortable

## Approach
- Use a simple `PagedResponse` wrapper to flatten Spring page content
- delegate request to `TradeService.list(..)` with search constraint parameters
- Use Spring's `Pageable` to limit result size and make trades list sortable