package com.siec_acc.exceptions;

/**
 * Thrown when an invoice/proforma is saved with zero line items.
 */
public class InvoiceItemRequiredException extends RuntimeException {
    public InvoiceItemRequiredException(String message) {
        super(message);
    }
}
