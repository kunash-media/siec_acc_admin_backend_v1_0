package com.siec_acc.enum_status;

/**
 * Status values applicable to a TAX INVOICE (InvoiceEntity) only.
 * Proforma invoices use the separate {@link ProformaStatus} enum.
 */
public enum InvoiceStatus {
    UNPAID,
    PARTIAL,
    PAID,
    OVERDUE,
    CANCELLED
}
