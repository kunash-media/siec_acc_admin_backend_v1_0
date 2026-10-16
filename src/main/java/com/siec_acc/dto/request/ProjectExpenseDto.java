package com.siec_acc.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectExpenseDto {
    private LocalDate date;
    private String category;
    private String vendor;
    private BigDecimal amount;
    private String status;

    public ProjectExpenseDto() {}

    public ProjectExpenseDto(LocalDate date, String category, String vendor, BigDecimal amount, String status) {
        this.date = date;
        this.category = category;
        this.vendor = vendor;
        this.amount = amount;
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}