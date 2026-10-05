package com.siec_acc.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class WarehouseResponseDTO {
    private Long warehousePrimeId;
    private String warehouseStrId;
    private String warehouseName;
    private String warehouseAddress;
    private Boolean warehouseIsDefault;
    private String warehouseStatus;
    private LocalDateTime warehouseCreatedAt;
    private LocalDateTime warehouseUpdatedAt;


    public WarehouseResponseDTO(Long warehousePrimeId, String warehouseStrId, String warehouseName, String warehouseAddress, Boolean warehouseIsDefault, String warehouseStatus, LocalDateTime warehouseCreatedAt, LocalDateTime warehouseUpdatedAt) {
        this.warehousePrimeId = warehousePrimeId;
        this.warehouseStrId = warehouseStrId;
        this.warehouseName = warehouseName;
        this.warehouseAddress = warehouseAddress;
        this.warehouseIsDefault = warehouseIsDefault;
        this.warehouseStatus = warehouseStatus;
        this.warehouseCreatedAt = warehouseCreatedAt;
        this.warehouseUpdatedAt = warehouseUpdatedAt;
    }

    public Long getWarehousePrimeId() {
        return warehousePrimeId;
    }

    public void setWarehousePrimeId(Long warehousePrimeId) {
        this.warehousePrimeId = warehousePrimeId;
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

    public String getWarehouseAddress() {
        return warehouseAddress;
    }

    public void setWarehouseAddress(String warehouseAddress) {
        this.warehouseAddress = warehouseAddress;
    }

    public Boolean getWarehouseIsDefault() {
        return warehouseIsDefault;
    }

    public void setWarehouseIsDefault(Boolean warehouseIsDefault) {
        this.warehouseIsDefault = warehouseIsDefault;
    }

    public String getWarehouseStatus() {
        return warehouseStatus;
    }

    public void setWarehouseStatus(String warehouseStatus) {
        this.warehouseStatus = warehouseStatus;
    }

    public LocalDateTime getWarehouseCreatedAt() {
        return warehouseCreatedAt;
    }

    public void setWarehouseCreatedAt(LocalDateTime warehouseCreatedAt) {
        this.warehouseCreatedAt = warehouseCreatedAt;
    }

    public LocalDateTime getWarehouseUpdatedAt() {
        return warehouseUpdatedAt;
    }

    public void setWarehouseUpdatedAt(LocalDateTime warehouseUpdatedAt) {
        this.warehouseUpdatedAt = warehouseUpdatedAt;
    }
}