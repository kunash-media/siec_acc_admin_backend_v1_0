package com.siec_acc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InvoiceItemRequestDto {

    // TODO: wire to real Product/Item module once mapping is confirmed.
    private Long invoiceItemProductId;

    @NotBlank(message = "Item name is required")
    private String invoiceItemName;

    private String invoiceItemHsnCode;

    private String invoiceItemUnit;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private BigDecimal invoiceItemQty;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be greater than 0")
    private BigDecimal invoiceItemRate;

    private BigDecimal invoiceItemGstPercent;

    public InvoiceItemRequestDto(){}

    public InvoiceItemRequestDto(Long invoiceItemProductId, String invoiceItemName, String invoiceItemHsnCode, String invoiceItemUnit, BigDecimal invoiceItemQty, BigDecimal invoiceItemRate, BigDecimal invoiceItemGstPercent) {
        this.invoiceItemProductId = invoiceItemProductId;
        this.invoiceItemName = invoiceItemName;
        this.invoiceItemHsnCode = invoiceItemHsnCode;
        this.invoiceItemUnit = invoiceItemUnit;
        this.invoiceItemQty = invoiceItemQty;
        this.invoiceItemRate = invoiceItemRate;
        this.invoiceItemGstPercent = invoiceItemGstPercent;
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
}
