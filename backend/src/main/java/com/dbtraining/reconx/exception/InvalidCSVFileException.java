package com.dbtraining.reconx.exception;

public class InvalidCSVFileException extends ReconException {
    public InvalidCSVFileException(String message) {
        super("CSV File is invalid: " + message);
    }
}
