package com.siec_acc.exceptions;

/**
 * Thrown when a ProformaEntity cannot be located by proformaPrimeId / proformaStrId.
 */
public class ProformaNotFoundException extends RuntimeException {
    public ProformaNotFoundException(String message) {
        super(message);
    }
}
