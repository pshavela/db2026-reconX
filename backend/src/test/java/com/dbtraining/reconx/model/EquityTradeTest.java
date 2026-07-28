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
                .tradeRef(TradeRef.of("AAA-20260101-0002"))
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
        EquityTrade a = EquityTrade.builder()
                .tradeRef(TradeRef.of("AAA-20260101-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .price(new BigDecimal("105"))
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();

        EquityTrade b = EquityTrade.builder()
                .tradeRef(TradeRef.of("AAA-20260101-0001"))
                .instrumentSymbol("APPLE.US")
                .quantity(new BigDecimal("50"))
                .currency("EUR").side(Side.BUY)
                .price(new BigDecimal("500"))
                .tradeDate(LocalDate.of(2026, 5, 3))
                .counterpartyId(3L)
                .build();


        EquityTrade c = EquityTrade.builder()
                .tradeRef(TradeRef.of("CCC-20260101-0001"))
                .instrumentSymbol("APPLE.US")
                .quantity(new BigDecimal("50"))
                .currency("EUR").side(Side.BUY)
                .price(new BigDecimal("500"))
                .tradeDate(LocalDate.of(2026, 5, 3))
                .counterpartyId(3L)
                .build();

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        assertThat(b).isNotEqualTo(c);
        assertThat(b.hashCode()).isNotEqualTo(c.hashCode());
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
