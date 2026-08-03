package com.dbtraining.reconx.exception;

/** TICKET-ADV136 — 404 Not Found: no dlq_messages row for the given eventId. */
public class DlqMessageNotFoundException extends ReconException {
    public DlqMessageNotFoundException(String eventId) {
        super("DLQ message not found for eventId: " + eventId);
    }
}
