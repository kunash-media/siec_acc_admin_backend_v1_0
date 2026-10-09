package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouses")
@Builder
public class WarehouseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_prime_id")
    private Long warehousePrimeId;

    @Column(name = "warehouse_str_id", unique = true, length = 30)
    private String warehouseStrId;

    @Column(name = "warehouse_name", nullable = false, unique = true)
    private String warehouseName;

    @Column(name = "warehouse_address")
    private String warehouseAddress;

    @Column(name = "warehouse_is_default", nullable = false)
    private Boolean warehouseIsDefault;

    @Column(name = "warehouse_status", nullable = false, length = 20)
    private String warehouseStatus; // ACTIVE, INACTIVE

    @Column(name = "warehouse_created_at")
    private LocalDateTime warehouseCreatedAt;

    @Column(name = "warehouse_updated_at")
    private LocalDateTime warehouseUpdatedAt;

    @PrePersist
    protected void onCreate() {
        this.warehouseCreatedAt = LocalDateTime.now();
        this.warehouseUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.warehouseUpdatedAt = LocalDateTime.now();
    }

    public WarehouseEntity(){}

    public WarehouseEntity(Long warehousePrimeId, String warehouseStrId, String warehouseName, String warehouseAddress, Boolean warehouseIsDefault, String warehouseStatus, LocalDateTime warehouseCreatedAt, LocalDateTime warehouseUpdatedAt) {
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