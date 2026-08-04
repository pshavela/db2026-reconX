package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * TICKET-ADV136 — one row per message that landed on trade-events-dlq,
 * persisted by DlqConsumer. Deleted by DlqAdminController once replayed.
 */
@Entity
@Table(name = "dlq_messages")
public class DlqMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "trade_ref", nullable = false, length = 30)
    private String tradeRef;

    @Column(name = "original_topic", nullable = false, length = 100)
    private String originalTopic;

    @Column(name = "kafka_partition", nullable = false)
    private int partition;

    @Column(name = "kafka_offset", nullable = false)
    private long offset;

    // Serialized TradeEvent JSON — kept as text (not the entity/record itself)
    // so replay only needs TradeEventProducer + Jackson, no Kafka-specific coupling.
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "reason", length = 2000)
    private String reason;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    public DlqMessage() {}

    public DlqMessage(String eventId, String tradeRef, String originalTopic,
                       int partition, long offset, String payload, String reason, Instant firstSeen) {
        this.eventId = eventId;
        this.tradeRef = tradeRef;
        this.originalTopic = originalTopic;
        this.partition = partition;
        this.offset = offset;
        this.payload = payload;
        this.reason = reason;
        this.firstSeen = firstSeen;
    }

    public Long getId()             { return id; }
    public String getEventId()      { return eventId; }
    public String getTradeRef()     { return tradeRef; }
    public String getOriginalTopic(){ return originalTopic; }
    public int getPartition()       { return partition; }
    public long getOffset()         { return offset; }
    public String getPayload()      { return payload; }
    public String getReason()       { return reason; }
    public Instant getFirstSeen()   { return firstSeen; }
}
