package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.dbtraining.reconx.repository.entity.DlqMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * ============================================================================
 * TICKET-ADV136 — DLQ consumer
 *
 * WHAT:    Listens on `trade-events-dlq` (the topic ADV134's
 *          DeadLetterPublishingRecoverer publishes to on final failure) and
 *          persists a DlqMessage row per poisoned record.
 * HOW:     @KafkaListener on trade-events-dlq, groupId `dlq-monitor` — a
 *          distinct group from recon-service/audit-service so this consumer
 *          only ever sees DLQ traffic, never the main trade-events topic.
 * WHY:     Without a durable record, an operator has no way to see what
 *          failed, or to replay a single event by id via
 *          DlqAdminController — see TICKET-ADV136.
 * OBSERVE: Force an exception in a listener (e.g. AuditEventConsumer) and,
 *          after ADV135's retries exhaust, a row appears here with the
 *          original topic/partition/offset and the exception message.
 * ============================================================================
 */
@Component
public class DlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);

    private final DlqMessageRepository repo;
    private final ObjectMapper objectMapper;

    public DlqConsumer(DlqMessageRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "trade-events-dlq", groupId = "dlq-monitor")
    public void onDlqMessage(ConsumerRecord<String, TradeEvent> record,
                             @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exMsg) {
        TradeEvent event = record.value();
        log.error("DLQ: trade={} eventId={} topic={} partition={} offset={} reason={}",
                event.tradeRef(), event.eventId(), record.topic(), record.partition(), record.offset(), exMsg);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.warn("Failed to serialize DLQ payload for eventId={}", event.eventId(), e);
            payload = "{}";
        }

        repo.save(new DlqMessage(
                event.eventId().toString(),
                event.tradeRef(),
                record.topic().replace("-dlq", ""),
                record.partition(),
                record.offset(),
                payload,
                exMsg,
                Instant.now()));
    }
}
