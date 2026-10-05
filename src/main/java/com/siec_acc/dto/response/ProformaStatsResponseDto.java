package com.siec_acc.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Backs the stat cards on the Proforma Invoices tab (Total / Open / Declined / Total Value).
 */
@Data
@Builder
public class ProformaStatsResponseDto {
    private long totalCount;
    private long openCount;
    private long declinedCount;
    private BigDecimal totalValueInr;

    public ProformaStatsResponseDto(){}

    public ProformaStatsResponseDto(long totalCount, long openCount, long declinedCount, BigDecimal totalValueInr) {
        this.totalCount = totalCount;
        this.openCount = openCount;
        this.declinedCount = declinedCount;
        this.totalValueInr = totalValueInr;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getOpenCount() {
        return openCount;
    }

    public void setOpenCount(long openCount) {
        this.openCount = openCount;
    }

    public long getDeclinedCount() {
        return declinedCount;
    }

    public void setDeclinedCount(long declinedCount) {
        this.declinedCount = declinedCount;
    }

    public BigDecimal getTotalValueInr() {
        return totalValueInr;
    }

    public void setTotalValueInr(BigDecimal totalValueInr) {
        this.totalValueInr = totalValueInr;
    }
}
