package com.siec_acc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProformaItemRequestDto {

    // TODO: wire to real Product/Item module once mapping is confirmed.
    private Long proformaItemProductId;

    @NotBlank(message = "Item name is required")
    private String proformaItemName;

    private String proformaItemHsnCode;

    private String proformaItemUnit;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private BigDecimal proformaItemQty;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be greater than 0")
    private BigDecimal proformaItemRate;

    private BigDecimal proformaItemGstPercent;

    public ProformaItemRequestDto(Long proformaItemProductId, String proformaItemName, String proformaItemHsnCode, String proformaItemUnit, BigDecimal proformaItemQty, BigDecimal proformaItemRate, BigDecimal proformaItemGstPercent) {
        this.proformaItemProductId = proformaItemProductId;
        this.proformaItemName = proformaItemName;
        this.proformaItemHsnCode = proformaItemHsnCode;
        this.proformaItemUnit = proformaItemUnit;
        this.proformaItemQty = proformaItemQty;
        this.proformaItemRate = proformaItemRate;
        this.proformaItemGstPercent = proformaItemGstPercent;
    }

    public ProformaItemRequestDto(){}

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
}
