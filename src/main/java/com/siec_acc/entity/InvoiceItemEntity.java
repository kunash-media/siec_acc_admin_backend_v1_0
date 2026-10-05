package com.siec_acc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Line item belonging to an {@link InvoiceEntity}.
 * itemProductId is kept as a plain column for now — swap for a
 * @ManyToOne to the real Product/Item module once that mapping is confirmed.
 */
@Entity
@Table(name = "invoice_items")
@Data
@Builder
public class InvoiceItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_item_prime_id")
    private Long invoiceItemPrimeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_prime_id", nullable = false)
    @JsonBackReference
    private InvoiceEntity invoice;

    // TODO: replace with @ManyToOne ItemEntity/ProductEntity once that module's mapping is confirmed.
    @Column(name = "invoice_item_product_id")
    private Long invoiceItemProductId;

    @Column(name = "invoice_item_name", nullable = false)
    private String invoiceItemName;

    @Column(name = "invoice_item_hsn_code")
    private String invoiceItemHsnCode;

    @Column(name = "invoice_item_unit")
    private String invoiceItemUnit;

    @Column(name = "invoice_item_qty", precision = 15, scale = 2)
    private BigDecimal invoiceItemQty;

    @Column(name = "invoice_item_rate", precision = 15, scale = 2)
    private BigDecimal invoiceItemRate;

    @Column(name = "invoice_item_gst_percent", precision = 5, scale = 2)
    private BigDecimal invoiceItemGstPercent;

    // qty * rate — computed & stored by the service layer at save time.
    @Column(name = "invoice_item_amount", precision = 15, scale = 2)
    private BigDecimal invoiceItemAmount;

    public InvoiceItemEntity(Long invoiceItemPrimeId, InvoiceEntity invoice, Long invoiceItemProductId, String invoiceItemName, String invoiceItemHsnCode, String invoiceItemUnit, BigDecimal invoiceItemQty, BigDecimal invoiceItemRate, BigDecimal invoiceItemGstPercent, BigDecimal invoiceItemAmount) {
        this.invoiceItemPrimeId = invoiceItemPrimeId;
        this.invoice = invoice;
        this.invoiceItemProductId = invoiceItemProductId;
        this.invoiceItemName = invoiceItemName;
        this.invoiceItemHsnCode = invoiceItemHsnCode;
        this.invoiceItemUnit = invoiceItemUnit;
        this.invoiceItemQty = invoiceItemQty;
        this.invoiceItemRate = invoiceItemRate;
        this.invoiceItemGstPercent = invoiceItemGstPercent;
        this.invoiceItemAmount = invoiceItemAmount;
    }

    public InvoiceItemEntity(){}

    public Long getInvoiceItemPrimeId() {
        return invoiceItemPrimeId;
    }

    public void setInvoiceItemPrimeId(Long invoiceItemPrimeId) {
        this.invoiceItemPrimeId = invoiceItemPrimeId;
    }

    public InvoiceEntity getInvoice() {
        return invoice;
    }

    public void setInvoice(InvoiceEntity invoice) {
        this.invoice = invoice;
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
