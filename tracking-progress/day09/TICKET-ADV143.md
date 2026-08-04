# Ticket ADV143

Assignee: Lavinia31

## Problem
- Needed a `@SpringBootTest` that boots real Kafka via Testcontainers, publishes 100 events, and waits for 100 audit rows via Awaitility (not `Thread.sleep`) — no such test existed
- The guide's reference assumes a `TradeEvent.created(tradeRef, JsonNode)` static factory and `JsonNode`-typed `after` field, neither of which exist on this codebase's actual `TradeEvent` (a plain record with `String before/after`, no factories — same mismatch already found in ADV130/ADV129)
- `awaitility` wasn't a dependency anywhere in `pom.xml`, not even transitively via `spring-kafka-test`/testcontainers

## Approach
- Wrote `KafkaPipelineIT`: boots `KafkaContainer` via Testcontainers, constructs `TradeEvent` directly via its real constructor (matching how `TradeService` already does it), publishes 100 distinct events, awaits `auditRepo.count()` growing by exactly 100 (delta from a captured baseline, not an absolute count, so a non-empty seeded database can't fail it)
- Added the `awaitility` test dependency explicitly

## Notes
- Initially could not run this (or the two pre-existing Testcontainers tests) at all: "Could not find a valid Docker environment" even against a freshly reinstalled Docker Desktop, and even after forcing `DOCKER_HOST` to the active context's real named pipe. Root-caused to a genuine version incompatibility between Testcontainers 1.20.3's bundled `docker-java` and this Docker Desktop release — fixed by bumping to Testcontainers 1.21.4 (see the separate pom.xml commit), after which this test (and the two older ones) went green
- Verified live: 100 events through a real broker in 27.65s, under the 30s Awaitility budget
