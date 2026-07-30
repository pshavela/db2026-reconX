# Ticket ADV095

Assignee: alexandraelenadumitrescu
Status: Completed

## Problem
- No reusable way to package the audit-event publisher as a Spring Boot
  starter that other services could pull in without copy-pasting the wiring.

## Approach
- New sibling Maven module `recon-audit-starter/` at the repo root (not a
  reactor module of `backend/` — `backend/pom.xml` stays the single
  runnable project per TICKET-ADV048's deliberate decision; the starter is
  a fully independent project consumed as a plain Maven dependency).
- `recon-audit-starter/pom.xml` — same `spring-boot-starter-parent` (3.5.0)
  as `backend`, depends on `spring-boot-autoconfigure` +
  `spring-boot-configuration-processor`.
- Three classes under `com.dbtraining.reconx.audit`:
  `AuditAutoConfiguration` (`@AutoConfiguration`, `@ConditionalOnClass`,
  `@ConditionalOnProperty(prefix = "reconx.audit", name = "enabled",
  matchIfMissing = true)`, `@EnableConfigurationProperties`), `AuditProperties`
  (`@ConfigurationProperties("reconx.audit")`, `enabled` + `topic`), and
  `AuditEventPublisher` (constructor-injected `ApplicationEventPublisher` +
  `AuditProperties`).
- Discovery file:
  `recon-audit-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- `backend/pom.xml` — added a plain dependency on
  `com.dbtraining.reconx:recon-audit-starter:1.0.0`.
- `.github/workflows/backend.yml` — added a step that builds + installs
  `recon-audit-starter` before `mvn verify` runs (CI's working directory is
  `backend/`, so the starter has to be built first or dependency resolution
  fails).
- `application.yml` — added `beans` to `management.endpoints.web.exposure.include`,
  needed to verify bean presence/absence via `/actuator/beans`.

## Deviations from the ticket's own instructions
- The guide's "Run the project" section uses `../mvnw` from inside
  `recon-audit-starter/`, assuming a root-level Maven wrapper. This repo has
  no root `pom.xml`/`mvnw` (only `backend/` has one) — used
  `../backend/mvnw -f pom.xml` instead.
- Passing `-Dreconx.audit.enabled=false` directly on the `./mvnw` command
  line does **not** reach the Spring app — it only sets a Maven-process
  system property, not one forwarded to the forked JVM `spring-boot:run`
  starts. The property has to go through
  `-Dspring-boot.run.arguments=--reconx.audit.enabled=false` instead.

## How to demonstrate it works

1. Build and install the starter standalone:
   ```powershell
   cd recon-audit-starter
   ../backend/mvnw -f pom.xml clean install
   jar tf target/recon-audit-starter-1.0.0.jar | Select-String "AutoConfiguration.imports"
   ```
   Confirmed: JAR installs to `~/.m2/repository/com/dbtraining/reconx/recon-audit-starter/1.0.0/`,
   and the imports file is present inside the JAR.

2. Confirm the bean auto-wires with default config:
   ```powershell
   docker compose stop backend   # free port 8080 if the container is running
   cd backend
   ./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```
   In another terminal:
   ```powershell
   curl.exe -s http://localhost:8080/api/actuator/beans | Select-String "auditEventPublisher"
   ```
   **Confirmed present** — 2026-07-29.

3. Confirm it disappears when disabled:
   ```powershell
   ./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.arguments=--reconx.audit.enabled=false"
   curl.exe -s http://localhost:8080/api/actuator/beans | Select-String "auditEventPublisher"
   ```
   **Confirmed absent** (no match) — 2026-07-29.

4. Full backend test suite still green after adding the new dependency:
   ```powershell
   ./mvnw test
   ```
   **20/20 tests passed, BUILD SUCCESS** — 2026-07-29, no regressions.
