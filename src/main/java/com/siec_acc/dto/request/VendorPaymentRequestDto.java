package com.siec_acc.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Body of create-vendor-payment / update-vendor-payment. */
public class VendorPaymentRequestDto {

    @NotBlank(message = "Vendor is required")
    private String vendorId;

    @NotNull(message = "Payment date is required")
    private LocalDate date;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Payment mode is required")
    @Size(max = 40, message = "Mode must be at most 40 characters")
    private String mode;

    @Size(max = 100, message = "Reference must be at most 100 characters")
    private String reference;

    @Size(max = 100, message = "Bank account must be at most 100 characters")
    private String bankAccountName;

    // paid | pending | bounced | cleared (defaults to paid)
    private String status;

    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    private String notes;

    @Valid
    private List<Allocation> allocations = new ArrayList<>();

    public static class Allocation {
        @NotBlank(message = "Bill is required for every allocation")
        private String billStrId;

        @NotNull(message = "Allocation amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Allocation amount must be greater than 0")
        private Double amount;

        public Allocation() {}

        public String getBillStrId() { return billStrId; }
        public void setBillStrId(String billStrId) { this.billStrId = billStrId; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    public VendorPaymentRequestDto() {}

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

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
    public void setAllocations(List<Allocation> allocations) {
        this.allocations = allocations == null ? new ArrayList<>() : allocations;
    }
}