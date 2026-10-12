package com.siec_acc.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Builder
public class InventoryListItemDTO {
    private String productStrId, productName, productSku, productCategory, productUnit, status;
    private BigDecimal productSellingPrice, productStock;
    private LocalDateTime inventoryUpdatedAt;
    private String warehouseStrId, warehouseName;
    private String variantStrId, variantName;

    public InventoryListItemDTO(String productStrId, String productName, String productSku, String productCategory, String productUnit, String status, BigDecimal productSellingPrice, BigDecimal productStock, LocalDateTime inventoryUpdatedAt, String warehouseStrId, String warehouseName, String variantStrId, String variantName) {
        this.productStrId = productStrId;
        this.productName = productName;
        this.productSku = productSku;
        this.productCategory = productCategory;
        this.productUnit = productUnit;
        this.status = status;
        this.productSellingPrice = productSellingPrice;
        this.productStock = productStock;
        this.inventoryUpdatedAt = inventoryUpdatedAt;
        this.warehouseStrId = warehouseStrId;
        this.warehouseName = warehouseName;
        this.variantStrId = variantStrId;
        this.variantName = variantName;
    }

    public InventoryListItemDTO(){}

    public String getProductStrId() {
        return productStrId;
    }

    public void setProductStrId(String productStrId) {
        this.productStrId = productStrId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getProductSellingPrice() {
        return productSellingPrice;
    }

    public void setProductSellingPrice(BigDecimal productSellingPrice) {
        this.productSellingPrice = productSellingPrice;
    }

    public BigDecimal getProductStock() {
        return productStock;
    }

    public void setProductStock(BigDecimal productStock) {
        this.productStock = productStock;
    }

    public LocalDateTime getInventoryUpdatedAt() {
        return inventoryUpdatedAt;
    }

    public void setInventoryUpdatedAt(LocalDateTime inventoryUpdatedAt) {
        this.inventoryUpdatedAt = inventoryUpdatedAt;
    }

    public String getWarehouseStrId() {
        return warehouseStrId;
    }

    public void setWarehouseStrId(String warehouseStrId) {
        this.warehouseStrId = warehouseStrId;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getVariantStrId() {
        return variantStrId;
    }

    public void setVariantStrId(String variantStrId) {
        this.variantStrId = variantStrId;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }
}