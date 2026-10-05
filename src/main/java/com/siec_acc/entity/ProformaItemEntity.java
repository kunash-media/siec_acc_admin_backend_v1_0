package com.siec_acc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Line item belonging to a {@link ProformaEntity}.
 * itemProductId is kept as a plain column for now — swap for a
 * @ManyToOne to the real Product/Item module once that mapping is confirmed.
 */
@Entity
@Table(name = "proforma_items")
@Data
@Builder
public class ProformaItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proforma_item_prime_id")
    private Long proformaItemPrimeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proforma_prime_id", nullable = false)
    @JsonBackReference
    private ProformaEntity proforma;

    // TODO: replace with @ManyToOne ItemEntity/ProductEntity once that module's mapping is confirmed.
    @Column(name = "proforma_item_product_id")
    private Long proformaItemProductId;

    @Column(name = "proforma_item_name", nullable = false)
    private String proformaItemName;

    @Column(name = "proforma_item_hsn_code")
    private String proformaItemHsnCode;

    @Column(name = "proforma_item_unit")
    private String proformaItemUnit;

    @Column(name = "proforma_item_qty", precision = 15, scale = 2)
    private BigDecimal proformaItemQty;

    @Column(name = "proforma_item_rate", precision = 15, scale = 2)
    private BigDecimal proformaItemRate;

    @Column(name = "proforma_item_gst_percent", precision = 5, scale = 2)
    private BigDecimal proformaItemGstPercent;

    // qty * rate — computed & stored by the service layer at save time.
    @Column(name = "proforma_item_amount", precision = 15, scale = 2)
    private BigDecimal proformaItemAmount;

    public ProformaItemEntity(){}

    public ProformaItemEntity(Long proformaItemPrimeId, ProformaEntity proforma, Long proformaItemProductId, String proformaItemName, String proformaItemHsnCode, String proformaItemUnit, BigDecimal proformaItemQty, BigDecimal proformaItemRate, BigDecimal proformaItemGstPercent, BigDecimal proformaItemAmount) {
        this.proformaItemPrimeId = proformaItemPrimeId;
        this.proforma = proforma;
        this.proformaItemProductId = proformaItemProductId;
        this.proformaItemName = proformaItemName;
        this.proformaItemHsnCode = proformaItemHsnCode;
        this.proformaItemUnit = proformaItemUnit;
        this.proformaItemQty = proformaItemQty;
        this.proformaItemRate = proformaItemRate;
        this.proformaItemGstPercent = proformaItemGstPercent;
        this.proformaItemAmount = proformaItemAmount;
    }

    public Long getProformaItemPrimeId() {
        return proformaItemPrimeId;
    }

    public void setProformaItemPrimeId(Long proformaItemPrimeId) {
        this.proformaItemPrimeId = proformaItemPrimeId;
    }

    public ProformaEntity getProforma() {
        return proforma;
    }

    public void setProforma(ProformaEntity proforma) {
        this.proforma = proforma;
    }

    public Long getProformaItemProductId() {
        return proformaItemProductId;
    }

    public void setProformaItemProductId(Long proformaItemProductId) {
        this.proformaItemProductId = proformaItemProductId;
    }

    public String getProformaItemName() {
        return proformaItemName;
    }

    public void setProformaItemName(String proformaItemName) {
        this.proformaItemName = proformaItemName;
    }

    public String getProformaItemHsnCode() {
        return proformaItemHsnCode;
    }

    public void setProformaItemHsnCode(String proformaItemHsnCode) {
        this.proformaItemHsnCode = proformaItemHsnCode;
    }

    public String getProformaItemUnit() {
        return proformaItemUnit;
    }

    public void setProformaItemUnit(String proformaItemUnit) {
        this.proformaItemUnit = proformaItemUnit;
    }

    public BigDecimal getProformaItemQty() {
        return proformaItemQty;
    }

    public void setProformaItemQty(BigDecimal proformaItemQty) {
        this.proformaItemQty = proformaItemQty;
    }

    public BigDecimal getProformaItemRate() {
        return proformaItemRate;
    }

    public void setProformaItemRate(BigDecimal proformaItemRate) {
        this.proformaItemRate = proformaItemRate;
    }

    public BigDecimal getProformaItemGstPercent() {
        return proformaItemGstPercent;
    }

    public void setProformaItemGstPercent(BigDecimal proformaItemGstPercent) {
        this.proformaItemGstPercent = proformaItemGstPercent;
    }

    public BigDecimal getProformaItemAmount() {
        return proformaItemAmount;
    }

    public void setProformaItemAmount(BigDecimal proformaItemAmount) {
        this.proformaItemAmount = proformaItemAmount;
    }
}
