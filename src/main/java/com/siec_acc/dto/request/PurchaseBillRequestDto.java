package com.siec_acc.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class PurchaseBillRequestDto {

    @NotBlank(message = "Vendor name is required")
    @Size(max = 255, message = "Vendor name must be at most 255 characters")
    private String vendorName;

    // Optional — set this to link the bill to an existing Purchase Order.
    @Size(max = 30, message = "PO number must be at most 30 characters")
    private String poNumber;

    private LocalDate billDate;

    private LocalDate dueDate;

    @NotNull(message = "Bill amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Bill amount must be greater than 0")
    private Double amount;

    public PurchaseBillRequestDto() {}

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }

    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}


