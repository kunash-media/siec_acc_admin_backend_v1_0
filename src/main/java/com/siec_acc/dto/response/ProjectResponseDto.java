package com.siec_acc.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectResponseDto {
    private String id;               // projectStrId
    private Long primeId;
    private String code;             // auto-generated project code
    private String name;
    private String clientStrId;
    private String customer;
    private String pm;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progress;
    private BigDecimal budget;
    private BigDecimal purchaseBudget;
    private BigDecimal expenseBudget;
    private BigDecimal hoursBudget;
    private String description;
    private String holdReason;
    // computed
    private BigDecimal revenue;
    private BigDecimal actualCost;
    private BigDecimal profit;
    private BigDecimal margin;        // % of revenue
    private BigDecimal budgetVariance;
    private BigDecimal budgetUsagePct;
    private LocalDateTime createdAt;

    public ProjectResponseDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPrimeId() {
        return primeId;
    }

    public void setPrimeId(Long primeId) {
        this.primeId = primeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClientStrId() {
        return clientStrId;
    }

    public void setClientStrId(String clientStrId) {
        this.clientStrId = clientStrId;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getPm() {
        return pm;
    }

    public void setPm(String pm) {
        this.pm = pm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getPurchaseBudget() {
        return purchaseBudget;
    }

    public void setPurchaseBudget(BigDecimal purchaseBudget) {
        this.purchaseBudget = purchaseBudget;
    }

    public BigDecimal getExpenseBudget() {
        return expenseBudget;
    }

    public void setExpenseBudget(BigDecimal expenseBudget) {
        this.expenseBudget = expenseBudget;
    }

    public BigDecimal getHoursBudget() {
        return hoursBudget;
    }

    public void setHoursBudget(BigDecimal hoursBudget) {
        this.hoursBudget = hoursBudget;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHoldReason() {
        return holdReason;
    }

    public void setHoldReason(String holdReason) {
        this.holdReason = holdReason;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public BigDecimal getBudgetVariance() {
        return budgetVariance;
    }

    public void setBudgetVariance(BigDecimal budgetVariance) {
        this.budgetVariance = budgetVariance;
    }

    public BigDecimal getBudgetUsagePct() {
        return budgetUsagePct;
    }

    public void setBudgetUsagePct(BigDecimal budgetUsagePct) {
        this.budgetUsagePct = budgetUsagePct;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}