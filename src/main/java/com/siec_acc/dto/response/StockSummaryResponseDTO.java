package com.siec_acc.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public class StockSummaryResponseDTO {
    private String productStrId;
    private String variantStrId;      // null for a product summary
    private BigDecimal totalStock;
    private List<InventoryResponseDTO> warehouses;

    public StockSummaryResponseDTO(String productStrId, String variantStrId, BigDecimal totalStock, List<InventoryResponseDTO> warehouses) {
        this.productStrId = productStrId;
        this.variantStrId = variantStrId;
        this.totalStock = totalStock;
        this.warehouses = warehouses;
    }

    public String getProductStrId() {
        return productStrId;
    }

    public void setProductStrId(String productStrId) {
        this.productStrId = productStrId;
    }

    public String getVariantStrId() {
        return variantStrId;
    }

    public void setVariantStrId(String variantStrId) {
        this.variantStrId = variantStrId;
    }

    public BigDecimal getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(BigDecimal totalStock) {
        this.totalStock = totalStock;
    }

    public List<InventoryResponseDTO> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<InventoryResponseDTO> warehouses) {
        this.warehouses = warehouses;
    }
}