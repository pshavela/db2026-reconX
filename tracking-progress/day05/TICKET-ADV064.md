# Ticket ADV064

Assignee: pshavela

## Problem
- Implement POST endpoint for `/api/v1/trades`

## Approach
- Use Spring's `@PostMapping` and transform user trade request to entity
- implement builder pattern for `Trade`
- map corresponding `instrument` and `counterparty` via their respective repositories
- finally save using the `TradeRepository`