package com.dbtraining.reconx.service;


import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.exception.ReconJobNotFoundException;
import com.dbtraining.reconx.grpc.ReconGrpc;
import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.ReconJobRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import com.dbtraining.reconx.repository.entity.ReconJob;
import com.dbtraining.reconx.repository.entity.Trade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReconJobPersistenceService {

    private final ReconJobRepository jobRepository;
    private final ReconBreakRepository reconBreakRepository;
    private final TradeRepository tradeRepository;

    public ReconJobPersistenceService(
            ReconJobRepository jobRepository,
            ReconBreakRepository reconBreakRepository,
          TradeRepository tradeRepository
    ) {
        this.jobRepository = jobRepository;
        this.reconBreakRepository = reconBreakRepository;
        this.tradeRepository = tradeRepository;
    }

    public void updateJobStatus(String jobId, Integer tradesProcessed, Integer breaksDetected, String status) {
        ReconJob job = jobRepository
                .findReconJobByJobId(jobId)
                .orElseThrow(() -> new ReconJobNotFoundException(jobId));

        job.setTradesProcessed(tradesProcessed);
        job.setBreaksDetected(breaksDetected);
        job.setStatus(status);

        jobRepository.save(job);
    }

    public void updateTradeStatus(List<String> tradeRefs, String status) {
        List<Trade> matchedTrades = tradeRepository.findByTradeRefIn(tradeRefs);
        matchedTrades.forEach(trade -> trade.setStatus(status));

        tradeRepository.saveAll(matchedTrades);
    }

    public void saveBreaks(List<ReconBreak> breaks) {
        reconBreakRepository.saveAll(breaks);
    }
}
