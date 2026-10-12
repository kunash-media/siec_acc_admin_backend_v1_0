package com.siec_acc.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public class InventoryHistoryResponseDTO {
    private String historyStrId, historyChangeType, historyRemarks;
    private BigDecimal historyPreviousStock, historyNewStock, historyChangeQty;
    private LocalDateTime historyCreatedAt;
    private String warehouseStrId;

    public InventoryHistoryResponseDTO(){}

    public InventoryHistoryResponseDTO(String historyStrId, String historyChangeType, String historyRemarks, BigDecimal historyPreviousStock, BigDecimal historyNewStock, BigDecimal historyChangeQty, LocalDateTime historyCreatedAt, String warehouseStrId) {
        this.historyStrId = historyStrId;
        this.historyChangeType = historyChangeType;
        this.historyRemarks = historyRemarks;
        this.historyPreviousStock = historyPreviousStock;
        this.historyNewStock = historyNewStock;
        this.historyChangeQty = historyChangeQty;
        this.historyCreatedAt = historyCreatedAt;
        this.warehouseStrId = warehouseStrId;
    }

    public String getHistoryStrId() {
        return historyStrId;
    }

    public void setHistoryStrId(String historyStrId) {
        this.historyStrId = historyStrId;
    }

    public String getHistoryChangeType() {
        return historyChangeType;
    }

    public void setHistoryChangeType(String historyChangeType) {
        this.historyChangeType = historyChangeType;
    }

    public String getHistoryRemarks() {
        return historyRemarks;
    }

    public void setHistoryRemarks(String historyRemarks) {
        this.historyRemarks = historyRemarks;
    }

    public BigDecimal getHistoryPreviousStock() {
        return historyPreviousStock;
    }

    public void setHistoryPreviousStock(BigDecimal historyPreviousStock) {
        this.historyPreviousStock = historyPreviousStock;
    }

    public BigDecimal getHistoryNewStock() {
        return historyNewStock;
    }

    public void setHistoryNewStock(BigDecimal historyNewStock) {
        this.historyNewStock = historyNewStock;
    }

    public BigDecimal getHistoryChangeQty() {
        return historyChangeQty;
    }

    public void setHistoryChangeQty(BigDecimal historyChangeQty) {
        this.historyChangeQty = historyChangeQty;
    }

    public LocalDateTime getHistoryCreatedAt() {
        return historyCreatedAt;
    }

    public void setHistoryCreatedAt(LocalDateTime historyCreatedAt) {
        this.historyCreatedAt = historyCreatedAt;
    }

    public String getWarehouseStrId() {
        return warehouseStrId;
    }

    public void setWarehouseStrId(String warehouseStrId) {
        this.warehouseStrId = warehouseStrId;
    }
}