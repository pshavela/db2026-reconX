package com.dbtraining.reconx.exception;

public class InstrumentNotFoundException extends ReconException {
    public InstrumentNotFoundException(Long instrumentId) {
        super("Instrument not found: " + instrumentId);
    }
}
