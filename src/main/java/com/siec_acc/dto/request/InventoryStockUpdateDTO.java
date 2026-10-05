package com.siec_acc.dto.request;

import lombok.*;
import java.math.BigDecimal;

@Builder
public class InventoryStockUpdateDTO {
    private BigDecimal changeQty; // must be > 0; direction decided by endpoint (add/reduce)
    private String remarks;
    private String source; // optional: null = normal add, "VENDOR_PURCHASE" = vendor flow

    private String warehouseStrId; // optional; omitted = default warehouse

    public InventoryStockUpdateDTO(BigDecimal changeQty, String remarks, String source, String warehouseStrId) {
        this.changeQty = changeQty;
        this.remarks = remarks;
        this.source = source;
        this.warehouseStrId = warehouseStrId;
    }

    public InventoryStockUpdateDTO(){}

    public BigDecimal getChangeQty() {
        return changeQty;
    }

    public void setChangeQty(BigDecimal changeQty) {
        this.changeQty = changeQty;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getWarehouseStrId() {
        return warehouseStrId;
    }

    public void setWarehouseStrId(String warehouseStrId) {
        this.warehouseStrId = warehouseStrId;
    }
}