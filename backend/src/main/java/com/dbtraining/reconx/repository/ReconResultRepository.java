
package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.dto.ReconResult;

// Only for Mock testing - TICKET-ADV044
public interface ReconResultRepository {
    ReconResult save(ReconResult result);
}