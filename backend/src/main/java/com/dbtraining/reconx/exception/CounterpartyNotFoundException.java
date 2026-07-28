package com.dbtraining.reconx.exception;

public class CounterpartyNotFoundException extends ReconException {
    public CounterpartyNotFoundException(Long counterpartyId) {
        super("Counterparty not found: " + counterpartyId);
    }
}