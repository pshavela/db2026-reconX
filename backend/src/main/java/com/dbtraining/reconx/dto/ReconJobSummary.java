package com.dbtraining.reconx.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ReconJobSummary(
        String jobId,
        LocalDate fromDate,
        LocalDate toDate,
        String status,
        Instant startedAt,
        Instant finishedAt,
        Integer tradesProcessed,
        Integer breaksDetected
) {}
