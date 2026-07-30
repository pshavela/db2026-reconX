package com.dbtraining.reconx.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TICKET-ADV080 — Retired v0 trades endpoint, kept only to demonstrate the
 * deprecation contract. Real trade data lives under /v1/trades.
 */
@RestController
@RequestMapping("/v0/trades")
public class LegacyTradesController {

    @Deprecated(since = "v1.4.0", forRemoval = true)
    @GetMapping
    public ResponseEntity<Void> listDeprecated() {
        String sunset = ZonedDateTime.now().plusMonths(3)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME);

        return ResponseEntity.status(HttpStatus.GONE)
                .header("Deprecation", "true")
                .header("Sunset", sunset)
                .header("Link", "</api/v1/trades>; rel=\"successor-version\"")
                .build();
    }
}
