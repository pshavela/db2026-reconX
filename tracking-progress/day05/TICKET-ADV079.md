# Ticket ADV079

Assignee: alexandraelenadumitrescu
Status: Completed

## Problem
- Verify every Liquibase changeset applies cleanly on a fresh Postgres and seed data lands

## Approach
- `LiquibaseMigrationsIntegrationTest` — `@SpringBootTest @Testcontainers` with its own `PostgreSQLContainer`
  (same `@DynamicPropertySource` pattern as the existing `ReconciliationIntegrationTest`), `JdbcTemplate`
  queries against `databasechangelog`, `counterparties`, `users`

## Notes
- The ticket's original assumption ("trades → 500 seeded") doesn't hold: `db/seed_data.sql` (500 trades) is
  a standalone script, never wired into the Liquibase changelog (see ADV017) — a fresh container has 0 rows
  in `trades`. Asserted on `counterparties` (10) and `users` (4) instead, which 008-seed.xml actually loads
- Testcontainers couldn't find Docker on this machine ("Could not find a valid Docker environment") even
  though `docker info`/`docker ps` worked fine from the shell. Same fix already logged under ADV044:
  `echo api.version=1.44 >> ~/.docker-java.properties`
