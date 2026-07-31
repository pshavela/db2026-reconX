package db.reconx.engine.matcher;

import db.reconx.engine.grpc.ReconGrpc.TradeGrpc;
import db.reconx.engine.grpc.ReconGrpc.EquityTradeGrpc;
import db.reconx.engine.grpc.ReconGrpc.ReconResultGrpc;

import java.util.Iterator;
import java.util.Map;

import static db.reconx.engine.grpc.ReconGrpc.StatusGrpc.BREAK;
import static db.reconx.engine.grpc.ReconGrpc.StatusGrpc.MATCHED;


// Simplified version of Project Reconciliation Engine, using ReconciliationRule EXACT and only EquityTradeGrpcs
public class ReconEngine {

    // double might still cause precision leaks
    private static final double epsilon = 1e-6;

    private final Map<String, TradeGrpc> internalTrades;
    private final Map<String, TradeGrpc> externalTrades;

    int matches = 0;
    int breaks = 0;
    int missing = 0;

    public ReconEngine(Map<String, TradeGrpc> internalTrades, Map<String, TradeGrpc> externalTrades) {
        this.internalTrades = internalTrades;
        this.externalTrades = externalTrades;
    }

    public Iterator<ReconResultGrpc> matchTrades() {
        // since tradeRefs are unique and order is irrelevant, parallel streams can be utilized
        return this.internalTrades.values().parallelStream().map(this::match).iterator();
    }

    public ReconResultGrpc match(TradeGrpc in) {
        EquityTradeGrpc eqIn = in.getEquityTrade();
        TradeGrpc ex = this.externalTrades.getOrDefault(eqIn.getTradeRef(), null);

        if (ex == null) {
            ++missing;

            return ReconResultGrpc.newBuilder()
                    .setTradeId(in.getTradeId())
                    .setTradeRef(eqIn.getTradeRef())
                    .setStatus(BREAK)
                    .setDiscrepancyType("MISSING_EXTERNAL")
                    .setDetails("No external trade found for ref " + eqIn.getTradeRef())
                    .build();
        }

        EquityTradeGrpc eqEx = ex.getEquityTrade();

        // only EXACT rule for now
        double priceDifference = Math.abs(eqIn.getPrice() - eqEx.getPrice());
        double quantityDifference = Math.abs(eqIn.getQuantity() - eqEx.getQuantity());

        if((priceDifference <= epsilon) && (quantityDifference <= epsilon)) {
            ++matches;

            return ReconResultGrpc.newBuilder()
                    .setTradeId(in.getTradeId())
                    .setTradeRef(eqIn.getTradeRef())
                    .setStatus(MATCHED)
                    .setDiscrepancyType("")
                    .setDetails("")
                    .build();
        }

        ++breaks;

        return ReconResultGrpc.newBuilder()
                .setTradeId(in.getTradeId())
                .setTradeRef(eqIn.getTradeRef())
                .setStatus(BREAK)
                .setDiscrepancyType("VALUE_MISMATCH")
                .setDetails("internal=%e/%e external=%e/%e".formatted(
                        eqIn.getPrice(), eqIn.getQuantity(),
                        eqEx.getPrice(), eqEx.getQuantity()))
                .build();
    }
}
