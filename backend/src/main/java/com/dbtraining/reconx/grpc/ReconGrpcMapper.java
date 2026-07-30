package com.dbtraining.reconx.grpc;

import com.dbtraining.reconx.grpc.ReconGrpc.TradeGrpc;
import com.dbtraining.reconx.repository.entity.Trade;

public class ReconGrpcMapper {
    // for now only equityTrades

    public static TradeGrpc fromTradeEntity(Trade t) {
        assert(t.getAssetClass().equals("EQUITY"));

        return TradeGrpc.newBuilder()
                .setTradeId(t.getId())
                .setOrigin(ReconGrpc.OriginGrpc.INTERNAL)
                .setEquityTrade(
                        ReconGrpc.EquityTradeGrpc.newBuilder()
                                .setTradeRef(t.getTradeRef())
                                .setInstrumentSymbol(t.getInstrument().getSymbol())
                                .setCounterPartyId(t.getCounterparty().getId())
                                .setQuantity(t.getQuantity().doubleValue())
                                .setCurrency(t.getInstrument().getCurrency())
                                .setPrice(t.getPrice().doubleValue())
                                .setTradeDate(t.getTradeDate().toString())
                                .setSide(ReconGrpc.SideGrpc.valueOf(t.getSide()))
                                .build()
                )
                .build();
    }

    public static TradeGrpc fromExternalCsvLine(String l) {
        // Format: trade_ref,instrument_symbol,counterparty_id,quantity,price,currency,side,trade_date
        String[] fields = l.split(",");

        return TradeGrpc.newBuilder()
                .setOrigin(ReconGrpc.OriginGrpc.EXTERNAL)
                .setTradeId(-1) // external trades are not present in internal database
                .setEquityTrade(
                        ReconGrpc.EquityTradeGrpc.newBuilder()
                                .setTradeRef(fields[0])
                                .setInstrumentSymbol(fields[1])
                                .setCounterPartyId(Long.parseLong(fields[2]))
                                .setQuantity(Double.parseDouble(fields[3]))
                                .setPrice(Double.parseDouble(fields[4]))
                                .setCurrency(fields[5])
                                .setSide(ReconGrpc.SideGrpc.valueOf(fields[6]))
                                .setTradeDate(fields[7])
                                .build()
                )
                .build();
    }
}
