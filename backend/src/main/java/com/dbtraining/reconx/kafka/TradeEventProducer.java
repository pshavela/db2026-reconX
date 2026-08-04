package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * ============================================================================
 * TICKET-ADV129 — TradeEventProducer
 *
 * WHAT:    Publishes TradeEvent messages to the `trade-events` Kafka topic.
 * HOW:     KafkaTemplate<String, TradeEvent>. Key = tradeRef so that all
 *          events for the same trade hash to the same partition and
 *          preserve ordering.
 * WHY:     Out-of-order events for the same trade would make event sourcing
 *          impossible (you'd "apply" CREATE after UPDATE).
 * OBSERVE: Kafdrop -> `trade-events` shows one message per published event,
 *          partitioned by tradeRef.
 * ============================================================================
 *
 *  FIXED (was the GOTCHA below): TradeService.create/update/updateStatus/
 *  softDelete are @Transactional and used to call publish() inline. If the
 *  broker was unreachable, KafkaTemplate.send() blocked the calling thread
 *  for up to producer max.block.ms (60s default) *with the DB transaction
 *  still open*, holding the row/table lock on `trades` the whole time — any
 *  concurrent trade insert then hit H2's much shorter lock timeout and threw
 *  JdbcSQLTimeoutException ("Timeout trying to lock table trades"). publish()
 *  now defers the actual send until after the caller's transaction commits,
 *  so the DB work is never held hostage by Kafka's availability. See
 *  docs/KAFKA.md for the max.block.ms config that bounds the worst case when
 *  a send is attempted with no broker reachable.
 *
 *  DISABLED — see docs/KAFKA.md "Re-enabling Kafka". Left un-gutted (unlike
 *  the consumers) so the fix above is intact and ready the moment someone
 *  uncomments @Component here + the `events` field/param in TradeService.
 * ============================================================================
 */
// @Component
public class TradeEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TradeEventProducer.class);
    private static final String TOPIC = "trade-events";

    private final KafkaTemplate<String, TradeEvent> template;

    public TradeEventProducer(KafkaTemplate<String, TradeEvent> template) {
        this.template = template;
    }

    public void publish(TradeEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doPublish(event);
                }
            });
        } else {
            doPublish(event);
        }
    }

    private void doPublish(TradeEvent event) {
        log.debug("Publishing TradeEvent eventId={} ref={} type={}",
            event.eventId(), event.tradeRef(), event.eventType());
        template.send(TOPIC, event.tradeRef(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish TradeEvent eventId={} ref={}",
                                event.eventId(), event.tradeRef(), ex);
                    } else {
                        log.debug("Published TradeEvent eventId={} ref={} partition={} offset={}",
                                event.eventId(), event.tradeRef(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
