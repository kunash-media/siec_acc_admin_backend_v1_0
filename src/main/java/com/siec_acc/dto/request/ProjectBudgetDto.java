package com.siec_acc.dto.response;

import java.math.BigDecimal;

public class ProjectBudgetDto {
    private String projectId;
    private BigDecimal totalBudget;
    private BigDecimal purchaseBudget;
    private BigDecimal expenseBudget;
    private BigDecimal hoursBudget;
    private BigDecimal actualCost;
    private BigDecimal variance;
    private BigDecimal usagePct;

    public ProjectBudgetDto() {}

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
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

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public BigDecimal getVariance() {
        return variance;
    }

    public void setVariance(BigDecimal variance) {
        this.variance = variance;
    }

    public BigDecimal getUsagePct() {
        return usagePct;
    }

    public void setUsagePct(BigDecimal usagePct) {
        this.usagePct = usagePct;
    }
}