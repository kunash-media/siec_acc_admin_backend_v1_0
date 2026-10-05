package com.siec_acc.exceptions;

/**
 * Thrown when a submitted PDF (invoice/proforma) can't be read/processed.
 * Message is written to be shown directly in the UI's toast notification —
 * keep it short and non-technical.
 */
public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
