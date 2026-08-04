# Ticket ADV139

Assignee: Lavinia31

## Problem
- No Kafka client metrics (`kafka_consumer_*`, `kafka_producer_*`) were exposed on `/actuator/prometheus` — needed for the Grafana panels in ADV140-142
- `prometheus` was already in `management.endpoints.web.exposure.include` and `spring.json.trusted.packages` was already set from earlier Kafka work, so those parts of the guide's checklist were already satisfied

## Approach
- Enabled `management.metrics.binders.kafka.enabled: true` — this is all that was actually needed; Spring Boot auto-wires `MicrometerConsumerListener`/`MicrometerProducerListener` onto the already-configured consumer/producer factories on its own
- Deliberately did **not** add the guide's other suggested property, `spring.kafka.consumer.properties.metric.reporters: io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics` — tried it first and it crashes the app on startup (`NullPointerException` inside `ClassicKafkaConsumer`'s constructor cleanup path). `KafkaClientMetrics` is a Micrometer wrapper meant to be bound programmatically via `bindTo(registry)`, not a Kafka-native `MetricsReporter` — configuring it as one makes Kafka try to instantiate it via that lifecycle, which fails

## Notes
- Verified live: after publishing events, `kafka_producer_record_send_total` and multiple `kafka_consumer_*` series appeared on `/api/actuator/prometheus`, correctly tagged `application="reconx"`
- Important for the Grafana panels that follow: on this project's pinned `kafka-clients` 3.9.1, the consumer-side metrics are under the `kafka_consumer_fetch_manager_*` prefix (`records_lag`, `records_consumed_total`, `fetch_total`) — not the bare `kafka_consumer_records_lag` / `kafka_consumer_fetch_total` names the ticket's checklist literally names. ADV140/141/142 all use the real, `fetch_manager`-prefixed names
