# Ticket ADV071

Assignee: pshavela

## Problem
- Implement GET endpoint for `/api/v1/audit/trades/{tradeRef}` to inspect the revision history for a specific trade
- In our case (dev), we use a database table `audit_log` which tracks trade amendments
- the endpoint should return the revision list ordered oldest-first

## Approach
- Use Spring's `@GetMapping`
- Use Spring JPAs automatic generation of a query by implementing `JpaRepository<AuditLogEntry, Long>` and defining a `findByTradeRefOrderByEventTimestampAsc(String tradeRef)` method, JPA will automatically generate the corresponding query method