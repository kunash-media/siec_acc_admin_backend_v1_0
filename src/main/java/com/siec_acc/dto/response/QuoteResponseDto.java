package com.siec_acc.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.siec_acc.enum_status.CurrencyType;
import com.siec_acc.enum_status.QuoteGstType;
import com.siec_acc.enum_status.QuoteStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuoteResponseDto {

    private Long       quotePrimeId;
    private String     quoteStrId;
    private LocalDate  quoteDate;
    private LocalDate  quoteValidTill;

    // ── Client snapshot ────────────────────────────────────────
    private Long       quoteClientPrimeId;
    private String     quoteClientName;
    private String     quoteClientCompanyName;
    private String     quoteClientGstin;
    private String     quoteClientEmail;
    private String     quoteClientState;
    private String     quoteClientStateCode;
    private String     quoteClientCountry;
    private String     quoteClientBillingAddress;
    private String     quoteShippingAddress;

    // ── GST / currency ─────────────────────────────────────────
    private String     quoteCompanyStateCode;
    private String     quotePlaceOfSupply;
    private String     quotePlaceOfSupplyStateCode;
    private QuoteGstType quoteGstType;
    private CurrencyType quoteCurrency;
    private BigDecimal quoteExchangeRate;

    // ── Amounts ────────────────────────────────────────────────
    private BigDecimal quoteSubTotal;
    private BigDecimal quoteDiscountPercent;
    private BigDecimal quoteDiscountAmount;
    private BigDecimal quoteTaxableAmount;
    private BigDecimal quoteCgstAmount;
    private BigDecimal quoteSgstAmount;
    private BigDecimal quoteIgstAmount;
    private BigDecimal quoteShippingCharges;
    private BigDecimal quoteOtherCharges;
    private BigDecimal quoteRoundOff;
    private BigDecimal quoteTotalAmount;
    private BigDecimal quoteTotalAmountInr;

    // ── Meta ───────────────────────────────────────────────────
    private QuoteStatus quoteStatus;
    private String     quoteReferenceNumber;
    private String     quoteCustomerNotes;
    private String     quoteTerms;
    private String       quoteTemplateId;
    private Boolean    quoteConvertedToSo;
    private String     quotePdfUrl;

    // ── Line items ─────────────────────────────────────────────
    private List<QuoteItemResponseDto> quoteItems;

    // ── Audit ──────────────────────────────────────────────────
    private LocalDateTime quoteCreatedAt;
    private LocalDateTime quoteUpdatedAt;


    public QuoteResponseDto(){}

    public QuoteResponseDto(Long quotePrimeId, String quoteStrId, LocalDate quoteDate,
                            LocalDate quoteValidTill, Long quoteClientPrimeId,
                            String quoteClientName, String quoteClientCompanyName,
                            String quoteClientGstin, String quoteClientEmail,
                            String quoteClientState, String quoteClientStateCode,
                            String quoteClientCountry, String quoteClientBillingAddress,
                            String quoteShippingAddress, String quoteCompanyStateCode,
                            String quotePlaceOfSupply, String quotePlaceOfSupplyStateCode,
                            QuoteGstType quoteGstType, CurrencyType quoteCurrency, BigDecimal quoteExchangeRate, BigDecimal quoteSubTotal, BigDecimal quoteDiscountPercent, BigDecimal quoteDiscountAmount, BigDecimal quoteTaxableAmount, BigDecimal quoteCgstAmount, BigDecimal quoteSgstAmount, BigDecimal quoteIgstAmount, BigDecimal quoteShippingCharges, BigDecimal quoteOtherCharges, BigDecimal quoteRoundOff, BigDecimal quoteTotalAmount, BigDecimal quoteTotalAmountInr, QuoteStatus quoteStatus, String quoteReferenceNumber, String quoteCustomerNotes, String quoteTerms, String quoteTemplateId, Boolean quoteConvertedToSo, String quotePdfUrl, List<QuoteItemResponseDto> quoteItems, LocalDateTime quoteCreatedAt, LocalDateTime quoteUpdatedAt) {
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

    public List<QuoteItemResponseDto> getQuoteItems() {
        return quoteItems;
    }

    public void setQuoteItems(List<QuoteItemResponseDto> quoteItems) {
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