package com.siec_acc.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_payment")
public class CustomerPaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_prime_id")
    private Long paymentPrimeId;

    @Column(name = "payment_id", unique = true, nullable = false, length = 30)
    private String paymentId;

    @Column(name = "receipt_number", unique = true, nullable = false, length = 50)
    private String receiptNumber;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "client_str_id", length = 50)
    private String clientStrId;


    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_mode", nullable = false, length = 30)
    private String paymentMode;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "bank_account", length = 150)
    private String bankAccount;

    @Column(name = "invoice_reference", length = 500)
    private String invoiceReference;

    @Column(name = "invoice_prime_id")
    private Long invoicePrimeId;

    @Column(name = "allocated_amount", precision = 15, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(name = "outstanding_amount", precision = 15, scale = 2)
    private BigDecimal outstandingAmount;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CustomerPaymentEntity() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null || status.isBlank()) status = "RECEIVED";
        if (allocatedAmount == null) allocatedAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getPaymentPrimeId() { return paymentPrimeId; }
    public void setPaymentPrimeId(Long value) { paymentPrimeId = value; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String value) { paymentId = value; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String value) { receiptNumber = value; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate value) { paymentDate = value; }
    public String getClientStrId() { return clientStrId; }
    public void setClientStrId(String value) { clientStrId = value; }
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
    public OrganizationEntity getOrganization() { return organization; }
    public void setOrganization(OrganizationEntity value) { organization = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}