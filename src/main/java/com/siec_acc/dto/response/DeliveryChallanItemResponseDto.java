package com.siec_acc.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class DeliveryChallanItemResponseDto {
    private Long dcItemPrimeId;
    private Long dcItemProductId;
    private Long dcItemVariantId;
    private String dcItemName;
    private String dcItemDescription;
    private String dcItemHsnCode;
    private String dcItemUnit;
    private BigDecimal dcItemQty;
    private BigDecimal dcItemRate;
    private BigDecimal dcItemAmount;
    private BigDecimal dcItemGstPercent;
    private BigDecimal dcItemCgstAmount;
    private BigDecimal dcItemSgstAmount;
    private BigDecimal dcItemIgstAmount;
    private String dcItemSerial;

    public DeliveryChallanItemResponseDto(Long dcItemPrimeId, Long dcItemProductId, Long dcItemVariantId, String dcItemName, String dcItemDescription, String dcItemHsnCode, String dcItemUnit, BigDecimal dcItemQty, BigDecimal dcItemRate, BigDecimal dcItemAmount, BigDecimal dcItemGstPercent, BigDecimal dcItemCgstAmount, BigDecimal dcItemSgstAmount, BigDecimal dcItemIgstAmount, String dcItemSerial) {
        this.dcItemPrimeId = dcItemPrimeId;
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
    public DeliveryChallanItemResponseDto(){}


    public Long getDcItemPrimeId() {
        return dcItemPrimeId;
    }

    public void setDcItemPrimeId(Long dcItemPrimeId) {
        this.dcItemPrimeId = dcItemPrimeId;
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