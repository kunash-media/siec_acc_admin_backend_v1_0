package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "delivery_challan_items")
@Builder
public class DeliveryChallanItemEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dc_item_prime_id")
    private Long dcItemPrimeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dc_prime_id", nullable = false)
    private DeliveryChallanEntity dc;

    @Column(name = "dc_item_product_id")
    private Long dcItemProductId;   // ProductEntity.productPrimeId
    @Column(name = "dc_item_variant_id")
    private Long dcItemVariantId;   // VariantEntity.variantPrimeId
    @Column(name = "dc_item_name", nullable = false)
    private String dcItemName;
    @Column(name = "dc_item_description", columnDefinition = "TEXT")
    private String dcItemDescription;
    @Column(name = "dc_item_hsn_code")
    private String dcItemHsnCode;
    @Column(name = "dc_item_unit")
    private String dcItemUnit;
    @Column(name = "dc_item_qty", precision = 15, scale = 3, nullable = false)
    private BigDecimal dcItemQty;
    @Column(name = "dc_item_rate", precision = 15, scale = 2, nullable = false)
    private BigDecimal dcItemRate;
    @Column(name = "dc_item_amount", precision = 15, scale = 2)
    private BigDecimal dcItemAmount;
    @Column(name = "dc_item_gst_percent", precision = 5, scale = 2)
    private BigDecimal dcItemGstPercent;
    @Column(name = "dc_item_cgst_amount", precision = 15, scale = 2)
    private BigDecimal dcItemCgstAmount;
    @Column(name = "dc_item_sgst_amount", precision = 15, scale = 2)
    private BigDecimal dcItemSgstAmount;
    @Column(name = "dc_item_igst_amount", precision = 15, scale = 2)
    private BigDecimal dcItemIgstAmount;
    @Column(name = "dc_item_serial", columnDefinition = "TEXT")
    private String dcItemSerial;  // internal only


    public DeliveryChallanItemEntity(Long dcItemPrimeId, DeliveryChallanEntity dc, Long dcItemProductId, Long dcItemVariantId, String dcItemName, String dcItemDescription, String dcItemHsnCode, String dcItemUnit, BigDecimal dcItemQty, BigDecimal dcItemRate, BigDecimal dcItemAmount, BigDecimal dcItemGstPercent, BigDecimal dcItemCgstAmount, BigDecimal dcItemSgstAmount, BigDecimal dcItemIgstAmount, String dcItemSerial) {
        this.dcItemPrimeId = dcItemPrimeId;
        this.dc = dc;
        this.dcItemProductId = dcItemProductId;
        this.dcItemVariantId = dcItemVariantId;
        this.dcItemName = dcItemName;
        this.dcItemDescription = dcItemDescription;
        this.dcItemHsnCode = dcItemHsnCode;
        this.dcItemUnit = dcItemUnit;
        this.dcItemQty = dcItemQty;
        this.dcItemRate = dcItemRate;
        this.dcItemAmount = dcItemAmount;
        this.dcItemGstPercent = dcItemGstPercent;
        this.dcItemCgstAmount = dcItemCgstAmount;
        this.dcItemSgstAmount = dcItemSgstAmount;
        this.dcItemIgstAmount = dcItemIgstAmount;
        this.dcItemSerial = dcItemSerial;
    }

    public DeliveryChallanItemEntity(){}

    public Long getDcItemPrimeId() {
        return dcItemPrimeId;
    }

    public void setDcItemPrimeId(Long dcItemPrimeId) {
        this.dcItemPrimeId = dcItemPrimeId;
    }

    public DeliveryChallanEntity getDc() {
        return dc;
    }

    public void setDc(DeliveryChallanEntity dc) {
        this.dc = dc;
    }

    public Long getDcItemProductId() {
        return dcItemProductId;
    }

    public void setDcItemProductId(Long dcItemProductId) {
        this.dcItemProductId = dcItemProductId;
    }

    public Long getDcItemVariantId() {
        return dcItemVariantId;
    }

    public void setDcItemVariantId(Long dcItemVariantId) {
        this.dcItemVariantId = dcItemVariantId;
    }

    public String getDcItemName() {
        return dcItemName;
    }

    public void setDcItemName(String dcItemName) {
        this.dcItemName = dcItemName;
    }

    public String getDcItemDescription() {
        return dcItemDescription;
    }

    public void setDcItemDescription(String dcItemDescription) {
        this.dcItemDescription = dcItemDescription;
    }

    public String getDcItemHsnCode() {
        return dcItemHsnCode;
    }

    public void setDcItemHsnCode(String dcItemHsnCode) {
        this.dcItemHsnCode = dcItemHsnCode;
    }

    public String getDcItemUnit() {
        return dcItemUnit;
    }

    public void setDcItemUnit(String dcItemUnit) {
        this.dcItemUnit = dcItemUnit;
    }

    public BigDecimal getDcItemQty() {
        return dcItemQty;
    }

    public void setDcItemQty(BigDecimal dcItemQty) {
        this.dcItemQty = dcItemQty;
    }

    public BigDecimal getDcItemRate() {
        return dcItemRate;
    }

    public void setDcItemRate(BigDecimal dcItemRate) {
        this.dcItemRate = dcItemRate;
    }

    public BigDecimal getDcItemAmount() {
        return dcItemAmount;
    }

    public void setDcItemAmount(BigDecimal dcItemAmount) {
        this.dcItemAmount = dcItemAmount;
    }

    public BigDecimal getDcItemGstPercent() {
        return dcItemGstPercent;
    }

    public void setDcItemGstPercent(BigDecimal dcItemGstPercent) {
        this.dcItemGstPercent = dcItemGstPercent;
    }

    public BigDecimal getDcItemCgstAmount() {
        return dcItemCgstAmount;
    }

    public void setDcItemCgstAmount(BigDecimal dcItemCgstAmount) {
        this.dcItemCgstAmount = dcItemCgstAmount;
    }

    public BigDecimal getDcItemSgstAmount() {
        return dcItemSgstAmount;
    }

    public void setDcItemSgstAmount(BigDecimal dcItemSgstAmount) {
        this.dcItemSgstAmount = dcItemSgstAmount;
    }

    public BigDecimal getDcItemIgstAmount() {
        return dcItemIgstAmount;
    }

    public void setDcItemIgstAmount(BigDecimal dcItemIgstAmount) {
        this.dcItemIgstAmount = dcItemIgstAmount;
    }

    public String getDcItemSerial() {
        return dcItemSerial;
    }

    public void setDcItemSerial(String dcItemSerial) {
        this.dcItemSerial = dcItemSerial;
    }
}