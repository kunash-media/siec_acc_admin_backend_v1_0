package com.siec_acc.dto.response;

import com.siec_acc.enum_status.CurrencyType;
import com.siec_acc.enum_status.ProformaStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProformaResponseDto {

    private Long proformaPrimeId;
    private String proformaStrId;

    private LocalDate proformaDate;
    private LocalDate proformaValidUntil;

    private Long proformaCustomerId;
    private String proformaCustomerName;
    private String proformaCustomerGst;
    private String proformaCustomerEmail;
    private String proformaCustomerState;
    private String proformaCustomerStateCode;
    private String proformaCustomerAddress;
    private String proformaShippingAddress;

    private CurrencyType proformaCurrency;
    private BigDecimal proformaExchangeRate;

    private BigDecimal proformaSubTotal;
    private BigDecimal proformaDiscountPercent;
    private BigDecimal proformaTaxPercent;
    private BigDecimal proformaShippingCharges;
    private BigDecimal proformaOtherCharges;
    private BigDecimal proformaRoundOff;
    private BigDecimal proformaTotalAmount;
    private BigDecimal proformaTotalAmountInr;

    private ProformaStatus proformaStatus;

    private String proformaPlaceOfSupply;
    private String proformaTerms;
    private String proformaNotes;
    private String proformaReferenceNumber;
    private String proformaPoNumber;

    private String proformaPdfUrl;
    private String proformaAttachmentUrl;
    private String proformaSignatureUrl;
    private String proformaAuthorizedSignatory;

    private Long proformaConvertedToInvoiceId;

    private List<ProformaItemResponseDto> proformaItems;

    private LocalDateTime proformaCreatedAt;
    private LocalDateTime proformaUpdatedAt;

    public ProformaResponseDto(){}

    public ProformaResponseDto(Long proformaPrimeId, String proformaStrId, LocalDate proformaDate, LocalDate proformaValidUntil, Long proformaCustomerId, String proformaCustomerName, String proformaCustomerGst, String proformaCustomerEmail, String proformaCustomerState, String proformaCustomerStateCode, String proformaCustomerAddress, String proformaShippingAddress, CurrencyType proformaCurrency, BigDecimal proformaExchangeRate, BigDecimal proformaSubTotal, BigDecimal proformaDiscountPercent, BigDecimal proformaTaxPercent, BigDecimal proformaShippingCharges, BigDecimal proformaOtherCharges, BigDecimal proformaRoundOff, BigDecimal proformaTotalAmount, BigDecimal proformaTotalAmountInr, ProformaStatus proformaStatus, String proformaPlaceOfSupply, String proformaTerms, String proformaNotes, String proformaReferenceNumber, String proformaPoNumber, String proformaPdfUrl, String proformaAttachmentUrl, String proformaSignatureUrl, String proformaAuthorizedSignatory, Long proformaConvertedToInvoiceId, List<ProformaItemResponseDto> proformaItems, LocalDateTime proformaCreatedAt, LocalDateTime proformaUpdatedAt) {
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
        this.proformaAttachmentUrl = proformaAttachmentUrl;
        this.proformaSignatureUrl = proformaSignatureUrl;
        this.proformaAuthorizedSignatory = proformaAuthorizedSignatory;
        this.proformaConvertedToInvoiceId = proformaConvertedToInvoiceId;
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

    public List<ProformaItemResponseDto> getProformaItems() {
        return proformaItems;
    }

    public void setProformaItems(List<ProformaItemResponseDto> proformaItems) {
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
