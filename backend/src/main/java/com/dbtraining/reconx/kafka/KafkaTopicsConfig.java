package com.dbtraining.reconx.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

/**
 * ============================================================================
 * TICKET-ADV128 — Declare Kafka topics on app start (3 main topics + DLQ)
 * TICKET-ADV134 — DLQ topic must be pre-declared for DeadLetterPublishingRecoverer
 *
 * WHAT:    Creates 4 topics if they don't already exist (trade-events,
 *          recon-results, system-alerts, trade-events-dlq).
 * HOW:     NewTopic @Bean instances are picked up by KafkaAdmin which
 *          creates them at application startup.
 * WHY:     Auto-create in code keeps `docker compose up` -> "ready" in one
 *          step. No manual `kafka-topics --create` ceremony.
 * OBSERVE: Kafdrop (http://localhost:9000) lists all 4 topics after startup,
 *          with the right partition counts.
 * ============================================================================
 *
 * Deviates from the guide's own hint, which scopes this @Profile("!dev &
 * !test") on the reasoning that "in dev ... the compose stack is expected to
 * have the topics already". In practice that just means the broker's
 * KAFKA_AUTO_CREATE_TOPICS_ENABLE=true silently creates them on first use
 * with the default partition count (1) — not the 3/2/1/3 this ticket
 * actually asks for. Scoped to "!test" instead so this still runs in dev
 * (where we've verified everything else in this session) while staying out
 * of the way of Testcontainers-managed Kafka in integration tests, which
 * gets its own fresh auto-created topics per test run.
 *
 * recon-results is declared per the ticket, but nothing in this codebase
 * publishes or consumes it yet — ReconciliationConsumer (TICKET-ADV131)
 * only logs the trigger. It's provisioned infrastructure for a future
 * ticket, not wired to anything today.
 * ============================================================================
 *
 *  DISABLED — see docs/KAFKA.md "Re-enabling Kafka" to turn this back on.
 *  Topics can still be created independently of the backend via
 *  scripts/kafka-init-topics.sh, with the same partition counts as below.
 * ============================================================================
 */
// @Configuration
// @Profile("!test")
public class KafkaTopicsConfig {

    // @Bean
    public NewTopic tradeEvents() {
        return TopicBuilder.name("trade-events").partitions(3).replicas(1).build();
    }

    // @Bean
    public NewTopic reconResults() {
        return TopicBuilder.name("recon-results").partitions(2).replicas(1).build();
    }

    // @Bean
    public NewTopic systemAlerts() {
        return TopicBuilder.name("system-alerts").partitions(1).replicas(1).build();
    }

    // @Bean
    public NewTopic tradeEventsDlq() {
        return TopicBuilder.name("trade-events-dlq").partitions(3).replicas(1).build();
    }
}
