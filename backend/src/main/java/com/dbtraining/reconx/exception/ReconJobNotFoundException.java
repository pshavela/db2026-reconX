package com.dbtraining.reconx.exception;

public class ReconJobNotFoundException extends ReconException {
    public ReconJobNotFoundException(String jobId) {
        super("Reconciliation Job not found : " + jobId);
    }
}