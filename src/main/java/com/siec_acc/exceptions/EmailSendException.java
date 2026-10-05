package com.siec_acc.exceptions;

/**
 * Thrown when the email dispatch (SMTP / provider API) fails.
 */
public class EmailSendException extends RuntimeException {
    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
