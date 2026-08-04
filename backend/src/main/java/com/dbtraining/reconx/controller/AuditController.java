package com.dbtraining.reconx.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;
import com.dbtraining.reconx.service.TradeAggregator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * TICKET-ADV071 — GET /api/v1/audit/trades/{tradeRef}
 * TICKET-ADV138 — GET /api/v1/audit/trades/{tradeRef}/events
 *
 * @PreAuthorize is redundant with SecurityConfig's /v1/audit/** matcher
 * (also RECON_ANALYST/ADMIN only), kept explicit here per ADV138's own
 * Done-when checklist and for defense in depth.
 */
@RestController
@RequestMapping("/v1/audit")
@PreAuthorize("hasAnyRole('ADMIN','RECON_ANALYST')")
@Tag(name = "audit")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditLogRepository auditRepo;
    private final TradeAggregator aggregator;

    public AuditController(AuditLogRepository auditRepo, TradeAggregator aggregator) {
        this.auditRepo = auditRepo;
        this.aggregator = aggregator;
    }

    @GetMapping("/trades/{tradeRef}")
    @Operation(summary = "Get audit history for a trade (by tradeRef)")
    public List<AuditLogEntry> history(@PathVariable String tradeRef) {
        return auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef);
    }

    @GetMapping("/trades/{tradeRef}/events")
    @Operation(summary = "Full ordered event history for a trade, oldest first")
    public List<AuditLogEntry> events(@PathVariable String tradeRef) {
        return auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef);
    }

    // TICKET-ADV137 bonus wiring — the aggregator had no caller anywhere in the
    // codebase until this endpoint; pairs the raw event log above with the
    // rebuilt current state folded from it. 404 covers both "no such trade"
    // and "trade was cancelled" (rebuild() returns empty for both).
    @GetMapping("/trades/{tradeRef}/rebuild")
    @Operation(summary = "Rebuild a trade's current state purely from its audit_log event history")
    public ResponseEntity<String> rebuild(@PathVariable String tradeRef) {
        return aggregator.rebuild(tradeRef)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
