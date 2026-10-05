package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Builder
public class InventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_prime_id")
    private Long inventoryPrimeId;

    @Column(name = "inventory_str_id", unique = true, length = 30)
    private String inventoryStrId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_prime_id", referencedColumnName = "product_prime_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_prime_id", referencedColumnName = "variant_prime_id")
    private VariantEntity variant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_prime_id", referencedColumnName = "warehouse_prime_id", nullable = false)
    private WarehouseEntity warehouse;

    @Column(name = "product_stock", precision = 15, scale = 2)
    private BigDecimal productStock;

    @Column(name = "inventory_created_at")
    private LocalDateTime inventoryCreatedAt;

    @Column(name = "inventory_updated_at")
    private LocalDateTime inventoryUpdatedAt;

    @PrePersist
    protected void onCreate() {
        this.inventoryCreatedAt = LocalDateTime.now();
        this.inventoryUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.inventoryUpdatedAt = LocalDateTime.now();
    }

    public InventoryEntity(Long inventoryPrimeId, String inventoryStrId, ProductEntity product, VariantEntity variant, WarehouseEntity warehouse, BigDecimal productStock, LocalDateTime inventoryCreatedAt, LocalDateTime inventoryUpdatedAt) {
        this.inventoryPrimeId = inventoryPrimeId;
        this.inventoryStrId = inventoryStrId;
        this.product = product;
        this.variant = variant;
        this.warehouse = warehouse;
        this.productStock = productStock;
        this.inventoryCreatedAt = inventoryCreatedAt;
        this.inventoryUpdatedAt = inventoryUpdatedAt;
    }

    public InventoryEntity(){}

    public Long getInventoryPrimeId() {
        return inventoryPrimeId;
    }

    public void setInventoryPrimeId(Long inventoryPrimeId) {
        this.inventoryPrimeId = inventoryPrimeId;
    }

    public String getInventoryStrId() {
        return inventoryStrId;
    }

    public void setInventoryStrId(String inventoryStrId) {
        this.inventoryStrId = inventoryStrId;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public BigDecimal getProductStock() {
        return productStock;
    }

    public void setProductStock(BigDecimal productStock) {
        this.productStock = productStock;
    }

    public LocalDateTime getInventoryCreatedAt() {
        return inventoryCreatedAt;
    }

    public void setInventoryCreatedAt(LocalDateTime inventoryCreatedAt) {
        this.inventoryCreatedAt = inventoryCreatedAt;
    }

    public LocalDateTime getInventoryUpdatedAt() {
        return inventoryUpdatedAt;
    }

    public void setInventoryUpdatedAt(LocalDateTime inventoryUpdatedAt) {
        this.inventoryUpdatedAt = inventoryUpdatedAt;
    }

    public VariantEntity getVariant() {
        return variant;
    }

    public void setVariant(VariantEntity variant) {
        this.variant = variant;
    }

    public WarehouseEntity getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseEntity warehouse) {
        this.warehouse = warehouse;
    }
}