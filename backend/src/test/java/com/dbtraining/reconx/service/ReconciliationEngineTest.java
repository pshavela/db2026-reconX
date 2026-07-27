package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 */
class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void testReconcile_exactMatch_returnsMatched() {
        // TODO(TICKET-ADV040): two identical EquityTrades + EXACT rule -> one ReconResult with status MATCHED.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV040 not implemented yet");
    }

    @Test
    void testReconcile_priceTolerance_withinThreshold() {
        // TODO(TICKET-ADV041): prices 100.00 vs 100.50 + PRICE_TOLERANCE_1PCT rule -> status MATCHED.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV041 not implemented yet");
    }

    @Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        EquityTrade internal = equity("EQU-20260603-0003", "100.00", "1000");

    List<ReconResult> out = engine.reconcile(List.of(internal), List.of(), ReconciliationRule.EXACT);

    assertThat(out.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
    assertThat(out.get(0).discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    
    }

    @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        List<ReconResult> results = engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT);
        assertThat(results).isEmpty();
    }

    @Test
    void testReconcile_singleInternalNoExternal_returnsBreakMissingExternal() {
        EquityTrade internal = equity("EQU-20260603-0001", "100.00", "50");
        List<ReconResult> results = engine.reconcile(List.of(internal), List.of(), ReconciliationRule.EXACT);
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().status()).isEqualTo(ReconResult.Status.BREAK);
        assertThat(results.getFirst().discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    }

    @Test
    void testReconcile_allMismatched_returnsAllBreaks() {
        EquityTrade in1 = equity("EQU-20260603-0001", "100.00", "50");
        EquityTrade in2 = equity("EQU-20260603-0002", "200.00", "30");
        EquityTrade in3 = equity("EQU-20260603-0003", "150.00", "20");
        EquityTrade ex1 = equity("EQU-20260603-0001", "999.00", "50");
        EquityTrade ex2 = equity("EQU-20260603-0002", "200.00", "999");
        EquityTrade ex3 = equity("EQU-20260603-0003", "999.00", "999");
        List<ReconResult> results = engine.reconcile(
                List.of(in1, in2, in3), List.of(ex1, ex2, ex3), ReconciliationRule.EXACT);
        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r -> r.status() == ReconResult.Status.BREAK);
        long matched = results.stream().filter(r -> r.status() == ReconResult.Status.MATCHED).count();
        assertThat(matched).isZero();
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
