package com.siec_acc.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerPaymentRequestDTO {
    private String organizationId;
    private String clientStrId;
    private String receiptNumber;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String paymentMode;
    private String referenceNumber;
    private String bankAccount;
    private String invoiceReference;
    private Long invoicePrimeId;
    private BigDecimal allocatedAmount;
    private String status;
    private String notes;

    public CustomerPaymentRequestDTO() {}
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String value) { organizationId = value; }
    public String getClientStrId() { return clientStrId; }
    public void setClientStrId(String value) { clientStrId = value; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String value) { receiptNumber = value; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate value) { paymentDate = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String value) { paymentMode = value; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String value) { referenceNumber = value; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String value) { bankAccount = value; }
    public String getInvoiceReference() { return invoiceReference; }
    public void setInvoiceReference(String value) { invoiceReference = value; }
    public Long getInvoicePrimeId() { return invoicePrimeId; }
    public void setInvoicePrimeId(Long value) { invoicePrimeId = value; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal value) { allocatedAmount = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }
}