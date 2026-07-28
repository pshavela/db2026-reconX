# Ticket ADV065

Assignee: pshavela

## Problem
- Implement PUT endpoint for `/api/v1/trades/{id}` to update existing trade

## Approach
- Use Spring's `@PutMapping` and update `Trade` Entity
- Add custom `CounterpartyNotFoundException` and `InstrumentNotFoundException` in case request contains invalid counterparty/instrument fields; Attach them in `GlobalExceptionHandler`
- Fix error: Return `ResponseEntity<TradeResponse>` instead of previous `TradeResponse`, otherwise aforementioned exceptions will not be translated into 404s