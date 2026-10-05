package com.siec_acc.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProformaItemResponseDto {
    private Long proformaItemPrimeId;
    private Long proformaItemProductId;
    private String proformaItemName;
    private String proformaItemHsnCode;
    private String proformaItemUnit;
    private BigDecimal proformaItemQty;
    private BigDecimal proformaItemRate;
    private BigDecimal proformaItemGstPercent;
    private BigDecimal proformaItemAmount;

    public ProformaItemResponseDto(){}

    public ProformaItemResponseDto(Long proformaItemPrimeId, Long proformaItemProductId, String proformaItemName, String proformaItemHsnCode, String proformaItemUnit, BigDecimal proformaItemQty, BigDecimal proformaItemRate, BigDecimal proformaItemGstPercent, BigDecimal proformaItemAmount) {
        this.proformaItemPrimeId = proformaItemPrimeId;
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
