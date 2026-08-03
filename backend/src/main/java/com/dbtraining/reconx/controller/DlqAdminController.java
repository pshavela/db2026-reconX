package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.exception.DlqMessageNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.dbtraining.reconx.repository.entity.DlqMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ============================================================================
 * TICKET-ADV136 — DLQ admin endpoints
 *
 * WHAT:    Lists dead-lettered messages and replays one at a time by eventId.
 * WHY:     One-at-a-time replay (never bulk) prevents re-poisoning the same
 *          bug at scale; ADMIN-only echoes Day 5's RBAC contract.
 * ============================================================================
 */
@RestController
@RequestMapping("/v1/admin/dlq")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "admin-dlq")
@SecurityRequirement(name = "bearerAuth")
public class DlqAdminController {

    private final DlqMessageRepository repo;
    private final TradeEventProducer producer;
    private final ObjectMapper objectMapper;

    public DlqAdminController(DlqMessageRepository repo, TradeEventProducer producer, ObjectMapper objectMapper) {
        this.repo = repo;
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    @Operation(summary = "List all messages currently on the DLQ")
    public List<DlqMessage> list() {
        return repo.findAll();
    }

    @PostMapping("/replay")
    @Operation(summary = "Replay a single DLQ message back to its original topic, by eventId")
    public ResponseEntity<Map<String, Object>> replay(
            @RequestParam UUID eventId,
            @RequestParam(defaultValue = "false") boolean dryRun) {

        DlqMessage msg = repo.findByEventId(eventId.toString())
                .orElseThrow(() -> new DlqMessageNotFoundException(eventId.toString()));

        if (dryRun) {
            return ResponseEntity.ok(Map.of(
                    "dryRun", true,
                    "wouldReplayTo", msg.getOriginalTopic(),
                    "tradeRef", msg.getTradeRef()));
        }

        TradeEvent event;
        try {
            event = objectMapper.readValue(msg.getPayload(), TradeEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize stored DLQ payload for eventId=" + eventId, e);
        }

        producer.publish(event);
        repo.delete(msg);

        return ResponseEntity.ok(Map.of(
                "replayed", true,
                "eventId", eventId,
                "topic", msg.getOriginalTopic()));
    }
}
