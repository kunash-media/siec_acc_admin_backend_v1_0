package com.siec_acc.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectPurchaseDto {
    private String number;
    private LocalDate date;
    private String vendor;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String status;   // received | partial | paid | overdue (derived by PurchaseBillService)
    private String refId;    // pbStrId - used by the UI to unlink the bill

    public ProjectPurchaseDto() {}

    public ProjectPurchaseDto(String number, LocalDate date, String vendor, BigDecimal amount, BigDecimal paidAmount, String status) {
        this.number = number;
        this.date = date;
        this.vendor = vendor;
        this.amount = amount;
        this.paidAmount = paidAmount;
        this.status = status;
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

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
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

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }
}