package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_items")
@Builder
public class QuoteItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_item_prime_id")
    private Long quoteItemPrimeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_prime_id", nullable = false)
    private QuoteEntity quote;

    // Plain FKs -> ProductEntity.productPrimeId / variantPrimeId
    @Column(name = "quote_item_product_id")
    private Long quoteItemProductId;

    @Column(name = "quote_item_variant_id")
    private Long quoteItemVariantId;

    @Column(name = "quote_item_name", nullable = false)
    private String quoteItemName;

    // Multi-line description printed in the Description column
    @Column(name = "quote_item_description", columnDefinition = "TEXT")
    private String quoteItemDescription;

    @Column(name = "quote_item_hsn_code")
    private String quoteItemHsnCode;

    @Column(name = "quote_item_unit")
    private String quoteItemUnit;

    @Column(name = "quote_item_qty", precision = 15, scale = 3, nullable = false)
    private BigDecimal quoteItemQty;

    @Column(name = "quote_item_rate", precision = 15, scale = 2, nullable = false)
    private BigDecimal quoteItemRate;

    // qty * rate
    @Column(name = "quote_item_amount", precision = 15, scale = 2)
    private BigDecimal quoteItemAmount;

    // amount after header discount share
    @Column(name = "quote_item_taxable_amount", precision = 15, scale = 2)
    private BigDecimal quoteItemTaxableAmount;

    @Column(name = "quote_item_gst_percent", precision = 5, scale = 2)
    private BigDecimal quoteItemGstPercent;

    @Column(name = "quote_item_cgst_amount", precision = 15, scale = 2)
    private BigDecimal quoteItemCgstAmount;

    @Column(name = "quote_item_sgst_amount", precision = 15, scale = 2)
    private BigDecimal quoteItemSgstAmount;

    @Column(name = "quote_item_igst_amount", precision = 15, scale = 2)
    private BigDecimal quoteItemIgstAmount;


    public QuoteItemEntity(Long quoteItemPrimeId, QuoteEntity quote, Long quoteItemProductId, Long quoteItemVariantId, String quoteItemName, String quoteItemDescription, String quoteItemHsnCode, String quoteItemUnit, BigDecimal quoteItemQty, BigDecimal quoteItemRate, BigDecimal quoteItemAmount, BigDecimal quoteItemTaxableAmount, BigDecimal quoteItemGstPercent, BigDecimal quoteItemCgstAmount, BigDecimal quoteItemSgstAmount, BigDecimal quoteItemIgstAmount) {
        this.quoteItemPrimeId = quoteItemPrimeId;
        this.quote = quote;
        this.quoteItemProductId = quoteItemProductId;
        this.quoteItemVariantId = quoteItemVariantId;
        this.quoteItemName = quoteItemName;
        this.quoteItemDescription = quoteItemDescription;
        this.quoteItemHsnCode = quoteItemHsnCode;
        this.quoteItemUnit = quoteItemUnit;
        this.quoteItemQty = quoteItemQty;
        this.quoteItemRate = quoteItemRate;
        this.quoteItemAmount = quoteItemAmount;
        this.quoteItemTaxableAmount = quoteItemTaxableAmount;
        this.quoteItemGstPercent = quoteItemGstPercent;
        this.quoteItemCgstAmount = quoteItemCgstAmount;
        this.quoteItemSgstAmount = quoteItemSgstAmount;
        this.quoteItemIgstAmount = quoteItemIgstAmount;
    }

    public QuoteItemEntity() {}

    public Long getQuoteItemPrimeId() {
        return quoteItemPrimeId;
    }

    public void setQuoteItemPrimeId(Long quoteItemPrimeId) {
        this.quoteItemPrimeId = quoteItemPrimeId;
    }

    public QuoteEntity getQuote() {
        return quote;
    }

    public void setQuote(QuoteEntity quote) {
        this.quote = quote;
    }

    public Long getQuoteItemProductId() {
        return quoteItemProductId;
    }

    public void setQuoteItemProductId(Long quoteItemProductId) {
        this.quoteItemProductId = quoteItemProductId;
    }

    public Long getQuoteItemVariantId() {
        return quoteItemVariantId;
    }

    public void setQuoteItemVariantId(Long quoteItemVariantId) {
        this.quoteItemVariantId = quoteItemVariantId;
    }

    public String getQuoteItemName() {
        return quoteItemName;
    }

    public void setQuoteItemName(String quoteItemName) {
        this.quoteItemName = quoteItemName;
    }

    public String getQuoteItemDescription() {
        return quoteItemDescription;
    }

    public void setQuoteItemDescription(String quoteItemDescription) {
        this.quoteItemDescription = quoteItemDescription;
    }

    public String getQuoteItemHsnCode() {
        return quoteItemHsnCode;
    }

    public void setQuoteItemHsnCode(String quoteItemHsnCode) {
        this.quoteItemHsnCode = quoteItemHsnCode;
    }

    public String getQuoteItemUnit() {
        return quoteItemUnit;
    }

    public void setQuoteItemUnit(String quoteItemUnit) {
        this.quoteItemUnit = quoteItemUnit;
    }

    public BigDecimal getQuoteItemQty() {
        return quoteItemQty;
    }

    public void setQuoteItemQty(BigDecimal quoteItemQty) {
        this.quoteItemQty = quoteItemQty;
    }

    public BigDecimal getQuoteItemRate() {
        return quoteItemRate;
    }

    public void setQuoteItemRate(BigDecimal quoteItemRate) {
        this.quoteItemRate = quoteItemRate;
    }

    public BigDecimal getQuoteItemAmount() {
        return quoteItemAmount;
    }

    public void setQuoteItemAmount(BigDecimal quoteItemAmount) {
        this.quoteItemAmount = quoteItemAmount;
    }

    public BigDecimal getQuoteItemTaxableAmount() {
        return quoteItemTaxableAmount;
    }

    public void setQuoteItemTaxableAmount(BigDecimal quoteItemTaxableAmount) {
        this.quoteItemTaxableAmount = quoteItemTaxableAmount;
    }

    public BigDecimal getQuoteItemGstPercent() {
        return quoteItemGstPercent;
    }

    public void setQuoteItemGstPercent(BigDecimal quoteItemGstPercent) {
        this.quoteItemGstPercent = quoteItemGstPercent;
    }

    public BigDecimal getQuoteItemCgstAmount() {
        return quoteItemCgstAmount;
    }

    public void setQuoteItemCgstAmount(BigDecimal quoteItemCgstAmount) {
        this.quoteItemCgstAmount = quoteItemCgstAmount;
    }

    public BigDecimal getQuoteItemSgstAmount() {
        return quoteItemSgstAmount;
    }

    public void setQuoteItemSgstAmount(BigDecimal quoteItemSgstAmount) {
        this.quoteItemSgstAmount = quoteItemSgstAmount;
    }

    public BigDecimal getQuoteItemIgstAmount() {
        return quoteItemIgstAmount;
    }

    public void setQuoteItemIgstAmount(BigDecimal quoteItemIgstAmount) {
        this.quoteItemIgstAmount = quoteItemIgstAmount;
    }
}