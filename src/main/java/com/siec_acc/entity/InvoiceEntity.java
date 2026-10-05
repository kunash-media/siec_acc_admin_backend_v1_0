package com.siec_acc.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.siec_acc.enum_status.CurrencyType;
import com.siec_acc.enum_status.InvoiceStatus;
import com.siec_acc.enum_status.PaymentMode;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TAX INVOICE entity. Mirrors the "Invoices" tab on the frontend.
 * Customer/Company relations are kept as plain FK columns for now
 * (no @ManyToOne) since those modules aren't wired up yet — swap in
 * real relations once CustomerEntity / CompanyEntity are confirmed.
 */
@Entity
@Table(name = "inventory", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_product_wh", columnNames = {"product_prime_id", "warehouse_prime_id"}),
        @UniqueConstraint(name = "uk_inventory_variant_wh", columnNames = {"variant_prime_id", "warehouse_prime_id"})
})
@Data
@Builder
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_prime_id")
    private Long invoicePrimeId;

    // Human-readable printed invoice number, e.g. "INV/25-26/001" — unique.
    @Column(name = "invoice_str_id", nullable = false, unique = true)
    private String invoiceStrId;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "invoice_due_date", nullable = false)
    private LocalDate invoiceDueDate;

    // ---- Customer snapshot (plain FK, no relation mapped yet) ----
    @Column(name = "invoice_customer_id", nullable = false)
    private Long invoiceCustomerId;

    @Column(name = "invoice_customer_name")
    private String invoiceCustomerName;

    @Column(name = "invoice_customer_gst")
    private String invoiceCustomerGst;

    @Column(name = "invoice_customer_email")
    private String invoiceCustomerEmail;

    @Column(name = "invoice_customer_state")
    private String invoiceCustomerState;

    @Column(name = "invoice_customer_state_code")
    private String invoiceCustomerStateCode;

    @Column(name = "invoice_customer_address", length = 1000)
    private String invoiceCustomerAddress;

    @Column(name = "invoice_shipping_address", length = 1000)
    private String invoiceShippingAddress;

    // ---- Currency / totals ----
    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_currency", nullable = false)
    private CurrencyType invoiceCurrency;

    @Column(name = "invoice_exchange_rate", precision = 10, scale = 4)
    private BigDecimal invoiceExchangeRate;

    @Column(name = "invoice_sub_total", precision = 15, scale = 2)
    private BigDecimal invoiceSubTotal;

    @Column(name = "invoice_discount_percent", precision = 5, scale = 2)
    private BigDecimal invoiceDiscountPercent;

    @Column(name = "invoice_tax_percent", precision = 5, scale = 2)
    private BigDecimal invoiceTaxPercent;

    @Column(name = "invoice_shipping_charges", precision = 15, scale = 2)
    private BigDecimal invoiceShippingCharges;

    @Column(name = "invoice_other_charges", precision = 15, scale = 2)
    private BigDecimal invoiceOtherCharges;

    @Column(name = "invoice_round_off", precision = 10, scale = 2)
    private BigDecimal invoiceRoundOff;

    @Column(name = "invoice_total_amount", precision = 15, scale = 2)
    private BigDecimal invoiceTotalAmount;

    @Column(name = "invoice_total_amount_inr", precision = 15, scale = 2)
    private BigDecimal invoiceTotalAmountInr;

    @Column(name = "invoice_paid_amount", precision = 15, scale = 2)
    private BigDecimal invoicePaidAmount;

    // ---- Status / meta ----
    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false)
    private InvoiceStatus invoiceStatus;

    @Column(name = "invoice_place_of_supply")
    private String invoicePlaceOfSupply;

    @Column(name = "invoice_terms", length = 1000)
    private String invoiceTerms;

    // Internal notes — not printed on the invoice PDF.
    @Column(name = "invoice_notes", length = 1000)
    private String invoiceNotes;

    @Column(name = "invoice_reference_number")
    private String invoiceReferenceNumber;

    @Column(name = "invoice_po_number")
    private String invoicePoNumber;

    @Column(name = "invoice_eway_bill_number")
    private String invoiceEwayBillNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_payment_mode")
    private PaymentMode invoicePaymentMode;

    // ---- Stored PDF (LONGBLOB) ----
    // invoicePdfUrl is set automatically by the service to
    // "/api/v1/invoices/{invoiceStrId}" as soon as invoiceStrId is generated.
    // GET that URL (see InvoiceController) streams invoicePdfData back with
    // invoicePdfContentType — set invoicePdfData via InvoiceService#storePdf
    // once the PDF for this invoice has been generated.
    @Column(name = "invoice_pdf_url")
    private String invoicePdfUrl;

    @Lob
    @Column(name = "invoice_pdf_data", columnDefinition = "LONGBLOB")
    private byte[] invoicePdfData;

    @Column(name = "invoice_pdf_content_type")
    private String invoicePdfContentType;

    @Column(name = "invoice_attachment_url")
    private String invoiceAttachmentUrl;

    @Column(name = "invoice_signature_url")
    private String invoiceSignatureUrl;

    @Column(name = "invoice_authorized_signatory")
    private String invoiceAuthorizedSignatory;

    @Column(name = "invoice_is_recurring")
    private Boolean invoiceIsRecurring;

    @Column(name = "invoice_recurring_frequency")
    private String invoiceRecurringFrequency;

    // Set when this invoice was generated by converting a proforma —
    // ties back to ProformaEntity.proformaPrimeId.
    @Column(name = "invoice_converted_from_proforma_id")
    private Long invoiceConvertedFromProformaId;

    // ---- Chosen invoice template (Choose Template feature) ----
    @Column(name = "invoice_template_id")
    private String invoiceTemplateId;

    // ---- Stored generated PDF (served back via GET /api/v1/invoices/{invoiceStrId}) ----
    @Lob
    @Column(name = "invoice_pdf_blob", columnDefinition = "LONGBLOB")
    private byte[] invoicePdfBlob;


    @Column(name = "invoice_pdf_file_name")
    private String invoicePdfFileName;

    // ---- Line items ----
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<InvoiceItemEntity> invoiceItems = new ArrayList<>();

    // ---- Audit ----
    @Column(name = "invoice_created_at", updatable = false)
    private LocalDateTime invoiceCreatedAt;

    @Column(name = "invoice_updated_at")
    private LocalDateTime invoiceUpdatedAt;

    @PrePersist
    protected void onCreate() {
        this.invoiceCreatedAt = LocalDateTime.now();
        this.invoiceUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.invoiceUpdatedAt = LocalDateTime.now();
    }

    public InvoiceEntity(Long invoicePrimeId, String invoiceStrId, LocalDate invoiceDate, LocalDate invoiceDueDate, Long invoiceCustomerId, String invoiceCustomerName, String invoiceCustomerGst, String invoiceCustomerEmail, String invoiceCustomerState, String invoiceCustomerStateCode, String invoiceCustomerAddress, String invoiceShippingAddress, CurrencyType invoiceCurrency, BigDecimal invoiceExchangeRate, BigDecimal invoiceSubTotal, BigDecimal invoiceDiscountPercent, BigDecimal invoiceTaxPercent, BigDecimal invoiceShippingCharges, BigDecimal invoiceOtherCharges, BigDecimal invoiceRoundOff, BigDecimal invoiceTotalAmount, BigDecimal invoiceTotalAmountInr, BigDecimal invoicePaidAmount, InvoiceStatus invoiceStatus, String invoicePlaceOfSupply, String invoiceTerms, String invoiceNotes, String invoiceReferenceNumber, String invoicePoNumber, String invoiceEwayBillNumber, PaymentMode invoicePaymentMode, String invoicePdfUrl, byte[] invoicePdfData, String invoicePdfContentType, String invoiceAttachmentUrl, String invoiceSignatureUrl, String invoiceAuthorizedSignatory, Boolean invoiceIsRecurring, String invoiceRecurringFrequency, Long invoiceConvertedFromProformaId, String invoiceTemplateId, byte[] invoicePdfBlob, String invoicePdfFileName, List<InvoiceItemEntity> invoiceItems, LocalDateTime invoiceCreatedAt, LocalDateTime invoiceUpdatedAt) {
        this.invoicePrimeId = invoicePrimeId;
        this.invoiceStrId = invoiceStrId;
        this.invoiceDate = invoiceDate;
        this.invoiceDueDate = invoiceDueDate;
        this.invoiceCustomerId = invoiceCustomerId;
        this.invoiceCustomerName = invoiceCustomerName;
        this.invoiceCustomerGst = invoiceCustomerGst;
        this.invoiceCustomerEmail = invoiceCustomerEmail;
        this.invoiceCustomerState = invoiceCustomerState;
        this.invoiceCustomerStateCode = invoiceCustomerStateCode;
        this.invoiceCustomerAddress = invoiceCustomerAddress;
        this.invoiceShippingAddress = invoiceShippingAddress;
        this.invoiceCurrency = invoiceCurrency;
        this.invoiceExchangeRate = invoiceExchangeRate;
        this.invoiceSubTotal = invoiceSubTotal;
        this.invoiceDiscountPercent = invoiceDiscountPercent;
        this.invoiceTaxPercent = invoiceTaxPercent;
        this.invoiceShippingCharges = invoiceShippingCharges;
        this.invoiceOtherCharges = invoiceOtherCharges;
        this.invoiceRoundOff = invoiceRoundOff;
        this.invoiceTotalAmount = invoiceTotalAmount;
        this.invoiceTotalAmountInr = invoiceTotalAmountInr;
        this.invoicePaidAmount = invoicePaidAmount;
        this.invoiceStatus = invoiceStatus;
        this.invoicePlaceOfSupply = invoicePlaceOfSupply;
        this.invoiceTerms = invoiceTerms;
        this.invoiceNotes = invoiceNotes;
        this.invoiceReferenceNumber = invoiceReferenceNumber;
        this.invoicePoNumber = invoicePoNumber;
        this.invoiceEwayBillNumber = invoiceEwayBillNumber;
        this.invoicePaymentMode = invoicePaymentMode;
        this.invoicePdfUrl = invoicePdfUrl;
        this.invoicePdfData = invoicePdfData;
        this.invoicePdfContentType = invoicePdfContentType;
        this.invoiceAttachmentUrl = invoiceAttachmentUrl;
        this.invoiceSignatureUrl = invoiceSignatureUrl;
        this.invoiceAuthorizedSignatory = invoiceAuthorizedSignatory;
        this.invoiceIsRecurring = invoiceIsRecurring;
        this.invoiceRecurringFrequency = invoiceRecurringFrequency;
        this.invoiceConvertedFromProformaId = invoiceConvertedFromProformaId;
        this.invoiceTemplateId = invoiceTemplateId;
        this.invoicePdfBlob = invoicePdfBlob;
        this.invoicePdfFileName = invoicePdfFileName;
        this.invoiceItems = invoiceItems;
        this.invoiceCreatedAt = invoiceCreatedAt;
        this.invoiceUpdatedAt = invoiceUpdatedAt;
    }

    public InvoiceEntity(){}

    public Long getInvoicePrimeId() {
        return invoicePrimeId;
    }

    public void setInvoicePrimeId(Long invoicePrimeId) {
        this.invoicePrimeId = invoicePrimeId;
    }

    public String getInvoiceStrId() {
        return invoiceStrId;
    }

    public void setInvoiceStrId(String invoiceStrId) {
        this.invoiceStrId = invoiceStrId;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getInvoiceDueDate() {
        return invoiceDueDate;
    }

    public void setInvoiceDueDate(LocalDate invoiceDueDate) {
        this.invoiceDueDate = invoiceDueDate;
    }

    public Long getInvoiceCustomerId() {
        return invoiceCustomerId;
    }

    public void setInvoiceCustomerId(Long invoiceCustomerId) {
        this.invoiceCustomerId = invoiceCustomerId;
    }

    public String getInvoiceCustomerName() {
        return invoiceCustomerName;
    }

    public void setInvoiceCustomerName(String invoiceCustomerName) {
        this.invoiceCustomerName = invoiceCustomerName;
    }

    public String getInvoiceCustomerGst() {
        return invoiceCustomerGst;
    }

    public void setInvoiceCustomerGst(String invoiceCustomerGst) {
        this.invoiceCustomerGst = invoiceCustomerGst;
    }

    public String getInvoiceCustomerEmail() {
        return invoiceCustomerEmail;
    }

    public void setInvoiceCustomerEmail(String invoiceCustomerEmail) {
        this.invoiceCustomerEmail = invoiceCustomerEmail;
    }

    public String getInvoiceCustomerState() {
        return invoiceCustomerState;
    }

    public void setInvoiceCustomerState(String invoiceCustomerState) {
        this.invoiceCustomerState = invoiceCustomerState;
    }

    public String getInvoiceCustomerStateCode() {
        return invoiceCustomerStateCode;
    }

    public void setInvoiceCustomerStateCode(String invoiceCustomerStateCode) {
        this.invoiceCustomerStateCode = invoiceCustomerStateCode;
    }

    public String getInvoiceCustomerAddress() {
        return invoiceCustomerAddress;
    }

    public void setInvoiceCustomerAddress(String invoiceCustomerAddress) {
        this.invoiceCustomerAddress = invoiceCustomerAddress;
    }

    public String getInvoiceShippingAddress() {
        return invoiceShippingAddress;
    }

    public void setInvoiceShippingAddress(String invoiceShippingAddress) {
        this.invoiceShippingAddress = invoiceShippingAddress;
    }

    public CurrencyType getInvoiceCurrency() {
        return invoiceCurrency;
    }

    public void setInvoiceCurrency(CurrencyType invoiceCurrency) {
        this.invoiceCurrency = invoiceCurrency;
    }

    public BigDecimal getInvoiceExchangeRate() {
        return invoiceExchangeRate;
    }

    public void setInvoiceExchangeRate(BigDecimal invoiceExchangeRate) {
        this.invoiceExchangeRate = invoiceExchangeRate;
    }

    public BigDecimal getInvoiceSubTotal() {
        return invoiceSubTotal;
    }

    public void setInvoiceSubTotal(BigDecimal invoiceSubTotal) {
        this.invoiceSubTotal = invoiceSubTotal;
    }

    public BigDecimal getInvoiceDiscountPercent() {
        return invoiceDiscountPercent;
    }

    public void setInvoiceDiscountPercent(BigDecimal invoiceDiscountPercent) {
        this.invoiceDiscountPercent = invoiceDiscountPercent;
    }

    public BigDecimal getInvoiceTaxPercent() {
        return invoiceTaxPercent;
    }

    public void setInvoiceTaxPercent(BigDecimal invoiceTaxPercent) {
        this.invoiceTaxPercent = invoiceTaxPercent;
    }

    public BigDecimal getInvoiceShippingCharges() {
        return invoiceShippingCharges;
    }

    public void setInvoiceShippingCharges(BigDecimal invoiceShippingCharges) {
        this.invoiceShippingCharges = invoiceShippingCharges;
    }

    public BigDecimal getInvoiceOtherCharges() {
        return invoiceOtherCharges;
    }

    public void setInvoiceOtherCharges(BigDecimal invoiceOtherCharges) {
        this.invoiceOtherCharges = invoiceOtherCharges;
    }

    public BigDecimal getInvoiceRoundOff() {
        return invoiceRoundOff;
    }

    public void setInvoiceRoundOff(BigDecimal invoiceRoundOff) {
        this.invoiceRoundOff = invoiceRoundOff;
    }

    public BigDecimal getInvoiceTotalAmount() {
        return invoiceTotalAmount;
    }

    public void setInvoiceTotalAmount(BigDecimal invoiceTotalAmount) {
        this.invoiceTotalAmount = invoiceTotalAmount;
    }

    public BigDecimal getInvoiceTotalAmountInr() {
        return invoiceTotalAmountInr;
    }

    public void setInvoiceTotalAmountInr(BigDecimal invoiceTotalAmountInr) {
        this.invoiceTotalAmountInr = invoiceTotalAmountInr;
    }

    public BigDecimal getInvoicePaidAmount() {
        return invoicePaidAmount;
    }

    public void setInvoicePaidAmount(BigDecimal invoicePaidAmount) {
        this.invoicePaidAmount = invoicePaidAmount;
    }

    public InvoiceStatus getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(InvoiceStatus invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getInvoicePlaceOfSupply() {
        return invoicePlaceOfSupply;
    }

    public void setInvoicePlaceOfSupply(String invoicePlaceOfSupply) {
        this.invoicePlaceOfSupply = invoicePlaceOfSupply;
    }

    public String getInvoiceTerms() {
        return invoiceTerms;
    }

    public void setInvoiceTerms(String invoiceTerms) {
        this.invoiceTerms = invoiceTerms;
    }

    public String getInvoiceNotes() {
        return invoiceNotes;
    }

    public void setInvoiceNotes(String invoiceNotes) {
        this.invoiceNotes = invoiceNotes;
    }

    public String getInvoiceReferenceNumber() {
        return invoiceReferenceNumber;
    }

    public void setInvoiceReferenceNumber(String invoiceReferenceNumber) {
        this.invoiceReferenceNumber = invoiceReferenceNumber;
    }

    public String getInvoicePoNumber() {
        return invoicePoNumber;
    }

    public void setInvoicePoNumber(String invoicePoNumber) {
        this.invoicePoNumber = invoicePoNumber;
    }

    public String getInvoiceEwayBillNumber() {
        return invoiceEwayBillNumber;
    }

    public void setInvoiceEwayBillNumber(String invoiceEwayBillNumber) {
        this.invoiceEwayBillNumber = invoiceEwayBillNumber;
    }

    public PaymentMode getInvoicePaymentMode() {
        return invoicePaymentMode;
    }

    public void setInvoicePaymentMode(PaymentMode invoicePaymentMode) {
        this.invoicePaymentMode = invoicePaymentMode;
    }

    public String getInvoicePdfUrl() {
        return invoicePdfUrl;
    }

    public void setInvoicePdfUrl(String invoicePdfUrl) {
        this.invoicePdfUrl = invoicePdfUrl;
    }

    public byte[] getInvoicePdfData() {
        return invoicePdfData;
    }

    public void setInvoicePdfData(byte[] invoicePdfData) {
        this.invoicePdfData = invoicePdfData;
    }

    public String getInvoicePdfContentType() {
        return invoicePdfContentType;
    }

    public void setInvoicePdfContentType(String invoicePdfContentType) {
        this.invoicePdfContentType = invoicePdfContentType;
    }

    public String getInvoiceAttachmentUrl() {
        return invoiceAttachmentUrl;
    }

    public void setInvoiceAttachmentUrl(String invoiceAttachmentUrl) {
        this.invoiceAttachmentUrl = invoiceAttachmentUrl;
    }

    public String getInvoiceSignatureUrl() {
        return invoiceSignatureUrl;
    }

    public void setInvoiceSignatureUrl(String invoiceSignatureUrl) {
        this.invoiceSignatureUrl = invoiceSignatureUrl;
    }

    public String getInvoiceAuthorizedSignatory() {
        return invoiceAuthorizedSignatory;
    }

    public void setInvoiceAuthorizedSignatory(String invoiceAuthorizedSignatory) {
        this.invoiceAuthorizedSignatory = invoiceAuthorizedSignatory;
    }

    public Boolean getInvoiceIsRecurring() {
        return invoiceIsRecurring;
    }

    public void setInvoiceIsRecurring(Boolean invoiceIsRecurring) {
        this.invoiceIsRecurring = invoiceIsRecurring;
    }

    public String getInvoiceRecurringFrequency() {
        return invoiceRecurringFrequency;
    }

    public void setInvoiceRecurringFrequency(String invoiceRecurringFrequency) {
        this.invoiceRecurringFrequency = invoiceRecurringFrequency;
    }

    public Long getInvoiceConvertedFromProformaId() {
        return invoiceConvertedFromProformaId;
    }

    public void setInvoiceConvertedFromProformaId(Long invoiceConvertedFromProformaId) {
        this.invoiceConvertedFromProformaId = invoiceConvertedFromProformaId;
    }

    public String getInvoiceTemplateId() {
        return invoiceTemplateId;
    }

    public void setInvoiceTemplateId(String invoiceTemplateId) {
        this.invoiceTemplateId = invoiceTemplateId;
    }

    public byte[] getInvoicePdfBlob() {
        return invoicePdfBlob;
    }

    public void setInvoicePdfBlob(byte[] invoicePdfBlob) {
        this.invoicePdfBlob = invoicePdfBlob;
    }

    public String getInvoicePdfFileName() {
        return invoicePdfFileName;
    }

    public void setInvoicePdfFileName(String invoicePdfFileName) {
        this.invoicePdfFileName = invoicePdfFileName;
    }

    public List<InvoiceItemEntity> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(List<InvoiceItemEntity> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    public LocalDateTime getInvoiceCreatedAt() {
        return invoiceCreatedAt;
    }

    public void setInvoiceCreatedAt(LocalDateTime invoiceCreatedAt) {
        this.invoiceCreatedAt = invoiceCreatedAt;
    }

    public LocalDateTime getInvoiceUpdatedAt() {
        return invoiceUpdatedAt;
    }

    public void setInvoiceUpdatedAt(LocalDateTime invoiceUpdatedAt) {
        this.invoiceUpdatedAt = invoiceUpdatedAt;
    }
}
