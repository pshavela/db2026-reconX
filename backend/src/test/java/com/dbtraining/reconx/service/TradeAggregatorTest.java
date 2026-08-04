package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeAggregatorTest {

    @Test
    void rebuild_returnsEmpty_whenNoEventsExist() {
        AuditLogRepository repo = mock(AuditLogRepository.class);
        when(repo.findByTradeRefOrderByEventTimestampAsc("TRD-000")).thenReturn(List.of());

        TradeAggregator aggregator = new TradeAggregator(repo);

        assertThat(aggregator.rebuild("TRD-000")).isEmpty();
    }

    @Test
    void rebuild_returnsLastAfterSnapshot_whenLatestEventIsAnUpdate() {
        AuditLogRepository repo = mock(AuditLogRepository.class);
        when(repo.findByTradeRefOrderByEventTimestampAsc("TRD-001")).thenReturn(List.of(
                event("TRD-001", "TRADE_CREATED", 1, null, "{\"qty\":100}"),
                event("TRD-001", "TRADE_UPDATED", 2, "{\"qty\":100}", "{\"qty\":200}")
        ));

        TradeAggregator aggregator = new TradeAggregator(repo);

        assertThat(aggregator.rebuild("TRD-001")).contains("{\"qty\":200}");
    }

    @Test
    void rebuild_returnsEmpty_whenLatestEventIsACancellation() {
        AuditLogRepository repo = mock(AuditLogRepository.class);
        when(repo.findByTradeRefOrderByEventTimestampAsc("TRD-002")).thenReturn(List.of(
                event("TRD-002", "TRADE_CREATED", 1, null, "{\"qty\":100}"),
                event("TRD-002", "TRADE_UPDATED", 2, "{\"qty\":100}", "{\"qty\":200}"),
                event("TRD-002", "TRADE_CANCELLED", 3, "{\"qty\":200}", null)
        ));

        TradeAggregator aggregator = new TradeAggregator(repo);

        assertThat(aggregator.rebuild("TRD-002")).isEqualTo(Optional.empty());
    }

    private AuditLogEntry event(String tradeRef, String eventType, int secondsOffset, String before, String after) {
        return new AuditLogEntry(
                java.util.UUID.randomUUID().toString(),
                tradeRef,
                eventType,
                Instant.EPOCH.plusSeconds(secondsOffset),
                "admin@db.com",
                before,
                after);
    }
}
