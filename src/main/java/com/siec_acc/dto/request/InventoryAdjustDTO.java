package com.siec_acc.dto.request;

import java.math.BigDecimal;

public class InventoryAdjustDTO {
    private BigDecimal changeQty;   // signed, non-zero
    private String reason;          // Damaged/Expired/Lost/Found/Physical Count/Other
    private String remarks;
    private String warehouseStrId;


    public InventoryAdjustDTO(BigDecimal changeQty, String reason, String remarks, String warehouseStrId) {
        this.changeQty = changeQty;
        this.reason = reason;
        this.remarks = remarks;
        this.warehouseStrId = warehouseStrId;
    }

    public InventoryAdjustDTO(){}

    public BigDecimal getChangeQty() {
        return changeQty;
    }

    public void setChangeQty(BigDecimal changeQty) {
        this.changeQty = changeQty;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getWarehouseStrId() {
        return warehouseStrId;
    }

    public void setWarehouseStrId(String warehouseStrId) {
        this.warehouseStrId = warehouseStrId;
    }
}