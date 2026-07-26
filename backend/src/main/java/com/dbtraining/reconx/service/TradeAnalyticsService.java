package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.*;
import com.dbtraining.reconx.repository.entity.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * TICKET-ADV034 — Trade analytics with Collectors (groupingBy + summarizing)
 * TICKET-ADV035 — VWAP calculator using Streams + custom collector
 * TICKET-ADV036 — P&L per instrument: stream reduction
 * ============================================================================
 */
@Service
public class TradeAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(TradeAnalyticsService.class);

    /** TICKET-ADV034 — count + sum of notional per counterparty. */
    public Map<Long, NotionalSummary> notionalByCounterparty(List<? extends TradeType> trades) {
        return trades.stream().collect(Collectors.groupingBy(
                this::counterpartyIdOf,
                Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> new NotionalSummary(
                                list.size(),
                                list.stream()
                                        .map(t -> t.notional().amount())
                                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                )));
    }

    /**
     * TICKET-ADV035 — VWAP = SUM(price * qty) / SUM(qty). Equity-only — only
     * EquityTrade has a meaningful price-volume pair.
     */
    public Map<String, BigDecimal> vwapByInstrument(List<EquityTrade> equityTrades) {
        return equityTrades.stream().collect(Collectors.groupingBy(EquityTrade::instrumentSymbol,
                Collectors.collectingAndThen(Collectors.toList(), list -> {
                            BigDecimal denominator = list.stream()
                                    .map(EquityTrade::quantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            BigDecimal numerator = list.stream()
                                    .map(equityTrade -> equityTrade.price().multiply(equityTrade.quantity()))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            return list.isEmpty() ? BigDecimal.ZERO : numerator.divide(denominator, 4, RoundingMode.HALF_UP);
                        })));
    }

    /** TICKET-ADV036 — P&L per instrument symbol (sign by Side). */
    public Map<String, BigDecimal> pnlByInstrument(List<EquityTrade> equityTrades) {
        return equityTrades.stream().collect(Collectors.groupingBy(
                EquityTrade::instrumentSymbol,
                Collectors.mapping(this::pnl, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));
    }

    private BigDecimal pnl(EquityTrade t) {
        BigDecimal abs = t.price().multiply(t.quantity());
        return t.side().equals(Side.SELL) ? abs : abs.negate();
    }

    private long counterpartyIdOf(TradeType t) {
        return switch(t) {
            case BondTrade bondTrade -> bondTrade.counterpartyId();
            case DerivativeTrade derivativeTrade -> derivativeTrade.counterpartyId();
            case EquityTrade equityTrade -> equityTrade.counterpartyId();
            case FXTrade fxTrade -> fxTrade.counterpartyId();
        };
    }

    public record NotionalSummary(long count, BigDecimal total) {}
}
