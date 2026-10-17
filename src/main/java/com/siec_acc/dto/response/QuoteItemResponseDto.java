package com.siec_acc.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Builder
public class QuoteItemResponseDto {
    private Long quoteItemPrimeId;
    private Long quoteItemProductId;
    private Long quoteItemVariantId;
    private String quoteItemName;
    private String quoteItemDescription;
    private String quoteItemHsnCode;
    private String quoteItemUnit;
    private BigDecimal quoteItemQty;
    private BigDecimal quoteItemRate;
    private BigDecimal quoteItemAmount;
    private BigDecimal quoteItemTaxableAmount;
    private BigDecimal quoteItemGstPercent;
    private BigDecimal quoteItemCgstAmount;
    private BigDecimal quoteItemSgstAmount;
    private BigDecimal quoteItemIgstAmount;

    public QuoteItemResponseDto(){}

    public QuoteItemResponseDto(Long quoteItemPrimeId, Long quoteItemProductId, Long quoteItemVariantId, String quoteItemName, String quoteItemDescription, String quoteItemHsnCode, String quoteItemUnit, BigDecimal quoteItemQty, BigDecimal quoteItemRate, BigDecimal quoteItemAmount, BigDecimal quoteItemTaxableAmount, BigDecimal quoteItemGstPercent, BigDecimal quoteItemCgstAmount, BigDecimal quoteItemSgstAmount, BigDecimal quoteItemIgstAmount) {
        this.quoteItemPrimeId = quoteItemPrimeId;
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

    public Long getQuoteItemPrimeId() {
        return quoteItemPrimeId;
    }

    public void setQuoteItemPrimeId(Long quoteItemPrimeId) {
        this.quoteItemPrimeId = quoteItemPrimeId;
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