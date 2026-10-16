package com.siec_acc.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A payment made to a vendor. It can be allocated against one or more purchase bills.
 * Only payments with status "paid" / "cleared" are applied to the bills (as PurchaseBillPaymentEntity rows
 * (each allocation remembers its row in billPaymentId)); "pending" / "bounced" payments never change a bill.
 */
@Entity
@Table(
        name = "vendor_payments",
        indexes = {
                @Index(name = "idx_vp_vendor_str_id", columnList = "vendor_str_id"),
                @Index(name = "idx_vp_date", columnList = "payment_date"),
                @Index(name = "idx_vp_status", columnList = "status")
        }
)
public class VendorPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vp_prime_id")
    private Long vpPrimeId;

    @Column(name = "vp_str_id", unique = true, length = 30)
    private String vpStrId;

    @Column(name = "vp_number", unique = true, length = 30)
    private String vpNumber;

    @Column(name = "vendor_str_id", nullable = false, length = 30)
    private String vendorStrId;

    // Snapshot of the vendor name at the time of payment (bills are linked to vendors by name).
    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "payment_date", nullable = false)
    private LocalDate date;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "mode", nullable = false, length = 40)
    private String mode;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "bank_account_name", length = 100)
    private String bankAccountName;

    // paid | pending | bounced | cleared
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "vendorPayment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Allocation> allocations = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public VendorPaymentEntity() {}

    /** Replaces the whole allocation list (orphanRemoval deletes the old rows). */
    public void replaceAllocations(List<Allocation> newOnes) {
        this.allocations.clear();
        if (newOnes != null) {
            for (Allocation a : newOnes) {
                a.setVendorPayment(this);
                this.allocations.add(a);
            }
        }
    }

    public Long getVpPrimeId() { return vpPrimeId; }
    public void setVpPrimeId(Long vpPrimeId) { this.vpPrimeId = vpPrimeId; }

    public String getVpStrId() { return vpStrId; }
    public void setVpStrId(String vpStrId) { this.vpStrId = vpStrId; }

    public String getVpNumber() { return vpNumber; }
    public void setVpNumber(String vpNumber) { this.vpNumber = vpNumber; }

    public String getVendorStrId() { return vendorStrId; }
    public void setVendorStrId(String vendorStrId) { this.vendorStrId = vendorStrId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getBankAccountName() { return bankAccountName; }
    public void setBankAccountName(String bankAccountName) { this.bankAccountName = bankAccountName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<Allocation> getAllocations() { return allocations; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * How much of the payment is allocated to one purchase bill. The bill is referenced by string id / number
     * (not a hard FK) so deleting a bill never fails because of a pending payment.
     * billPaymentId = id of the PurchaseBillPaymentEntity row this allocation created (only when the payment was applied).
     */
    @Entity(name = "VendorPaymentAllocation")
    @Table(name = "vendor_payment_allocations")
    public static class Allocation {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "allocation_id")
        private Long allocationId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "vp_prime_id", nullable = false)
        private VendorPaymentEntity vendorPayment;

        @Column(name = "pb_str_id", nullable = false, length = 30)
        private String billStrId;

        @Column(name = "pb_number", length = 30)
        private String billNumber;

        @Column(name = "amount", nullable = false)
        private Double amount;

        @Column(name = "bill_payment_id")
        private Long billPaymentId;

        public Allocation() {}

        public Allocation(String billStrId, String billNumber, Double amount) {
            this.billStrId = billStrId;
            this.billNumber = billNumber;
            this.amount = amount;
        }

        public Long getAllocationId() { return allocationId; }
        public VendorPaymentEntity getVendorPayment() { return vendorPayment; }
        public void setVendorPayment(VendorPaymentEntity vendorPayment) { this.vendorPayment = vendorPayment; }
        public String getBillStrId() { return billStrId; }
        public void setBillStrId(String billStrId) { this.billStrId = billStrId; }
        public String getBillNumber() { return billNumber; }
        public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Long getBillPaymentId() { return billPaymentId; }
        public void setBillPaymentId(Long billPaymentId) { this.billPaymentId = billPaymentId; }
    }
}