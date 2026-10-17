package com.siec_acc.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Builder
public class QuoteStatsResponseDto {
    private long totalCount;
    private long draftCount;
    private long sentCount;
    private long acceptedCount;
    private BigDecimal totalValueInr;

    public QuoteStatsResponseDto(){}

    public QuoteStatsResponseDto(long totalCount, long draftCount, long sentCount, long acceptedCount, BigDecimal totalValueInr) {
        this.totalCount = totalCount;
        this.draftCount = draftCount;
        this.sentCount = sentCount;
        this.acceptedCount = acceptedCount;
        this.totalValueInr = totalValueInr;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getDraftCount() {
        return draftCount;
    }

    public void setDraftCount(long draftCount) {
        this.draftCount = draftCount;
    }

    public long getSentCount() {
        return sentCount;
    }

    public void setSentCount(long sentCount) {
        this.sentCount = sentCount;
    }

    public long getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(long acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public BigDecimal getTotalValueInr() {
        return totalValueInr;
    }

    public void setTotalValueInr(BigDecimal totalValueInr) {
        this.totalValueInr = totalValueInr;
    }
}