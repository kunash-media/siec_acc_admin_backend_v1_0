package com.siec_acc.enum_status;

/**
 * Status values applicable to a PROFORMA invoice (ProformaEntity) only.
 * Tax invoices use the separate {@link InvoiceStatus} enum.
 */
public enum ProformaStatus {
    OPEN,
    CONVERTED,
    DECLINED,
    EXPIRED
}
