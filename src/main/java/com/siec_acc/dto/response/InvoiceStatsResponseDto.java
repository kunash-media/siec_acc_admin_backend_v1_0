package com.siec_acc.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Backs the stat cards on the Invoices tab (Total / Due / Overdue / Total Value).
 */
@Data
@Builder
public class InvoiceStatsResponseDto {
    private long totalCount;
    private long dueCount;
    private long overdueCount;
    private BigDecimal totalValueInr;

    public InvoiceStatsResponseDto(long totalCount, long dueCount, long overdueCount, BigDecimal totalValueInr) {
        this.totalCount = totalCount;
        this.dueCount = dueCount;
        this.overdueCount = overdueCount;
        this.totalValueInr = totalValueInr;
    }

    public InvoiceStatsResponseDto(){}

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getDueCount() {
        return dueCount;
    }

    public void setDueCount(long dueCount) {
        this.dueCount = dueCount;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public BigDecimal getTotalValueInr() {
        return totalValueInr;
    }

    public void setTotalValueInr(BigDecimal totalValueInr) {
        this.totalValueInr = totalValueInr;
    }
}
