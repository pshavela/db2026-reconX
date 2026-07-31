package com.dbtraining.reconx.controller;

import java.net.URI;
import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dbtraining.reconx.dto.PagedResponse;
import com.dbtraining.reconx.dto.StatusUpdate;
import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.service.TradeService;
import com.dbtraining.reconx.service.TradeStreamBroadcaster;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * ============================================================================
 * TICKET-ADV063-ADV067 — TradeController (full CRUD + filterable list)
 * TICKET-ADV080 — API versioning: every endpoint under /v1/
 *
 * Combined with the /api context-path from application.yml, full URLs are
 * /api/v1/trades, /api/v1/trades/{id} etc.
 * ============================================================================
 */
@RestController
@RequestMapping("/v1/trades")
@Tag(name = "trades", description = "Trade CRUD and search")
@SecurityRequirement(name = "bearerAuth")
public class TradeController {

    private final TradeService service;
    private final TradeMapper mapper;
    private final TradeStreamBroadcaster broadcaster;

    public TradeController(TradeService service, TradeMapper mapper, TradeStreamBroadcaster broadcaster) {
        this.service = service;
        this.mapper = mapper;
        this.broadcaster = broadcaster;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Live trade feed (SSE) — temporary in-process bridge, not yet backed by Kafka (ADV128/ADV129)")
    public SseEmitter stream() {
        return broadcaster.subscribe();
    }

    @GetMapping
    @Operation(summary = "List trades — paginated, filterable, sortable")
    public PagedResponse<TradeResponse> list(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long counterpartyId,
            @PageableDefault(size = 20, sort = "tradeDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return PagedResponse.from(service.list(from, to, status, counterpartyId, pageable), it -> it);
    }

    @PostMapping
    @Operation(summary = "Create a trade")
    public ResponseEntity<TradeResponse> create(@Valid @RequestBody TradeRequest req,
                                                @AuthenticationPrincipal Object principal) {
        String actor = String.valueOf(principal);
        Trade saved = service.create(req, actor);
        TradeResponse response = mapper.toResponse(saved);
        broadcaster.broadcast(response);

        return ResponseEntity
                .created(URI.create("/api/v1/trades/" + saved.getId()))
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Full update of a trade")
    public ResponseEntity<TradeResponse> update(@PathVariable Long id, @Valid @RequestBody TradeRequest req,
                                @AuthenticationPrincipal Object principal) {
        String actor = String.valueOf(principal);
        Trade updatedTrade = service.update(id, req, actor);

        // PUT status code is same as Created
        return ResponseEntity
                .created(URI.create("/api/v1/trades/" + updatedTrade.getId()))
                .body(mapper.toResponse(updatedTrade));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update only the status field")
    public TradeResponse updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody StatusUpdate statusUpdate,
                                      @AuthenticationPrincipal Object principal) {
        String actor = String.valueOf(principal);

        return mapper.toResponse(service.updateStatus(id, statusUpdate.status(), actor));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete (sets deleted_at)")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Object principal) {
        String actor = String.valueOf(principal);

        service.softDelete(id, actor);
        return ResponseEntity.noContent().build();
    }
}
