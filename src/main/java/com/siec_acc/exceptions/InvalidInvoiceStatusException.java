package com.siec_acc.exceptions;

/**
 * Thrown on an illegal invoice/proforma status transition
 * (e.g. trying to send an already-PAID invoice, or convert an
 * already-CONVERTED proforma).
 */
public class InvalidInvoiceStatusException extends RuntimeException {
    public InvalidInvoiceStatusException(String message) {
        super(message);
    }
}
