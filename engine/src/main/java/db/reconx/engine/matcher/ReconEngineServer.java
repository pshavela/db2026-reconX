package db.reconx.engine.matcher;

import db.reconx.engine.grpc.ReconGrpc.TradeGrpc;
import db.reconx.engine.grpc.ReconGrpc.EquityTradeGrpc;
import db.reconx.engine.grpc.ReconGrpc.ReconResultGrpc;
import db.reconx.engine.grpc.ReconServiceGrpc.ReconServiceImplBase;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReconEngineServer {

    private static final int PORT = 5555;
    private static final Logger logger = Logger.getLogger(ReconEngineServer.class.getName());


    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(PORT)
                .addService(new ReconImpl())
                .build()
                .start();

        logger.log(Level.INFO, "Started Grpc Server - Reconciliation Engine");
        server.awaitTermination();
    }

    static class ReconImpl extends  ReconServiceImplBase {

        @Override
        public StreamObserver<TradeGrpc> reconcileTradesGrpc(StreamObserver<ReconResultGrpc> responseObserver) {
            return new StreamObserver<TradeGrpc>() {

                // for now only EquityTradeGrpc supported
                private final Map<String, TradeGrpc> internalTrades = new HashMap<>();
                private final Map<String, TradeGrpc> externalTrades = new HashMap<>();

                @Override
                public void onNext(TradeGrpc trade) {
                    if (internalTrades.isEmpty() && externalTrades.isEmpty()) {
                        logger.info("Opened new channel!");
                    }

                    EquityTradeGrpc t = trade.getEquityTrade();

                    (switch (trade.getOrigin()) {
                        case ORIGIN_UNSPECIFIED, UNRECOGNIZED -> null;
                        case INTERNAL -> internalTrades;
                        case EXTERNAL -> externalTrades;
                    }).put(t.getTradeRef(), trade);
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.throwing(ReconImpl.class.getName(), "Error while processing client messages", throwable);
                }

                @Override
                public void onCompleted() {
                    logger.info("Received data, sleeping for 10 seconds before executing job...");

                    // For demo
                    // try { Thread.sleep(10_000); } catch (InterruptedException e) { throw new RuntimeException(e); }

                    logger.info("Executing job with %d internal and %d external trades".formatted(internalTrades.size(), externalTrades.size()));

                    ReconEngine engine = new ReconEngine(internalTrades, externalTrades);
                    Iterator<ReconResultGrpc> it = engine.matchTrades();

                    logger.info("Streaming results back to client...");

                    while (it.hasNext()) {
                        responseObserver.onNext(it.next());
                    }

                    logger.info("Matches: %d, Breaks: %d, Missing: %d".formatted(
                            engine.matches,
                            engine.breaks,
                            engine.missing)
                    );

                    responseObserver.onCompleted();
                    logger.info("Transmission complete!");
                }
            };
        }
    }
}
