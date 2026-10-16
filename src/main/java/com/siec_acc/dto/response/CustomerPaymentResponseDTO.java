package com.siec_acc.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerPaymentResponseDTO {
    private String organizationId;
    private OrganizationSummaryDTO organization;
    private String paymentId;
    private String receiptNumber;
    private LocalDate paymentDate;
    private Long clientPrimeId;
    private String clientStrId;
    private String customerName;
    private String customerCompanyName;
    private BigDecimal amount;
    private String paymentMode;
    private String referenceNumber;
    private String bankAccount;
    private String invoiceReference;
    private Long invoicePrimeId;
    private BigDecimal allocatedAmount;
    private BigDecimal outstandingAmount;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerPaymentResponseDTO() {}
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String value) { organizationId = value; }
    public OrganizationSummaryDTO getOrganization() { return organization; }
    public void setOrganization(OrganizationSummaryDTO value) { organization = value; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String value) { paymentId = value; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String value) { receiptNumber = value; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate value) { paymentDate = value; }
    public Long getClientPrimeId() { return clientPrimeId; }
    public void setClientPrimeId(Long value) { clientPrimeId = value; }
    public String getClientStrId() { return clientStrId; }
    public void setClientStrId(String value) { clientStrId = value; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String value) { customerName = value; }
    public String getCustomerCompanyName() { return customerCompanyName; }
    public void setCustomerCompanyName(String value) { customerCompanyName = value; }
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
    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public void setOutstandingAmount(BigDecimal value) { outstandingAmount = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getNotes() { return notes; }
    public void setNotes(String value) { notes = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}