package com.dbtraining.reconx.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dbtraining.reconx.dto.PagedResponse;
import com.dbtraining.reconx.dto.ReconJobResponse;
import com.dbtraining.reconx.dto.ReconJobSummary;
import com.dbtraining.reconx.dto.ReconRunRequest;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import com.dbtraining.reconx.service.ReconJobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

/**
 * TICKET-ADV068 — POST /api/v1/recon/run — returns 202 + jobId
 * TICKET-ADV069 — GET  /api/v1/recon/jobs/{jobId}/results
 * TICKET-ADV070 — PUT  /api/v1/recon/results/{id}/resolve
 */
@RestController
@RequestMapping("/v1/recon")
@Tag(name = "recon", description = "Reconciliation operations")
@SecurityRequirement(name = "bearerAuth")
public class ReconController {

    private final ReconBreakRepository breaks;
    private final ReconJobService jobService;

    public ReconController(ReconBreakRepository breaks, ReconJobService jobService) {
        this.breaks = breaks;
        this.jobService = jobService;
    }

    @PostMapping("/run")
    @Operation(summary = "Trigger a reconciliation job (async)")
    public ResponseEntity<ReconJobResponse> runRecon(@Valid @RequestBody ReconRunRequest req) {
        ReconJobResponse reconJobResponse = jobService.create(req);

        return ResponseEntity.accepted().body(reconJobResponse);
    }

    @GetMapping("/jobs")
    @Operation(summary = "List recon jobs")
    public PagedResponse<ReconJobSummary> listJobs(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return jobService.listJobs(pageable);
    }

    @GetMapping("/jobs/{jobId}/results")
    @Operation(summary = "Get results for a recon job")
    public PagedResponse<ReconBreak> results(
            @PathVariable String jobId,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        // (TICKET-ADV069): once recon_jobs + recon_breaks tables are wired,
        //   return breaks.findByJobId(jobId). Day-0 returns an empty list so
        //   the React breaks-table renders "no breaks" gracefully.

        // for now we just return all breaks in a paginated way
        return PagedResponse.from(breaks.findAll(pageable), it-> it);
    }

    @PutMapping("/results/{id}/resolve")
    @Operation(summary = "Mark a recon break as RESOLVED with a note")
    public ResponseEntity<ReconBreak> resolve(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        ReconBreak rb = breaks.findById(id)
            .orElseThrow(() -> new TradeNotFoundException("recon_break " + id));

        rb.resolve(body.getOrDefault("note", "manually resolved"));
        return ResponseEntity.ok(breaks.save(rb));
    }

    @PostMapping("/run-upload-csv")
    @Operation(summary = "Trigger a reconciliation job (async) with external trade csv data")
    public ResponseEntity<ReconJobResponse> runRecon(
            @RequestParam("from") @DateTimeFormat(pattern = "YYYY-MM-dd") LocalDate from,
            @RequestParam("to") @DateTimeFormat(pattern = "YYYY-MM-dd") LocalDate to,
            @RequestParam("file") MultipartFile csvFile
    ) {
        // NOTE: csvFile is temporarily stored on disk or memory, will be removed once request processed.
        // Handle properly.

        ReconJobResponse reconJobResponse = jobService.create(from, to, csvFile);

        return ResponseEntity.accepted().body(reconJobResponse);
    }
}
