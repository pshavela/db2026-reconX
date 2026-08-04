package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.AuditLogRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ============================================================================
 * TICKET-ADV143 — Integration test: end-to-end happy path
 *
 * WHAT:    Boots a real Kafka broker via Testcontainers, publishes 100
 *          distinct TradeEvents through the real TradeEventProducer, and
 *          waits for all 100 to land in audit_log via the real
 *          AuditEventConsumer — no mocks anywhere in the pipeline.
 * HOW:     Awaitility polls auditRepo.count() instead of Thread.sleep, and
 *          asserts on the delta from a captured baseline rather than an
 *          absolute count, so a non-empty seeded database can't fail it.
 * WHY:     A mocked Kafka client would happily pass a unit test and still
 *          fail in production on a topic-name typo, wrong partition count,
 *          missing groupId, or serializer mismatch — this only catches
 *          those because bytes actually cross a real broker.
 * ============================================================================
 *
 * Adapted from the trainer guide's reference, which assumes a
 * TradeEvent.created(tradeRef, JsonNode) static factory and a JsonNode-typed
 * after field. This codebase's actual TradeEvent (see TICKET-ADV130) is a
 * plain record with String before/after and no static factories — matches
 * how TradeService already constructs events (TICKET-ADV129).
 */
@SpringBootTest
@Testcontainers
@Disabled("AuditEventConsumer is disabled by default — see docs/KAFKA.md \"Re-enabling Kafka\"")
class KafkaPipelineIT {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
    );

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private TradeEventProducer producer;

    @Autowired
    private AuditLogRepository auditRepo;

    @Test
    void publishesAndConsumes100Events() {
        long before = auditRepo.count();

        IntStream.range(0, 100).forEach(i -> producer.publish(new TradeEvent(
                UUID.randomUUID(),
                "TRD-IT-" + i,
                TradeEvent.EventType.TRADE_CREATED,
                Instant.now(),
                "integration-test",
                null,
                "{\"price\":" + i + "}"
        )));

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(auditRepo.count()).isEqualTo(before + 100));
    }
}
