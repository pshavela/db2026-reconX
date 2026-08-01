package com.dbtraining.reconx.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dbtraining.reconx.dto.PagedResponse;
import com.dbtraining.reconx.dto.ReconJobMapper;
import com.dbtraining.reconx.dto.ReconJobResponse;
import com.dbtraining.reconx.dto.ReconJobSummary;
import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.dto.ReconRunRequest;
import com.dbtraining.reconx.exception.InvalidCSVFileException;
import com.dbtraining.reconx.grpc.ReconGrpc.ReconResultGrpc;
import com.dbtraining.reconx.grpc.ReconGrpc.TradeGrpc;
import com.dbtraining.reconx.grpc.ReconGrpcMapper;
import com.dbtraining.reconx.grpc.ReconServiceGrpc.ReconServiceStub;
import com.dbtraining.reconx.repository.ReconJobRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import com.dbtraining.reconx.repository.entity.ReconJob;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class ReconJobService {

    private final ReconJobRepository reconJobRepository;
    private final ReconJobMapper mapper;
    private final ReconJobGrpcClient jobGrpcClient;
    private final ThreadPoolTaskExecutor executorService;

    public ReconJobService(
        ReconJobRepository repo, 
        ReconJobMapper mapper, 
        ReconJobGrpcClient jobGrpcClient,
        ThreadPoolTaskExecutor executorService
    ) {
        this.reconJobRepository = repo;
        this.mapper = mapper;
        this.jobGrpcClient = jobGrpcClient;
        this.executorService = executorService;
    }

    public ReconJobResponse create(ReconRunRequest req) {
        // Stub for TICKET

        String jobId = UUID.randomUUID().toString();

        ReconJob reconJob = new ReconJob.Builder()
                .jobId(jobId)
                .fromDate(req.from())
                .toDate(req.to())
                .build();

        return mapper.toResponse(reconJobRepository.save(reconJob));
    }

    public ReconJobResponse create(LocalDate from, LocalDate to, MultipartFile csvFile) {
        // Directly stream csvFile to via gRPC without saving it permanently to storage
        if (csvFile.isEmpty()) {
            throw new InvalidCSVFileException("CSV File is empty.");
        }

        String jobId = UUID.randomUUID().toString();

        ReconJobResponse jobResponse = mapper.toResponse(
                reconJobRepository.save(
                        new ReconJob.Builder()
                                .jobId(jobId)
                                .fromDate(from)
                                .toDate(to)
                                .build()
                )
        );

        // run on separate worker thread to avoid blocking caller.
        // But first csvFile must be persisted as it is destroyed once the request is processed
        try {
            File dst = File.createTempFile("csvRecon", null);
            csvFile.transferTo(dst.toPath());
            executorService.execute(() -> jobGrpcClient.triggerReconJob(jobId, dst, from, to));
        } catch (IllegalStateException | IOException e) {
            throw new InvalidCSVFileException("Failed to process CSV.");
        }

        return jobResponse;
    }

    public PagedResponse<ReconJobSummary> listJobs(Pageable pageable) {
        return PagedResponse.from(reconJobRepository.findAll(pageable), mapper::toSummary);
    }

    @Component
    @Transactional
    static class ReconJobGrpcClient {
        /**
         * For now only EquityTrades, can easily be adapted to include other trade types.
         * */

        private static final Logger log = LoggerFactory.getLogger(ReconJobGrpcClient.class);

        @GrpcClient("grpc-engine-server")
        private ReconServiceStub asyncStub;

        private final TradeService tradeService;
        private final ReconJobPersistenceService persistService;

        ReconJobGrpcClient(TradeService tradeService, ReconJobPersistenceService persistService) {
            this.tradeService = tradeService;
            this.persistService = persistService;
        }

        void triggerReconJob(
                String jobId,
                File csvFile,
                LocalDate from,
                LocalDate to
        ) {
            StreamObserver<TradeGrpc> tradeObserver = asyncStub.reconcileTradesGrpc(new StreamObserver<>() {
                /**
                 * Update Trade reconciliation results in batches; Trade-Off: Errors during processing
                 * cannot undo past transactions, however, for updating trade statuses we take this into
                 * account and prioritize performance and space overhead.
                 * Stream and map trade csv file line by line to engine, then internal trades. Await reconciliation
                 * results for internal trades.
                 * */
                private static final int batchSize = 100;
                private final List<ReconResultGrpc> matchedTrades = new ArrayList<>(batchSize);
                private final List<ReconResultGrpc> breakTrades = new ArrayList<>(batchSize);

                private int matches = 0;
                private int breaks = 0;

                @Override
                public void onNext(ReconResultGrpc reconResultGrpc) {
                    switch (reconResultGrpc.getStatus()) {
                        case MATCHED ->  {
                            matchedTrades.add(reconResultGrpc);
                            ++matches;
                        }
                        case BREAK -> {
                            breakTrades.add(reconResultGrpc);
                            ++breaks;
                        }
                    }

                    if (matchedTrades.size() == batchSize) {
                        persistMatchedTrades();
                    }

                    if (breakTrades.size() == batchSize) {
                        persistTradeBreaks();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    io.grpc.Status status = io.grpc.Status.fromThrowable(throwable);
                    log.error("ReconJob {} failed, gRPC Status: {}", jobId, status);
                    persistService.updateJobStatus(jobId, -1, -1, "FAILED");
                }

                @Override
                public void onCompleted() {
                    persistMatchedTrades();
                    persistTradeBreaks();

                    persistService.updateJobStatus(jobId, matches + breaks, breaks, "FINISHED");
                    log.info("Recon job {} complete, {} matches, {} breaks detected.", jobId, matches, breaks);
                }

                private void persistMatchedTrades() {
                    List<String> matchedTradeRefs = matchedTrades.stream().map(ReconResultGrpc::getTradeRef).toList();
                    persistService.updateTradeStatus(matchedTradeRefs, ReconResult.Status.MATCHED.name());

                    matchedTrades.clear();
                }

                private void persistTradeBreaks() {
                    List<String> breakTradeRefs = breakTrades.stream().map(ReconResultGrpc::getTradeRef).toList();
                    persistService.updateTradeStatus(breakTradeRefs, ReconResult.Status.BREAK.name());

                    persistService.saveBreaks(breakTrades.stream().map(b -> {
                        ReconBreak reconBreak = new ReconBreak();
                        reconBreak.setTradeId(b.getTradeId());
                        reconBreak.setDiscrepancyType(b.getDiscrepancyType());

                        return reconBreak;
                    }).toList());

                    breakTrades.clear();
                }
            });

            log.info("Running remote reconciliation engine via gRPC...");

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)))) {
                // first stream internal trades
                tradeService.listPending(from, to, "EQUITY").stream()
                        .map(ReconGrpcMapper::fromTradeEntity)
                        .forEach(tradeObserver::onNext);

                reader.lines()
                        .skip(1) // skip header line
                        .map(ReconGrpcMapper::fromExternalCsvLine)
                        .forEach(tradeObserver::onNext);

                log.info("Transmission of all internal and external trades complete, awaiting reconciliation results...");
            } catch (Exception e) {
                log.error("ReconJob {} failed, Error: {}", jobId, e.getMessage());
                tradeObserver.onError(e);
            }

            tradeObserver.onCompleted();
            // remove temporary csv file
            csvFile.delete();
        }
    }
}
