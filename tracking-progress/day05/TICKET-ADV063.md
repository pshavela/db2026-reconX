# Ticket ADV063

## Problem
- Implement GET endpoint for `/api/v1/trades`
- must be paginated, filterable and sortable

## Solution
- Use a simple `PagedResponse` wrapper to flatten Spring page content
- delegate request to `TradeService.list(..)` with search constraint parameters
- Use Spring's `Pageable` to limit result size and make trades list sortable