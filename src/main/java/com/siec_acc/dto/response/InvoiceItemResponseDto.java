package com.siec_acc.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InvoiceItemResponseDto {
    private Long invoiceItemPrimeId;
    private Long invoiceItemProductId;
    private String invoiceItemName;
    private String invoiceItemHsnCode;
    private String invoiceItemUnit;
    private BigDecimal invoiceItemQty;
    private BigDecimal invoiceItemRate;
    private BigDecimal invoiceItemGstPercent;
    private BigDecimal invoiceItemAmount;

    public InvoiceItemResponseDto(Long invoiceItemPrimeId, Long invoiceItemProductId, String invoiceItemName, String invoiceItemHsnCode, String invoiceItemUnit, BigDecimal invoiceItemQty, BigDecimal invoiceItemRate, BigDecimal invoiceItemGstPercent, BigDecimal invoiceItemAmount) {
        this.invoiceItemPrimeId = invoiceItemPrimeId;
        this.invoiceItemProductId = invoiceItemProductId;
        this.invoiceItemName = invoiceItemName;
        this.invoiceItemHsnCode = invoiceItemHsnCode;
        this.invoiceItemUnit = invoiceItemUnit;
        this.invoiceItemQty = invoiceItemQty;
        this.invoiceItemRate = invoiceItemRate;
        this.invoiceItemGstPercent = invoiceItemGstPercent;
        this.invoiceItemAmount = invoiceItemAmount;
    }

    public InvoiceItemResponseDto(){}

    public Long getInvoiceItemPrimeId() {
        return invoiceItemPrimeId;
    }

    public void setInvoiceItemPrimeId(Long invoiceItemPrimeId) {
        this.invoiceItemPrimeId = invoiceItemPrimeId;
    }

    public Long getInvoiceItemProductId() {
        return invoiceItemProductId;
    }

    public void setInvoiceItemProductId(Long invoiceItemProductId) {
        this.invoiceItemProductId = invoiceItemProductId;
    }

    public String getInvoiceItemName() {
        return invoiceItemName;
    }

    public void setInvoiceItemName(String invoiceItemName) {
        this.invoiceItemName = invoiceItemName;
    }

    public String getInvoiceItemHsnCode() {
        return invoiceItemHsnCode;
    }

    public void setInvoiceItemHsnCode(String invoiceItemHsnCode) {
        this.invoiceItemHsnCode = invoiceItemHsnCode;
    }

    public String getInvoiceItemUnit() {
        return invoiceItemUnit;
    }

    public void setInvoiceItemUnit(String invoiceItemUnit) {
        this.invoiceItemUnit = invoiceItemUnit;
    }

    public BigDecimal getInvoiceItemQty() {
        return invoiceItemQty;
    }

    public void setInvoiceItemQty(BigDecimal invoiceItemQty) {
        this.invoiceItemQty = invoiceItemQty;
    }

    public BigDecimal getInvoiceItemRate() {
        return invoiceItemRate;
    }

    public void setInvoiceItemRate(BigDecimal invoiceItemRate) {
        this.invoiceItemRate = invoiceItemRate;
    }

    public BigDecimal getInvoiceItemGstPercent() {
        return invoiceItemGstPercent;
    }

    public void setInvoiceItemGstPercent(BigDecimal invoiceItemGstPercent) {
        this.invoiceItemGstPercent = invoiceItemGstPercent;
    }

    public BigDecimal getInvoiceItemAmount() {
        return invoiceItemAmount;
    }

    public void setInvoiceItemAmount(BigDecimal invoiceItemAmount) {
        this.invoiceItemAmount = invoiceItemAmount;
    }
}
