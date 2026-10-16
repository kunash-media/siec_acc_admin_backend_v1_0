package com.siec_acc.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VendorPaymentResponseDto {

    private String vpStrId;
    private String vpNumber;
    private LocalDate date;
    private String vendorId;
    private String vendorName;
    private Double amount;
    private String mode;
    private String reference;
    private String bankAccountName;
    private String status;
    private String notes;
    private List<Allocation> allocations = new ArrayList<>();

    public static class Allocation {
        private String billStrId;
        private String billNumber;
        private Double amount;

        public Allocation() {}

        public Allocation(String billStrId, String billNumber, Double amount) {
            this.billStrId = billStrId;
            this.billNumber = billNumber;
            this.amount = amount;
        }

        public String getBillStrId() { return billStrId; }
        public void setBillStrId(String billStrId) { this.billStrId = billStrId; }

        public String getBillNumber() { return billNumber; }
        public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    public VendorPaymentResponseDto() {}

    public String getVpStrId() { return vpStrId; }
    public void setVpStrId(String vpStrId) { this.vpStrId = vpStrId; }

    public String getVpNumber() { return vpNumber; }
    public void setVpNumber(String vpNumber) { this.vpNumber = vpNumber; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

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
    public void setAllocations(List<Allocation> allocations) { this.allocations = allocations; }

    public static class VendorPaymentStatsResponseDto {

        private long totalPayments;
        private double totalPaid;
        private double thisMonth;
        private double pendingAmount;

        public VendorPaymentStatsResponseDto() {}

        public VendorPaymentStatsResponseDto(long totalPayments, double totalPaid, double thisMonth, double pendingAmount) {
            this.totalPayments = totalPayments;
            this.totalPaid = totalPaid;
            this.thisMonth = thisMonth;
            this.pendingAmount = pendingAmount;
        }

        public long getTotalPayments() { return totalPayments; }
        public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }

        public double getTotalPaid() { return totalPaid; }
        public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

        public double getThisMonth() { return thisMonth; }
        public void setThisMonth(double thisMonth) { this.thisMonth = thisMonth; }

        public double getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(double pendingAmount) { this.pendingAmount = pendingAmount; }
    }

    public static class VendorOpenBillResponseDto {

        private String pbStrId;
        private String pbNumber;
        private Double balance;

        public VendorOpenBillResponseDto() {}

        public VendorOpenBillResponseDto(String pbStrId, String pbNumber, Double balance) {
            this.pbStrId = pbStrId;
            this.pbNumber = pbNumber;
            this.balance = balance;
        }

        public String getPbStrId() { return pbStrId; }
        public void setPbStrId(String pbStrId) { this.pbStrId = pbStrId; }

        public String getPbNumber() { return pbNumber; }
        public void setPbNumber(String pbNumber) { this.pbNumber = pbNumber; }

        public Double getBalance() { return balance; }
        public void setBalance(Double balance) { this.balance = balance; }
    }
}