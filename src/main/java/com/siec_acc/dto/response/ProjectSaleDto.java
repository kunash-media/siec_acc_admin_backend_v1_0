package com.siec_acc.dto.response;

import com.siec_acc.enum_status.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectSaleDto {
    private String number;
    private LocalDate date;
    private String customer;
    private BigDecimal amount;      // total in INR
    private BigDecimal paidAmount;
    private String status;          // lower-case invoice status
    private String pdfFileName;
    private String pdfUrl;

    public ProjectSaleDto() {}

    // Used by the JPQL constructor expression in ProjectInvoiceQueryRepository.
    public ProjectSaleDto(String number, LocalDate date, String customer, BigDecimal amount, BigDecimal paidAmount,
                          InvoiceStatus status, String pdfFileName, String pdfUrl) {
        this.number = number;
        this.date = date;
        this.customer = customer;
        this.amount = amount == null ? BigDecimal.ZERO : amount;
        this.paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        this.status = status == null ? "unpaid" : status.name().toLowerCase();
        this.pdfFileName = pdfFileName;
        this.pdfUrl = pdfUrl;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}