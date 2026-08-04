# Kafka setup

## Current status: disabled by default

Everything Kafka-related from Day 9 (ADV128-145) is **commented out** in the
backend right now (see "Re-enabling Kafka" below) — producer, consumers, DLQ
error handler, topic auto-creation, and the admin/dlq replay endpoint. The
backend behaves exactly as it did before that work: trade creation doesn't
touch Kafka at all, no publish call, no delay, no dependency on a broker
being reachable.

This is a deliberate default, not a bug: running Kafka + Zookeeper is one
more thing every teammate has to remember to start, and until recently a
real bug (fixed — see the note in `TradeEventProducer.java`) made the
backend hang for up to a minute per request whenever Kafka wasn't reachable.
That specific bug is fixed, but the team decided to keep Kafka opt-in rather
than mandatory for local dev.

## Bringing Kafka up

```bash
docker compose up -d zookeeper kafka
```

Wait for the container to report healthy (it has a built-in healthcheck),
then optionally bring up Kafdrop to browse topics/messages in a browser:

```bash
docker compose --profile debug up -d kafdrop
# http://localhost:9000
```

## Creating topics

Normally topics are created automatically at backend startup, from the
`NewTopic` beans in `KafkaTopicsConfig.java` (currently commented out along
with everything else — see below). If you want topics to exist *before*
starting the backend — e.g. to poke around in Kafdrop, or to rule out
"topics don't exist yet" while debugging — run:

```bash
./scripts/kafka-init-topics.sh
```

This creates the 4 topics directly against the `reconx-kafka` container with
the correct partition counts (does **not** rely on Kafka's own
`KAFKA_AUTO_CREATE_TOPICS_ENABLE`, which would create them with only 1
partition on first use):

| Topic              | Partitions |
|---------------------|-----------:|
| `trade-events`       | 3 |
| `recon-results`       | 2 |
| `system-alerts`       | 1 |
| `trade-events-dlq`    | 3 |

## Re-enabling Kafka

Uncomment the following (all currently prefixed with `// ` and a
`DISABLED — see docs/KAFKA.md` marker comment):

- `backend/src/main/java/com/dbtraining/reconx/kafka/KafkaTopicsConfig.java` — `@Configuration`, `@Profile`, and the 4 `@Bean` methods
- `backend/src/main/java/com/dbtraining/reconx/kafka/KafkaErrorHandlerConfig.java` — `@Configuration` and `@Bean`
- `backend/src/main/java/com/dbtraining/reconx/kafka/AuditEventConsumer.java` — `@Component` and `@KafkaListener`
- `backend/src/main/java/com/dbtraining/reconx/kafka/AlertConsumer.java` — `@Component` and `@KafkaListener`
- `backend/src/main/java/com/dbtraining/reconx/kafka/ReconciliationConsumer.java` — `@Component` and `@KafkaListener`
- `backend/src/main/java/com/dbtraining/reconx/kafka/DlqConsumer.java` — `@Component` and `@KafkaListener`
- `backend/src/main/java/com/dbtraining/reconx/kafka/TradeEventProducer.java` — `@Component`
- `backend/src/main/java/com/dbtraining/reconx/controller/DlqAdminController.java` — `@RestController`, `@RequestMapping`, `@PreAuthorize`, `@Tag`, `@SecurityRequirement`
- `backend/src/main/java/com/dbtraining/reconx/service/TradeService.java` — the `events` field, the `TradeEventProducer events` constructor param, and the `events.publish(...)` block (plus its now-orphaned `beforeJson` line) in each of `create`/`update`/`updateStatus`/`softDelete`

The producer's `publish()` method itself was fixed while disabled (see the
comment block at the top of `TradeEventProducer.java`): it now defers the
actual Kafka send until after the caller's DB transaction commits, and
`max.block.ms` is capped at 3s (`application.yml`), so once you re-enable
it, a missing broker no longer blocks trade creation or holds a DB lock —
it just means published events silently never arrive anywhere, which only
matters once you also re-enable the consumers above.

After uncommenting, bring Kafka up (see above) before starting the backend,
or startup will log warnings while `KafkaTopicsConfig` tries to reach a
broker that isn't there yet.

Also remove the `@Disabled(...)` annotation from these two integration
tests — they exercise the consumers above via a real Testcontainers-managed
broker and will fail (correctly — the beans genuinely won't be there) until
you do:

- `backend/src/test/java/com/dbtraining/reconx/kafka/KafkaPipelineIT.java`
- `backend/src/test/java/com/dbtraining/reconx/kafka/DlqRoutingIT.java`

## Troubleshooting

**"Timeout trying to lock table trades" when creating a trade** — this was
a real bug (see the comment block at the top of `TradeEventProducer.java`),
fixed by publishing after commit instead of inside the transaction. If you
see it again after re-enabling Kafka, it's a regression, not expected
behavior.

**Backend seems to hang when Kafka isn't running** — check that
`max.block.ms: 3000` is still present under `spring.kafka.producer.properties`
in `application.yml`. Without it, a Kafka publish attempt with no reachable
broker blocks for the Kafka client default of 60 seconds.
