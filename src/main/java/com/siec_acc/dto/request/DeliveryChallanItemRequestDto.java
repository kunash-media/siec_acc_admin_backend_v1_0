package com.siec_acc.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class DeliveryChallanItemRequestDto {
    private Long dcItemProductId;
    private Long dcItemVariantId;
    private String dcItemName;
    private String dcItemDescription;
    private String dcItemHsnCode;
    private String dcItemUnit;
    private BigDecimal dcItemQty;
    private BigDecimal dcItemRate;
    private BigDecimal dcItemGstPercent;
    private String dcItemSerial;

    public DeliveryChallanItemRequestDto(Long dcItemProductId, Long dcItemVariantId, String dcItemName, String dcItemDescription, String dcItemHsnCode, String dcItemUnit, BigDecimal dcItemQty, BigDecimal dcItemRate, BigDecimal dcItemGstPercent, String dcItemSerial) {
        this.dcItemProductId = dcItemProductId;
        this.dcItemVariantId = dcItemVariantId;
        this.dcItemName = dcItemName;
        this.dcItemDescription = dcItemDescription;
        this.dcItemHsnCode = dcItemHsnCode;
        this.dcItemUnit = dcItemUnit;
        this.dcItemQty = dcItemQty;
        this.dcItemRate = dcItemRate;
        this.dcItemGstPercent = dcItemGstPercent;
        this.dcItemSerial = dcItemSerial;
    }

    public DeliveryChallanItemRequestDto(){}


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

    public BigDecimal getDcItemGstPercent() {
        return dcItemGstPercent;
    }

    public void setDcItemGstPercent(BigDecimal dcItemGstPercent) {
        this.dcItemGstPercent = dcItemGstPercent;
    }

    public String getDcItemSerial() {
        return dcItemSerial;
    }

    public void setDcItemSerial(String dcItemSerial) {
        this.dcItemSerial = dcItemSerial;
    }
}