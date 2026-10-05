package com.siec_acc.exceptions;

/**
 * Thrown when an InvoiceEntity cannot be located by invoicePrimeId / invoiceStrId.
 * Picked up by the existing global @ControllerAdvice exception handler.
 */
public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}
