package com.siec_acc.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceSummaryResponseDto {
    private Long invoicePrimeId;
    private String invoiceStrId;
    private LocalDate invoiceDate;
    private LocalDate invoiceDueDate;
    private BigDecimal invoiceTotalAmountInr;
    private BigDecimal invoicePaidAmount;

    public InvoiceSummaryResponseDto(){}

    public InvoiceSummaryResponseDto(Long invoicePrimeId, String invoiceStrId, LocalDate invoiceDate, LocalDate invoiceDueDate, BigDecimal invoiceTotalAmountInr, BigDecimal invoicePaidAmount) {
        this.invoicePrimeId = invoicePrimeId;
        this.invoiceStrId = invoiceStrId;
        this.invoiceDate = invoiceDate;
        this.invoiceDueDate = invoiceDueDate;
        this.invoiceTotalAmountInr = invoiceTotalAmountInr;
        this.invoicePaidAmount = invoicePaidAmount;
    }

    public Long getInvoicePrimeId() {
        return invoicePrimeId;
    }

    public void setInvoicePrimeId(Long invoicePrimeId) {
        this.invoicePrimeId = invoicePrimeId;
    }

    public String getInvoiceStrId() {
        return invoiceStrId;
    }

    public void setInvoiceStrId(String invoiceStrId) {
        this.invoiceStrId = invoiceStrId;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getInvoiceDueDate() {
        return invoiceDueDate;
    }

    public void setInvoiceDueDate(LocalDate invoiceDueDate) {
        this.invoiceDueDate = invoiceDueDate;
    }

    public BigDecimal getInvoiceTotalAmountInr() {
        return invoiceTotalAmountInr;
    }

    public void setInvoiceTotalAmountInr(BigDecimal invoiceTotalAmountInr) {
        this.invoiceTotalAmountInr = invoiceTotalAmountInr;
    }

    public BigDecimal getInvoicePaidAmount() {
        return invoicePaidAmount;
    }

    public void setInvoicePaidAmount(BigDecimal invoicePaidAmount) {
        this.invoicePaidAmount = invoicePaidAmount;
    }
}
