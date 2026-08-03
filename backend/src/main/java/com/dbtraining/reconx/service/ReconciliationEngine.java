package com.dbtraining.reconx.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.BondTrade;
import com.dbtraining.reconx.model.DerivativeTrade;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.FXTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeRef;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.observability.ReconMetrics;

// import io.micrometer.core.annotation.Timed;

/**
 * ============================================================================
 * TICKET-ADV033 — ReconciliationEngine using Streams (parallel matching)
 * TICKET-ADV037 — CompletableFuture: parallel recon by counterparty
 * TICKET-ADV047 — Edge cases: empty/single/all-mismatched inputs handled
 * TICKET-ADV084 — @Timed exports reconciliation_duration_seconds histogram
 *
 * WHAT:    Compares internal trades against external (counterparty) trades and
 *          returns a ReconResult per internal trade (MATCHED or BREAK).
 * HOW:     Index externals by tradeRef, then stream internals and look each
 *          up. CompletableFuture variant batches by counterparty for
 *          throughput on large books.
 * WHY:     This is the spine of the product. Everything else (REST API,
 *          Kafka consumers, dashboard) ultimately calls into here.
 * OBSERVE: Histogram appears at /actuator/prometheus under
 *          reconciliation_duration_seconds.
 * ============================================================================
 */
@Service
public class ReconciliationEngine {

    private final ReconMetrics metrics; // 2. Add the metrics dependency

    // 3. Inject it via the constructor
    public ReconciliationEngine(ReconMetrics metrics) {
        this.metrics = metrics;
    }

    // 4. Remove the @Timed annotation
    public List<ReconResult> reconcile(List<TradeType> internal,
                                       List<TradeType> external,
                                       ReconciliationRule rule) {
        
        // 5. Wrap the method body in metrics.reconciliationTimer().record(() -> { ... })
        return metrics.reconciliationTimer().record(() -> {
            if (internal == null || internal.isEmpty() ) {
                return List.of();
            }
            if(external==null){
                external= List.<TradeType>of();
            }
            Map<TradeRef, TradeType> externalTradeRefToTrade = external.stream()
                    .collect(Collectors.toMap(TradeType::tradeRef, Function.identity(), (a, b) -> a));

            return internal.parallelStream()
                    .map(in -> matchOne(in, externalTradeRefToTrade.get(in.tradeRef()), rule))
                    .toList();
        });
    }

/*    @Timed(value = "reconciliation.duration", description = "Wall time of reconcile()",
           percentiles = {0.5, 0.95, 0.99}, histogram = true)
    public List<ReconResult> reconcile(List<TradeType> internal,
                                       List<TradeType> external,
                                       ReconciliationRule rule) {
        if (internal == null || internal.isEmpty() ) {
            return List.of();
        }
        if(external==null){
            external= List.<TradeType>of();
        }
        Map<TradeRef, TradeType> externalTradeRefToTrade = external.stream()
                .collect(Collectors.toMap(TradeType::tradeRef, Function.identity(), (a, b) -> a));

        return internal.parallelStream()
                .map(in -> matchOne(in, externalTradeRefToTrade.get(in.tradeRef()), rule))
                .toList();
    }*/

    /**
     * TICKET-ADV037 — split by counterparty, reconcile each batch concurrently,
     * combine into a single result list. Caller passes one external feed per
     * counterparty (typical real-world shape).
     */
    public CompletableFuture<List<ReconResult>> reconcileByCounterparty(
            Map<Long, List<TradeType>> internalByCp,
            Map<Long, List<TradeType>> externalByCp,
            ReconciliationRule rule) {
        List<CompletableFuture<List<ReconResult>>> futures = internalByCp.entrySet().stream()
                .map(e -> CompletableFuture.supplyAsync(() ->
                        reconcile(e.getValue(), externalByCp.getOrDefault(e.getKey(), List.of()), rule)))
                .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream().flatMap(f -> f.join().stream()).toList());
    }

    private ReconResult matchOne(TradeType internal, TradeType external, ReconciliationRule rule) {
        String ref = internal.tradeRef().value();
        if (external == null) {
            return ReconResult.breakResult(ref, "MISSING_EXTERNAL",
                    "No external trade found for ref " + ref);
        }

        BigDecimal[] internalPriceQty = priceQty(internal);
        BigDecimal[] externalPriceQty = priceQty(external);

        boolean matches = rule.matches(internalPriceQty[0], internalPriceQty[1],
                externalPriceQty[0], externalPriceQty[1]);
        if (matches) {
            return ReconResult.matched(ref);
        }
        return ReconResult.breakResult(ref, "VALUE_MISMATCH",
                "internal=%s/%s external=%s/%s".formatted(
                        internalPriceQty[0], internalPriceQty[1],
                        externalPriceQty[0], externalPriceQty[1]));
    }

    /** TICKET-ADV018 — exhaustive switch over the sealed hierarchy. */
    private BigDecimal[] priceQty(TradeType t) {
        return switch (t) {
            case EquityTrade e     -> new BigDecimal[]{ e.price(), e.quantity() };
            case FXTrade fx        -> new BigDecimal[]{ fx.fxRate(), fx.notionalCcy1() };
            case BondTrade b       -> new BigDecimal[]{ b.couponRate(), b.faceValue() };
            case DerivativeTrade d -> new BigDecimal[]{ d.strike(), d.quantity() };
        };
    }
}
