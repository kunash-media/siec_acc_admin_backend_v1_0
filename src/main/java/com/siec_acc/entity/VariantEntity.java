package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "variants")
@Builder
public class VariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_prime_id")
    private Long variantPrimeId;

    @Column(name = "variant_str_id", unique = true, length = 30)
    private String variantStrId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_prime_id", referencedColumnName = "product_prime_id")
    private ProductEntity product;

    @Column(name = "variant_name")
    private String variantName;

    @Column(name = "variant_sku")
    private String variantSku;

    @Column(name = "variant_height")
    private String variantHeight;

    @Column(name = "variant_width")
    private String variantWidth;

    @Column(name = "variant_length")
    private String variantLength;

    @Column(name = "variant_unit")
    private String variantUnit;

    @Column(name = "variant_material_type")
    private String variantMaterialType;

    @Column(name = "variant_size")
    private String variantSize;

    @Column(name = "variant_product_number")
    private String variantProductNumber;

    @Column(name = "variant_category")
    private String variantCategory;

    @Column(name = "variant_sub_category")
    private String variantSubCategory;

    @Column(name = "variant_stock", precision = 15, scale = 2)
    private BigDecimal variantStock;

    @Column(name = "variant_created_at")
    private LocalDateTime variantCreatedAt;

    @Column(name = "variant_updated_at")
    private LocalDateTime variantUpdatedAt;

    @PrePersist
    protected void onCreate() {
        this.variantCreatedAt = LocalDateTime.now();
        this.variantUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.variantUpdatedAt = LocalDateTime.now();
    }

    public VariantEntity(Long variantPrimeId, String variantStrId, ProductEntity product, String variantName, String variantSku, String variantHeight, String variantWidth, String variantLength, String variantUnit, String variantMaterialType, String variantSize, String variantProductNumber, String variantCategory, String variantSubCategory, BigDecimal variantStock, LocalDateTime variantCreatedAt, LocalDateTime variantUpdatedAt) {
        this.variantPrimeId = variantPrimeId;
        this.variantStrId = variantStrId;
        this.product = product;
        this.variantName = variantName;
        this.variantSku = variantSku;
        this.variantHeight = variantHeight;
        this.variantWidth = variantWidth;
        this.variantLength = variantLength;
        this.variantUnit = variantUnit;
        this.variantMaterialType = variantMaterialType;
        this.variantSize = variantSize;
        this.variantProductNumber = variantProductNumber;
        this.variantCategory = variantCategory;
        this.variantSubCategory = variantSubCategory;
        this.variantStock = variantStock;
        this.variantCreatedAt = variantCreatedAt;
        this.variantUpdatedAt = variantUpdatedAt;
    }
    public VariantEntity(){}


    public Long getVariantPrimeId() {
        return variantPrimeId;
    }

    public void setVariantPrimeId(Long variantPrimeId) {
        this.variantPrimeId = variantPrimeId;
    }

    public String getVariantStrId() {
        return variantStrId;
    }

    public void setVariantStrId(String variantStrId) {
        this.variantStrId = variantStrId;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public String getVariantSku() {
        return variantSku;
    }

    public void setVariantSku(String variantSku) {
        this.variantSku = variantSku;
    }

    public String getVariantHeight() {
        return variantHeight;
    }

    public void setVariantHeight(String variantHeight) {
        this.variantHeight = variantHeight;
    }

    public String getVariantWidth() {
        return variantWidth;
    }

    public void setVariantWidth(String variantWidth) {
        this.variantWidth = variantWidth;
    }

    public String getVariantLength() {
        return variantLength;
    }

    public void setVariantLength(String variantLength) {
        this.variantLength = variantLength;
    }

    public String getVariantUnit() {
        return variantUnit;
    }

    public void setVariantUnit(String variantUnit) {
        this.variantUnit = variantUnit;
    }

    public String getVariantMaterialType() {
        return variantMaterialType;
    }

    public void setVariantMaterialType(String variantMaterialType) {
        this.variantMaterialType = variantMaterialType;
    }

    public String getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(String variantSize) {
        this.variantSize = variantSize;
    }

    public String getVariantProductNumber() {
        return variantProductNumber;
    }

    public void setVariantProductNumber(String variantProductNumber) {
        this.variantProductNumber = variantProductNumber;
    }

    public String getVariantCategory() {
        return variantCategory;
    }

    public void setVariantCategory(String variantCategory) {
        this.variantCategory = variantCategory;
    }

    public String getVariantSubCategory() {
        return variantSubCategory;
    }

    public void setVariantSubCategory(String variantSubCategory) {
        this.variantSubCategory = variantSubCategory;
    }

    public BigDecimal getVariantStock() {
        return variantStock;
    }

    public void setVariantStock(BigDecimal variantStock) {
        this.variantStock = variantStock;
    }

    public LocalDateTime getVariantCreatedAt() {
        return variantCreatedAt;
    }

    public void setVariantCreatedAt(LocalDateTime variantCreatedAt) {
        this.variantCreatedAt = variantCreatedAt;
    }

    public LocalDateTime getVariantUpdatedAt() {
        return variantUpdatedAt;
    }

    public void setVariantUpdatedAt(LocalDateTime variantUpdatedAt) {
        this.variantUpdatedAt = variantUpdatedAt;
    }
}