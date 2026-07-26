package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import com.dbtraining.reconx.model.EquityTrade.Builder;

class EquityTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {
        EquityTrade trade = sampleEquity("AAA-20260101-0001");

        assertThat(trade.tradeRef().value()).isEqualTo("AAA-20260101-0001");
        assertThat(trade.notional().amount()).isEqualTo(new BigDecimal("10000"));
    }

    @Test
    void builder_missingPrice_throws() {
        Builder b = EquityTrade.builder()
                .tradeRef(TradeRef.of("AAA-20260101-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                //.price(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L);

        assertThatThrownBy(b::build)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("price");
    }

    @Test
    void equality_byTradeRef() {
        // TODO(TICKET-ADV028): two EquityTrades with the same tradeRef are equal and share hashCode;
        //                     a third with a different tradeRef is not equal.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV028 not implemented yet");
    }

    private EquityTrade sampleEquity(String ref) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build();
    }
}
