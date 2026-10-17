package com.siec_acc.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
public class QuoteItemRequestDto {
    private Long quoteItemProductId;
    private Long quoteItemVariantId;
    private String quoteItemName;
    private String quoteItemDescription;
    private String quoteItemHsnCode;
    private String quoteItemUnit;
    private BigDecimal quoteItemQty;
    private BigDecimal quoteItemRate;
    private BigDecimal quoteItemGstPercent;


    public QuoteItemRequestDto(){}

    public QuoteItemRequestDto(Long quoteItemProductId, Long quoteItemVariantId, String quoteItemName, String quoteItemDescription, String quoteItemHsnCode, String quoteItemUnit, BigDecimal quoteItemQty, BigDecimal quoteItemRate, BigDecimal quoteItemGstPercent) {
        this.quoteItemProductId = quoteItemProductId;
        this.quoteItemVariantId = quoteItemVariantId;
        this.quoteItemName = quoteItemName;
        this.quoteItemDescription = quoteItemDescription;
        this.quoteItemHsnCode = quoteItemHsnCode;
        this.quoteItemUnit = quoteItemUnit;
        this.quoteItemQty = quoteItemQty;
        this.quoteItemRate = quoteItemRate;
        this.quoteItemGstPercent = quoteItemGstPercent;
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

    public BigDecimal getQuoteItemGstPercent() {
        return quoteItemGstPercent;
    }

    public void setQuoteItemGstPercent(BigDecimal quoteItemGstPercent) {
        this.quoteItemGstPercent = quoteItemGstPercent;
    }
}