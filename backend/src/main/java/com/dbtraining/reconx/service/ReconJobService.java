package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconJobMapper;
import com.dbtraining.reconx.dto.ReconJobResponse;
import com.dbtraining.reconx.dto.ReconRunRequest;
import com.dbtraining.reconx.repository.ReconJobRepository;
import com.dbtraining.reconx.repository.entity.ReconJob;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReconJobService {

    private final ReconJobRepository repo;
    private final ReconJobMapper mapper;

    public ReconJobService(ReconJobRepository repo, ReconJobMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public ReconJobResponse create(ReconRunRequest req) {
        String jobId = UUID.randomUUID().toString();

        ReconJob reconJob = new ReconJob.Builder()
                .jobId(jobId)
                .fromDate(req.from())
                .toDate(req.to())
                .build();

        return mapper.toResponse(repo.save(reconJob));
    }
}
