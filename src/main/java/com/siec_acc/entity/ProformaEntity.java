package com.siec_acc.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.siec_acc.enum_status.CurrencyType;
import com.siec_acc.enum_status.ProformaStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PROFORMA INVOICE entity. Mirrors the "Proforma Invoices" tab on the
 * frontend. Deliberately kept as its own table (rather than a shared
 * table with InvoiceEntity) per the invoicePrimeId/invoiceStrId vs.
 * proformaPrimeId/proformaStrId split.
 * Customer/Company relations are kept as plain FK columns for now.
 */
@Entity
@Table(name = "proformas")
@Data
@Builder
public class ProformaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proforma_prime_id")
    private Long proformaPrimeId;

    // Human-readable printed proforma number, e.g. "PI/25-26/001" — unique.
    @Column(name = "proforma_str_id", nullable = false, unique = true)
    private String proformaStrId;

    @Column(name = "proforma_date", nullable = false)
    private LocalDate proformaDate;

    // "Valid Until" on the frontend.
    @Column(name = "proforma_valid_until", nullable = false)
    private LocalDate proformaValidUntil;

    // ---- Customer snapshot (plain FK, no relation mapped yet) ----
    @Column(name = "proforma_customer_id", nullable = false)
    private Long proformaCustomerId;

    @Column(name = "proforma_customer_name")
    private String proformaCustomerName;

    @Column(name = "proforma_customer_gst")
    private String proformaCustomerGst;

    @Column(name = "proforma_customer_email")
    private String proformaCustomerEmail;

    @Column(name = "proforma_customer_state")
    private String proformaCustomerState;

    @Column(name = "proforma_customer_state_code")
    private String proformaCustomerStateCode;

    @Column(name = "proforma_customer_address", length = 1000)
    private String proformaCustomerAddress;

    @Column(name = "proforma_shipping_address", length = 1000)
    private String proformaShippingAddress;

    // ---- Currency / totals ----
    @Enumerated(EnumType.STRING)
    @Column(name = "proforma_currency", nullable = false)
    private CurrencyType proformaCurrency;

    @Column(name = "proforma_exchange_rate", precision = 10, scale = 4)
    private BigDecimal proformaExchangeRate;

    @Column(name = "proforma_sub_total", precision = 15, scale = 2)
    private BigDecimal proformaSubTotal;

    @Column(name = "proforma_discount_percent", precision = 5, scale = 2)
    private BigDecimal proformaDiscountPercent;

    @Column(name = "proforma_tax_percent", precision = 5, scale = 2)
    private BigDecimal proformaTaxPercent;

    @Column(name = "proforma_shipping_charges", precision = 15, scale = 2)
    private BigDecimal proformaShippingCharges;

    @Column(name = "proforma_other_charges", precision = 15, scale = 2)
    private BigDecimal proformaOtherCharges;

    @Column(name = "proforma_round_off", precision = 10, scale = 2)
    private BigDecimal proformaRoundOff;

    @Column(name = "proforma_total_amount", precision = 15, scale = 2)
    private BigDecimal proformaTotalAmount;

    @Column(name = "proforma_total_amount_inr", precision = 15, scale = 2)
    private BigDecimal proformaTotalAmountInr;

    // ---- Status / meta ----
    @Enumerated(EnumType.STRING)
    @Column(name = "proforma_status", nullable = false)
    private ProformaStatus proformaStatus;

    @Column(name = "proforma_place_of_supply")
    private String proformaPlaceOfSupply;

    @Column(name = "proforma_terms", length = 1000)
    private String proformaTerms;

    // Internal notes — not printed on the proforma PDF.
    @Column(name = "proforma_notes", length = 1000)
    private String proformaNotes;

    @Column(name = "proforma_reference_number")
    private String proformaReferenceNumber;

    @Column(name = "proforma_po_number")
    private String proformaPoNumber;

    // ---- Stored PDF (LONGBLOB) ----
    // proformaPdfUrl is set automatically by the service to
    // "/api/v1/proformas/{proformaStrId}" as soon as proformaStrId is generated.
    // GET that URL (see ProformaController) streams proformaPdfData back with
    // proformaPdfContentType — set proformaPdfData via ProformaService#storePdf
    // once the PDF for this proforma has been generated.
    @Column(name = "proforma_pdf_url")
    private String proformaPdfUrl;

    @Lob
    @Column(name = "proforma_pdf_data", columnDefinition = "LONGBLOB")
    private byte[] proformaPdfData;

    @Column(name = "proforma_pdf_content_type")
    private String proformaPdfContentType;

    @Column(name = "proforma_attachment_url")
    private String proformaAttachmentUrl;

    @Column(name = "proforma_signature_url")
    private String proformaSignatureUrl;

    @Column(name = "proforma_authorized_signatory")
    private String proformaAuthorizedSignatory;

    // Set once this proforma is converted — ties to InvoiceEntity.invoicePrimeId.
    @Column(name = "proforma_converted_to_invoice_id")
    private Long proformaConvertedToInvoiceId;

    // ---- Stored generated PDF (served back via GET /api/v1/proformas/{proformaStrId}) ----
    @Lob
    @Column(name = "proforma_pdf_blob", columnDefinition = "LONGBLOB")
    private byte[] proformaPdfBlob;

    @Column(name = "proforma_pdf_file_name")
    private String proformaPdfFileName;

    // ---- Line items ----
    @OneToMany(mappedBy = "proforma", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<ProformaItemEntity> proformaItems = new ArrayList<>();

    // ---- Audit ----
    @Column(name = "proforma_created_at", updatable = false)
    private LocalDateTime proformaCreatedAt;

    @Column(name = "proforma_updated_at")
    private LocalDateTime proformaUpdatedAt;

    @PrePersist
    protected void onCreate() {
        this.proformaCreatedAt = LocalDateTime.now();
        this.proformaUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.proformaUpdatedAt = LocalDateTime.now();
    }

    public ProformaEntity(){}

    public ProformaEntity(Long proformaPrimeId, String proformaStrId, LocalDate proformaDate, LocalDate proformaValidUntil, Long proformaCustomerId, String proformaCustomerName, String proformaCustomerGst, String proformaCustomerEmail, String proformaCustomerState, String proformaCustomerStateCode, String proformaCustomerAddress, String proformaShippingAddress, CurrencyType proformaCurrency, BigDecimal proformaExchangeRate, BigDecimal proformaSubTotal, BigDecimal proformaDiscountPercent, BigDecimal proformaTaxPercent, BigDecimal proformaShippingCharges, BigDecimal proformaOtherCharges, BigDecimal proformaRoundOff, BigDecimal proformaTotalAmount, BigDecimal proformaTotalAmountInr, ProformaStatus proformaStatus, String proformaPlaceOfSupply, String proformaTerms, String proformaNotes, String proformaReferenceNumber, String proformaPoNumber, String proformaPdfUrl, byte[] proformaPdfData, String proformaPdfContentType, String proformaAttachmentUrl, String proformaSignatureUrl, String proformaAuthorizedSignatory, Long proformaConvertedToInvoiceId, byte[] proformaPdfBlob, String proformaPdfFileName, List<ProformaItemEntity> proformaItems, LocalDateTime proformaCreatedAt, LocalDateTime proformaUpdatedAt) {
        this.proformaPrimeId = proformaPrimeId;
        this.proformaStrId = proformaStrId;
        this.proformaDate = proformaDate;
        this.proformaValidUntil = proformaValidUntil;
        this.proformaCustomerId = proformaCustomerId;
        this.proformaCustomerName = proformaCustomerName;
        this.proformaCustomerGst = proformaCustomerGst;
        this.proformaCustomerEmail = proformaCustomerEmail;
        this.proformaCustomerState = proformaCustomerState;
        this.proformaCustomerStateCode = proformaCustomerStateCode;
        this.proformaCustomerAddress = proformaCustomerAddress;
        this.proformaShippingAddress = proformaShippingAddress;
        this.proformaCurrency = proformaCurrency;
        this.proformaExchangeRate = proformaExchangeRate;
        this.proformaSubTotal = proformaSubTotal;
        this.proformaDiscountPercent = proformaDiscountPercent;
        this.proformaTaxPercent = proformaTaxPercent;
        this.proformaShippingCharges = proformaShippingCharges;
        this.proformaOtherCharges = proformaOtherCharges;
        this.proformaRoundOff = proformaRoundOff;
        this.proformaTotalAmount = proformaTotalAmount;
        this.proformaTotalAmountInr = proformaTotalAmountInr;
        this.proformaStatus = proformaStatus;
        this.proformaPlaceOfSupply = proformaPlaceOfSupply;
        this.proformaTerms = proformaTerms;
        this.proformaNotes = proformaNotes;
        this.proformaReferenceNumber = proformaReferenceNumber;
        this.proformaPoNumber = proformaPoNumber;
        this.proformaPdfUrl = proformaPdfUrl;
        this.proformaPdfData = proformaPdfData;
        this.proformaPdfContentType = proformaPdfContentType;
        this.proformaAttachmentUrl = proformaAttachmentUrl;
        this.proformaSignatureUrl = proformaSignatureUrl;
        this.proformaAuthorizedSignatory = proformaAuthorizedSignatory;
        this.proformaConvertedToInvoiceId = proformaConvertedToInvoiceId;
        this.proformaPdfBlob = proformaPdfBlob;
        this.proformaPdfFileName = proformaPdfFileName;
        this.proformaItems = proformaItems;
        this.proformaCreatedAt = proformaCreatedAt;
        this.proformaUpdatedAt = proformaUpdatedAt;
    }

    public Long getProformaPrimeId() {
        return proformaPrimeId;
    }

    public void setProformaPrimeId(Long proformaPrimeId) {
        this.proformaPrimeId = proformaPrimeId;
    }

    public String getProformaStrId() {
        return proformaStrId;
    }

    public void setProformaStrId(String proformaStrId) {
        this.proformaStrId = proformaStrId;
    }

    public LocalDate getProformaDate() {
        return proformaDate;
    }

    public void setProformaDate(LocalDate proformaDate) {
        this.proformaDate = proformaDate;
    }

    public LocalDate getProformaValidUntil() {
        return proformaValidUntil;
    }

    public void setProformaValidUntil(LocalDate proformaValidUntil) {
        this.proformaValidUntil = proformaValidUntil;
    }

    public Long getProformaCustomerId() {
        return proformaCustomerId;
    }

    public void setProformaCustomerId(Long proformaCustomerId) {
        this.proformaCustomerId = proformaCustomerId;
    }

    public String getProformaCustomerName() {
        return proformaCustomerName;
    }

    public void setProformaCustomerName(String proformaCustomerName) {
        this.proformaCustomerName = proformaCustomerName;
    }

    public String getProformaCustomerGst() {
        return proformaCustomerGst;
    }

    public void setProformaCustomerGst(String proformaCustomerGst) {
        this.proformaCustomerGst = proformaCustomerGst;
    }

    public String getProformaCustomerEmail() {
        return proformaCustomerEmail;
    }

    public void setProformaCustomerEmail(String proformaCustomerEmail) {
        this.proformaCustomerEmail = proformaCustomerEmail;
    }

    public String getProformaCustomerState() {
        return proformaCustomerState;
    }

    public void setProformaCustomerState(String proformaCustomerState) {
        this.proformaCustomerState = proformaCustomerState;
    }

    public String getProformaCustomerStateCode() {
        return proformaCustomerStateCode;
    }

    public void setProformaCustomerStateCode(String proformaCustomerStateCode) {
        this.proformaCustomerStateCode = proformaCustomerStateCode;
    }

    public String getProformaCustomerAddress() {
        return proformaCustomerAddress;
    }

    public void setProformaCustomerAddress(String proformaCustomerAddress) {
        this.proformaCustomerAddress = proformaCustomerAddress;
    }

    public String getProformaShippingAddress() {
        return proformaShippingAddress;
    }

    public void setProformaShippingAddress(String proformaShippingAddress) {
        this.proformaShippingAddress = proformaShippingAddress;
    }

    public CurrencyType getProformaCurrency() {
        return proformaCurrency;
    }

    public void setProformaCurrency(CurrencyType proformaCurrency) {
        this.proformaCurrency = proformaCurrency;
    }

    public BigDecimal getProformaExchangeRate() {
        return proformaExchangeRate;
    }

    public void setProformaExchangeRate(BigDecimal proformaExchangeRate) {
        this.proformaExchangeRate = proformaExchangeRate;
    }

    public BigDecimal getProformaSubTotal() {
        return proformaSubTotal;
    }

    public void setProformaSubTotal(BigDecimal proformaSubTotal) {
        this.proformaSubTotal = proformaSubTotal;
    }

    public BigDecimal getProformaDiscountPercent() {
        return proformaDiscountPercent;
    }

    public void setProformaDiscountPercent(BigDecimal proformaDiscountPercent) {
        this.proformaDiscountPercent = proformaDiscountPercent;
    }

    public BigDecimal getProformaTaxPercent() {
        return proformaTaxPercent;
    }

    public void setProformaTaxPercent(BigDecimal proformaTaxPercent) {
        this.proformaTaxPercent = proformaTaxPercent;
    }

    public BigDecimal getProformaShippingCharges() {
        return proformaShippingCharges;
    }

    public void setProformaShippingCharges(BigDecimal proformaShippingCharges) {
        this.proformaShippingCharges = proformaShippingCharges;
    }

    public BigDecimal getProformaOtherCharges() {
        return proformaOtherCharges;
    }

    public void setProformaOtherCharges(BigDecimal proformaOtherCharges) {
        this.proformaOtherCharges = proformaOtherCharges;
    }

    public BigDecimal getProformaRoundOff() {
        return proformaRoundOff;
    }

    public void setProformaRoundOff(BigDecimal proformaRoundOff) {
        this.proformaRoundOff = proformaRoundOff;
    }

    public BigDecimal getProformaTotalAmount() {
        return proformaTotalAmount;
    }

    public void setProformaTotalAmount(BigDecimal proformaTotalAmount) {
        this.proformaTotalAmount = proformaTotalAmount;
    }

    public BigDecimal getProformaTotalAmountInr() {
        return proformaTotalAmountInr;
    }

    public void setProformaTotalAmountInr(BigDecimal proformaTotalAmountInr) {
        this.proformaTotalAmountInr = proformaTotalAmountInr;
    }

    public ProformaStatus getProformaStatus() {
        return proformaStatus;
    }

    public void setProformaStatus(ProformaStatus proformaStatus) {
        this.proformaStatus = proformaStatus;
    }

    public String getProformaPlaceOfSupply() {
        return proformaPlaceOfSupply;
    }

    public void setProformaPlaceOfSupply(String proformaPlaceOfSupply) {
        this.proformaPlaceOfSupply = proformaPlaceOfSupply;
    }

    public String getProformaTerms() {
        return proformaTerms;
    }

    public void setProformaTerms(String proformaTerms) {
        this.proformaTerms = proformaTerms;
    }

    public String getProformaNotes() {
        return proformaNotes;
    }

    public void setProformaNotes(String proformaNotes) {
        this.proformaNotes = proformaNotes;
    }

    public String getProformaReferenceNumber() {
        return proformaReferenceNumber;
    }

    public void setProformaReferenceNumber(String proformaReferenceNumber) {
        this.proformaReferenceNumber = proformaReferenceNumber;
    }

    public String getProformaPoNumber() {
        return proformaPoNumber;
    }

    public void setProformaPoNumber(String proformaPoNumber) {
        this.proformaPoNumber = proformaPoNumber;
    }

    public String getProformaPdfUrl() {
        return proformaPdfUrl;
    }

    public void setProformaPdfUrl(String proformaPdfUrl) {
        this.proformaPdfUrl = proformaPdfUrl;
    }

    public byte[] getProformaPdfData() {
        return proformaPdfData;
    }

    public void setProformaPdfData(byte[] proformaPdfData) {
        this.proformaPdfData = proformaPdfData;
    }

    public String getProformaPdfContentType() {
        return proformaPdfContentType;
    }

    public void setProformaPdfContentType(String proformaPdfContentType) {
        this.proformaPdfContentType = proformaPdfContentType;
    }

    public String getProformaAttachmentUrl() {
        return proformaAttachmentUrl;
    }

    public void setProformaAttachmentUrl(String proformaAttachmentUrl) {
        this.proformaAttachmentUrl = proformaAttachmentUrl;
    }

    public String getProformaSignatureUrl() {
        return proformaSignatureUrl;
    }

    public void setProformaSignatureUrl(String proformaSignatureUrl) {
        this.proformaSignatureUrl = proformaSignatureUrl;
    }

    public String getProformaAuthorizedSignatory() {
        return proformaAuthorizedSignatory;
    }

    public void setProformaAuthorizedSignatory(String proformaAuthorizedSignatory) {
        this.proformaAuthorizedSignatory = proformaAuthorizedSignatory;
    }

    public Long getProformaConvertedToInvoiceId() {
        return proformaConvertedToInvoiceId;
    }

    public void setProformaConvertedToInvoiceId(Long proformaConvertedToInvoiceId) {
        this.proformaConvertedToInvoiceId = proformaConvertedToInvoiceId;
    }

    public byte[] getProformaPdfBlob() {
        return proformaPdfBlob;
    }

    public void setProformaPdfBlob(byte[] proformaPdfBlob) {
        this.proformaPdfBlob = proformaPdfBlob;
    }

    public String getProformaPdfFileName() {
        return proformaPdfFileName;
    }

    public void setProformaPdfFileName(String proformaPdfFileName) {
        this.proformaPdfFileName = proformaPdfFileName;
    }

    public List<ProformaItemEntity> getProformaItems() {
        return proformaItems;
    }

    public void setProformaItems(List<ProformaItemEntity> proformaItems) {
        this.proformaItems = proformaItems;
    }

    public LocalDateTime getProformaCreatedAt() {
        return proformaCreatedAt;
    }

    public void setProformaCreatedAt(LocalDateTime proformaCreatedAt) {
        this.proformaCreatedAt = proformaCreatedAt;
    }

    public LocalDateTime getProformaUpdatedAt() {
        return proformaUpdatedAt;
    }

    public void setProformaUpdatedAt(LocalDateTime proformaUpdatedAt) {
        this.proformaUpdatedAt = proformaUpdatedAt;
    }
}
