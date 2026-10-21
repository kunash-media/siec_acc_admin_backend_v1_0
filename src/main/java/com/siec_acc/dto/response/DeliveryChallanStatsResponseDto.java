package com.siec_acc.dto.response;

import lombok.Builder;

@Builder
public class DeliveryChallanStatsResponseDto {
    private long totalCount;
    private long draftCount;
    private long dispatchedCount;
    private long deliveredCount;

    public DeliveryChallanStatsResponseDto(){}

    public DeliveryChallanStatsResponseDto(long totalCount, long draftCount, long dispatchedCount, long deliveredCount) {
        this.totalCount = totalCount;
        this.draftCount = draftCount;
        this.dispatchedCount = dispatchedCount;
        this.deliveredCount = deliveredCount;
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

    public long getDispatchedCount() {
        return dispatchedCount;
    }

    public void setDispatchedCount(long dispatchedCount) {
        this.dispatchedCount = dispatchedCount;
    }

    public long getDeliveredCount() {
        return deliveredCount;
    }

    public void setDeliveredCount(long deliveredCount) {
        this.deliveredCount = deliveredCount;
    }
}