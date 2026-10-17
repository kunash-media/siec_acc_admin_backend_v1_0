package com.siec_acc.entity;

import com.siec_acc.enum_status.CurrencyType;
import com.siec_acc.enum_status.QuoteGstType;
import com.siec_acc.enum_status.QuoteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotes")
@Builder
public class QuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_prime_id")
    private Long quotePrimeId;

    // e.g. "QUO-25-26-001"
    @Column(name = "quote_str_id", nullable = false, unique = true)
    private String quoteStrId;

    @Column(name = "quote_date", nullable = false)
    private LocalDate quoteDate;

    @Column(name = "quote_valid_till", nullable = false)
    private LocalDate quoteValidTill;

    // ---- Client snapshot (plain FK -> ClientEntity.clientPrimeId) ----
    @Column(name = "quote_client_prime_id", nullable = false)
    private Long quoteClientPrimeId;

    @Column(name = "quote_client_name")
    private String quoteClientName;

    @Column(name = "quote_client_company_name")
    private String quoteClientCompanyName;

    @Column(name = "quote_client_gstin", length = 20)
    private String quoteClientGstin;

    @Column(name = "quote_client_email")
    private String quoteClientEmail;

    @Column(name = "quote_client_state")
    private String quoteClientState;

    @Column(name = "quote_client_state_code", length = 10)
    private String quoteClientStateCode;

    @Column(name = "quote_client_country")
    private String quoteClientCountry;

    @Column(name = "quote_client_billing_address", columnDefinition = "TEXT")
    private String quoteClientBillingAddress;

    @Column(name = "quote_shipping_address", columnDefinition = "TEXT")
    private String quoteShippingAddress;

    // ---- GST context ----
    // Company state code comes from the frontend (company details are not in DB yet).
    @Column(name = "quote_company_state_code", length = 10)
    private String quoteCompanyStateCode;

    @Column(name = "quote_place_of_supply")
    private String quotePlaceOfSupply;

    @Column(name = "quote_place_of_supply_state_code", length = 10)
    private String quotePlaceOfSupplyStateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "quote_gst_type", nullable = false)
    private QuoteGstType quoteGstType;

    // ---- Currency ----
    @Enumerated(EnumType.STRING)
    @Column(name = "quote_currency", nullable = false)
    private CurrencyType quoteCurrency;

    @Column(name = "quote_exchange_rate", precision = 10, scale = 4)
    private BigDecimal quoteExchangeRate;

    // ---- Totals ----
    @Column(name = "quote_sub_total", precision = 15, scale = 2)
    private BigDecimal quoteSubTotal;

    @Column(name = "quote_discount_percent", precision = 5, scale = 2)
    private BigDecimal quoteDiscountPercent;

    @Column(name = "quote_discount_amount", precision = 15, scale = 2)
    private BigDecimal quoteDiscountAmount;

    @Column(name = "quote_taxable_amount", precision = 15, scale = 2)
    private BigDecimal quoteTaxableAmount;

    @Column(name = "quote_cgst_amount", precision = 15, scale = 2)
    private BigDecimal quoteCgstAmount;

    @Column(name = "quote_sgst_amount", precision = 15, scale = 2)
    private BigDecimal quoteSgstAmount;

    @Column(name = "quote_igst_amount", precision = 15, scale = 2)
    private BigDecimal quoteIgstAmount;

    @Column(name = "quote_shipping_charges", precision = 15, scale = 2)
    private BigDecimal quoteShippingCharges;

    @Column(name = "quote_other_charges", precision = 15, scale = 2)
    private BigDecimal quoteOtherCharges;

    @Column(name = "quote_round_off", precision = 10, scale = 2)
    private BigDecimal quoteRoundOff;

    @Column(name = "quote_total_amount", precision = 15, scale = 2)
    private BigDecimal quoteTotalAmount;

    @Column(name = "quote_total_amount_inr", precision = 15, scale = 2)
    private BigDecimal quoteTotalAmountInr;

    // ---- Status / meta ----
    @Enumerated(EnumType.STRING)
    @Column(name = "quote_status", nullable = false)
    private QuoteStatus quoteStatus;

    @Column(name = "quote_reference_number")
    private String quoteReferenceNumber;

    // Printed on the PDF (the "Note-" block next to totals)
    @Column(name = "quote_customer_notes", columnDefinition = "TEXT")
    private String quoteCustomerNotes;

    @Column(name = "quote_terms", columnDefinition = "TEXT")
    private String quoteTerms;

    @Column(name = "quote_template_id")
    private String quoteTemplateId;

    @Column(name = "quote_converted_to_so")
    private Boolean quoteConvertedToSo;

    // ---- Stored PDF ----
    @Column(name = "quote_pdf_url")
    private String quotePdfUrl;

    @Column(name = "quote_pdf_content_type")
    private String quotePdfContentType;

    @Column(name = "quote_pdf_file_name")
    private String quotePdfFileName;

    @Lob
    @Column(name = "quote_pdf_blob", columnDefinition = "LONGBLOB")
    private byte[] quotePdfBlob;

    // ---- Line items ----
    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QuoteItemEntity> quoteItems = new ArrayList<>();

    // ---- Audit ----
    @Column(name = "quote_created_at", updatable = false)
    private LocalDateTime quoteCreatedAt;

    @Column(name = "quote_updated_at")
    private LocalDateTime quoteUpdatedAt;

    @PrePersist
    protected void onCreate() {
        this.quoteCreatedAt = LocalDateTime.now();
        this.quoteUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.quoteUpdatedAt = LocalDateTime.now();
    }

    public QuoteEntity(){}

    public QuoteEntity(Long quotePrimeId, String quoteStrId, LocalDate quoteDate, LocalDate quoteValidTill, Long quoteClientPrimeId, String quoteClientName, String quoteClientCompanyName, String quoteClientGstin, String quoteClientEmail, String quoteClientState, String quoteClientStateCode, String quoteClientCountry, String quoteClientBillingAddress, String quoteShippingAddress, String quoteCompanyStateCode, String quotePlaceOfSupply, String quotePlaceOfSupplyStateCode, QuoteGstType quoteGstType, CurrencyType quoteCurrency, BigDecimal quoteExchangeRate, BigDecimal quoteSubTotal, BigDecimal quoteDiscountPercent, BigDecimal quoteDiscountAmount, BigDecimal quoteTaxableAmount, BigDecimal quoteCgstAmount, BigDecimal quoteSgstAmount, BigDecimal quoteIgstAmount, BigDecimal quoteShippingCharges, BigDecimal quoteOtherCharges, BigDecimal quoteRoundOff, BigDecimal quoteTotalAmount, BigDecimal quoteTotalAmountInr, QuoteStatus quoteStatus, String quoteReferenceNumber, String quoteCustomerNotes, String quoteTerms, String quoteTemplateId, Boolean quoteConvertedToSo, String quotePdfUrl, String quotePdfContentType, String quotePdfFileName, byte[] quotePdfBlob, List<QuoteItemEntity> quoteItems, LocalDateTime quoteCreatedAt, LocalDateTime quoteUpdatedAt) {
        this.quotePrimeId = quotePrimeId;
        this.quoteStrId = quoteStrId;
        this.quoteDate = quoteDate;
        this.quoteValidTill = quoteValidTill;
        this.quoteClientPrimeId = quoteClientPrimeId;
        this.quoteClientName = quoteClientName;
        this.quoteClientCompanyName = quoteClientCompanyName;
        this.quoteClientGstin = quoteClientGstin;
        this.quoteClientEmail = quoteClientEmail;
        this.quoteClientState = quoteClientState;
        this.quoteClientStateCode = quoteClientStateCode;
        this.quoteClientCountry = quoteClientCountry;
        this.quoteClientBillingAddress = quoteClientBillingAddress;
        this.quoteShippingAddress = quoteShippingAddress;
        this.quoteCompanyStateCode = quoteCompanyStateCode;
        this.quotePlaceOfSupply = quotePlaceOfSupply;
        this.quotePlaceOfSupplyStateCode = quotePlaceOfSupplyStateCode;
        this.quoteGstType = quoteGstType;
        this.quoteCurrency = quoteCurrency;
        this.quoteExchangeRate = quoteExchangeRate;
        this.quoteSubTotal = quoteSubTotal;
        this.quoteDiscountPercent = quoteDiscountPercent;
        this.quoteDiscountAmount = quoteDiscountAmount;
        this.quoteTaxableAmount = quoteTaxableAmount;
        this.quoteCgstAmount = quoteCgstAmount;
        this.quoteSgstAmount = quoteSgstAmount;
        this.quoteIgstAmount = quoteIgstAmount;
        this.quoteShippingCharges = quoteShippingCharges;
        this.quoteOtherCharges = quoteOtherCharges;
        this.quoteRoundOff = quoteRoundOff;
        this.quoteTotalAmount = quoteTotalAmount;
        this.quoteTotalAmountInr = quoteTotalAmountInr;
        this.quoteStatus = quoteStatus;
        this.quoteReferenceNumber = quoteReferenceNumber;
        this.quoteCustomerNotes = quoteCustomerNotes;
        this.quoteTerms = quoteTerms;
        this.quoteTemplateId = quoteTemplateId;
        this.quoteConvertedToSo = quoteConvertedToSo;
        this.quotePdfUrl = quotePdfUrl;
        this.quotePdfContentType = quotePdfContentType;
        this.quotePdfFileName = quotePdfFileName;
        this.quotePdfBlob = quotePdfBlob;
        this.quoteItems = quoteItems;
        this.quoteCreatedAt = quoteCreatedAt;
        this.quoteUpdatedAt = quoteUpdatedAt;
    }

    public Long getQuotePrimeId() {
        return quotePrimeId;
    }

    public void setQuotePrimeId(Long quotePrimeId) {
        this.quotePrimeId = quotePrimeId;
    }

    public String getQuoteStrId() {
        return quoteStrId;
    }

    public void setQuoteStrId(String quoteStrId) {
        this.quoteStrId = quoteStrId;
    }

    public LocalDate getQuoteDate() {
        return quoteDate;
    }

    public void setQuoteDate(LocalDate quoteDate) {
        this.quoteDate = quoteDate;
    }

    public LocalDate getQuoteValidTill() {
        return quoteValidTill;
    }

    public void setQuoteValidTill(LocalDate quoteValidTill) {
        this.quoteValidTill = quoteValidTill;
    }

    public Long getQuoteClientPrimeId() {
        return quoteClientPrimeId;
    }

    public void setQuoteClientPrimeId(Long quoteClientPrimeId) {
        this.quoteClientPrimeId = quoteClientPrimeId;
    }

    public String getQuoteClientName() {
        return quoteClientName;
    }

    public void setQuoteClientName(String quoteClientName) {
        this.quoteClientName = quoteClientName;
    }

    public String getQuoteClientCompanyName() {
        return quoteClientCompanyName;
    }

    public void setQuoteClientCompanyName(String quoteClientCompanyName) {
        this.quoteClientCompanyName = quoteClientCompanyName;
    }

    public String getQuoteClientGstin() {
        return quoteClientGstin;
    }

    public void setQuoteClientGstin(String quoteClientGstin) {
        this.quoteClientGstin = quoteClientGstin;
    }

    public String getQuoteClientEmail() {
        return quoteClientEmail;
    }

    public void setQuoteClientEmail(String quoteClientEmail) {
        this.quoteClientEmail = quoteClientEmail;
    }

    public String getQuoteClientState() {
        return quoteClientState;
    }

    public void setQuoteClientState(String quoteClientState) {
        this.quoteClientState = quoteClientState;
    }

    public String getQuoteClientStateCode() {
        return quoteClientStateCode;
    }

    public void setQuoteClientStateCode(String quoteClientStateCode) {
        this.quoteClientStateCode = quoteClientStateCode;
    }

    public String getQuoteClientCountry() {
        return quoteClientCountry;
    }

    public void setQuoteClientCountry(String quoteClientCountry) {
        this.quoteClientCountry = quoteClientCountry;
    }

    public String getQuoteClientBillingAddress() {
        return quoteClientBillingAddress;
    }

    public void setQuoteClientBillingAddress(String quoteClientBillingAddress) {
        this.quoteClientBillingAddress = quoteClientBillingAddress;
    }

    public String getQuoteShippingAddress() {
        return quoteShippingAddress;
    }

    public void setQuoteShippingAddress(String quoteShippingAddress) {
        this.quoteShippingAddress = quoteShippingAddress;
    }

    public String getQuoteCompanyStateCode() {
        return quoteCompanyStateCode;
    }

    public void setQuoteCompanyStateCode(String quoteCompanyStateCode) {
        this.quoteCompanyStateCode = quoteCompanyStateCode;
    }

    public String getQuotePlaceOfSupply() {
        return quotePlaceOfSupply;
    }

    public void setQuotePlaceOfSupply(String quotePlaceOfSupply) {
        this.quotePlaceOfSupply = quotePlaceOfSupply;
    }

    public String getQuotePlaceOfSupplyStateCode() {
        return quotePlaceOfSupplyStateCode;
    }

    public void setQuotePlaceOfSupplyStateCode(String quotePlaceOfSupplyStateCode) {
        this.quotePlaceOfSupplyStateCode = quotePlaceOfSupplyStateCode;
    }

    public QuoteGstType getQuoteGstType() {
        return quoteGstType;
    }

    public void setQuoteGstType(QuoteGstType quoteGstType) {
        this.quoteGstType = quoteGstType;
    }

    public CurrencyType getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(CurrencyType quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public BigDecimal getQuoteExchangeRate() {
        return quoteExchangeRate;
    }

    public void setQuoteExchangeRate(BigDecimal quoteExchangeRate) {
        this.quoteExchangeRate = quoteExchangeRate;
    }

    public BigDecimal getQuoteSubTotal() {
        return quoteSubTotal;
    }

    public void setQuoteSubTotal(BigDecimal quoteSubTotal) {
        this.quoteSubTotal = quoteSubTotal;
    }

    public BigDecimal getQuoteDiscountPercent() {
        return quoteDiscountPercent;
    }

    public void setQuoteDiscountPercent(BigDecimal quoteDiscountPercent) {
        this.quoteDiscountPercent = quoteDiscountPercent;
    }

    public BigDecimal getQuoteDiscountAmount() {
        return quoteDiscountAmount;
    }

    public void setQuoteDiscountAmount(BigDecimal quoteDiscountAmount) {
        this.quoteDiscountAmount = quoteDiscountAmount;
    }

    public BigDecimal getQuoteTaxableAmount() {
        return quoteTaxableAmount;
    }

    public void setQuoteTaxableAmount(BigDecimal quoteTaxableAmount) {
        this.quoteTaxableAmount = quoteTaxableAmount;
    }

    public BigDecimal getQuoteCgstAmount() {
        return quoteCgstAmount;
    }

    public void setQuoteCgstAmount(BigDecimal quoteCgstAmount) {
        this.quoteCgstAmount = quoteCgstAmount;
    }

    public BigDecimal getQuoteSgstAmount() {
        return quoteSgstAmount;
    }

    public void setQuoteSgstAmount(BigDecimal quoteSgstAmount) {
        this.quoteSgstAmount = quoteSgstAmount;
    }

    public BigDecimal getQuoteIgstAmount() {
        return quoteIgstAmount;
    }

    public void setQuoteIgstAmount(BigDecimal quoteIgstAmount) {
        this.quoteIgstAmount = quoteIgstAmount;
    }

    public BigDecimal getQuoteShippingCharges() {
        return quoteShippingCharges;
    }

    public void setQuoteShippingCharges(BigDecimal quoteShippingCharges) {
        this.quoteShippingCharges = quoteShippingCharges;
    }

    public BigDecimal getQuoteOtherCharges() {
        return quoteOtherCharges;
    }

    public void setQuoteOtherCharges(BigDecimal quoteOtherCharges) {
        this.quoteOtherCharges = quoteOtherCharges;
    }

    public BigDecimal getQuoteRoundOff() {
        return quoteRoundOff;
    }

    public void setQuoteRoundOff(BigDecimal quoteRoundOff) {
        this.quoteRoundOff = quoteRoundOff;
    }

    public BigDecimal getQuoteTotalAmount() {
        return quoteTotalAmount;
    }

    public void setQuoteTotalAmount(BigDecimal quoteTotalAmount) {
        this.quoteTotalAmount = quoteTotalAmount;
    }

    public BigDecimal getQuoteTotalAmountInr() {
        return quoteTotalAmountInr;
    }

    public void setQuoteTotalAmountInr(BigDecimal quoteTotalAmountInr) {
        this.quoteTotalAmountInr = quoteTotalAmountInr;
    }

    public QuoteStatus getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(QuoteStatus quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    public String getQuoteReferenceNumber() {
        return quoteReferenceNumber;
    }

    public void setQuoteReferenceNumber(String quoteReferenceNumber) {
        this.quoteReferenceNumber = quoteReferenceNumber;
    }

    public String getQuoteCustomerNotes() {
        return quoteCustomerNotes;
    }

    public void setQuoteCustomerNotes(String quoteCustomerNotes) {
        this.quoteCustomerNotes = quoteCustomerNotes;
    }

    public String getQuoteTerms() {
        return quoteTerms;
    }

    public void setQuoteTerms(String quoteTerms) {
        this.quoteTerms = quoteTerms;
    }

    public String getQuoteTemplateId() {
        return quoteTemplateId;
    }

    public void setQuoteTemplateId(String quoteTemplateId) {
        this.quoteTemplateId = quoteTemplateId;
    }

    public Boolean getQuoteConvertedToSo() {
        return quoteConvertedToSo;
    }

    public void setQuoteConvertedToSo(Boolean quoteConvertedToSo) {
        this.quoteConvertedToSo = quoteConvertedToSo;
    }

    public String getQuotePdfUrl() {
        return quotePdfUrl;
    }

    public void setQuotePdfUrl(String quotePdfUrl) {
        this.quotePdfUrl = quotePdfUrl;
    }

    public String getQuotePdfContentType() {
        return quotePdfContentType;
    }

    public void setQuotePdfContentType(String quotePdfContentType) {
        this.quotePdfContentType = quotePdfContentType;
    }

    public String getQuotePdfFileName() {
        return quotePdfFileName;
    }

    public void setQuotePdfFileName(String quotePdfFileName) {
        this.quotePdfFileName = quotePdfFileName;
    }

    public byte[] getQuotePdfBlob() {
        return quotePdfBlob;
    }

    public void setQuotePdfBlob(byte[] quotePdfBlob) {
        this.quotePdfBlob = quotePdfBlob;
    }

    public List<QuoteItemEntity> getQuoteItems() {
        return quoteItems;
    }

    public void setQuoteItems(List<QuoteItemEntity> quoteItems) {
        this.quoteItems = quoteItems;
    }

    public LocalDateTime getQuoteCreatedAt() {
        return quoteCreatedAt;
    }

    public void setQuoteCreatedAt(LocalDateTime quoteCreatedAt) {
        this.quoteCreatedAt = quoteCreatedAt;
    }

    public LocalDateTime getQuoteUpdatedAt() {
        return quoteUpdatedAt;
    }

    public void setQuoteUpdatedAt(LocalDateTime quoteUpdatedAt) {
        this.quoteUpdatedAt = quoteUpdatedAt;
    }
}