package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * TICKET-ADV137 — Event sourcing rebuild
 *
 * WHAT:    Reconstructs a trade's current state purely from its audit_log
 *          event history, without touching the `trades` table.
 * HOW:     Reads every AuditLogEntry for a tradeRef, ordered by
 *          event_timestamp (NOT eventId — UUIDs aren't monotonic), and folds
 *          them: TRADE_CREATED/TRADE_UPDATED replace the running state with
 *          the event's after-snapshot; TRADE_CANCELLED clears it to null.
 * WHY:     Proves the event log persisted by ADV132 is a real source of
 *          truth, not just a side-channel log — the canonical event-sourcing
 *          pattern. ADV138's admin endpoint exposes the same history this
 *          folds over.
 * OBSERVE: created -> updated -> cancelled folds to Optional.empty(); the
 *          same sequence without the cancel returns the last update's
 *          after-snapshot.
 * ============================================================================
 */
@Service
public class TradeAggregator {

    private final AuditLogRepository auditRepo;

    public TradeAggregator(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public Optional<String> rebuild(String tradeRef) {
        List<AuditLogEntry> events = auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef);
        if (events.isEmpty()) {
            return Optional.empty();
        }

        String state = null;
        for (AuditLogEntry e : events) {
            switch (TradeEvent.EventType.valueOf(e.getEventType())) {
                case TRADE_CREATED, TRADE_UPDATED -> state = e.getAfterState();
                case TRADE_CANCELLED -> state = null;
            }
        }
        return Optional.ofNullable(state);
    }
}
