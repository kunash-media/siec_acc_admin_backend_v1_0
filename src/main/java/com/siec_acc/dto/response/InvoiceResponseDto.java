package com.siec_acc.dto.response;

import com.siec_acc.enum_status.CurrencyType;
import com.siec_acc.enum_status.InvoiceStatus;
import com.siec_acc.enum_status.PaymentMode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceResponseDto {

    private Long invoicePrimeId;
    private String invoiceStrId;

    private LocalDate invoiceDate;
    private LocalDate invoiceDueDate;

    private Long invoiceCustomerId;
    private String invoiceCustomerName;
    private String invoiceCustomerGst;
    private String invoiceCustomerEmail;
    private String invoiceCustomerState;
    private String invoiceCustomerStateCode;
    private String invoiceCustomerAddress;
    private String invoiceShippingAddress;

    private CurrencyType invoiceCurrency;
    private BigDecimal invoiceExchangeRate;

    private BigDecimal invoiceSubTotal;
    private BigDecimal invoiceDiscountPercent;
    private BigDecimal invoiceTaxPercent;
    private BigDecimal invoiceShippingCharges;
    private BigDecimal invoiceOtherCharges;
    private BigDecimal invoiceRoundOff;
    private BigDecimal invoiceTotalAmount;
    private BigDecimal invoiceTotalAmountInr;
    private BigDecimal invoicePaidAmount;
    private BigDecimal invoiceBalanceDue;

    private InvoiceStatus invoiceStatus;

    private String invoicePlaceOfSupply;
    private String invoiceTerms;
    private String invoiceNotes;
    private String invoiceReferenceNumber;
    private String invoicePoNumber;
    private String invoiceEwayBillNumber;
    private PaymentMode invoicePaymentMode;

    private String invoicePdfUrl;
    private String invoiceAttachmentUrl;
    private String invoiceSignatureUrl;
    private String invoiceAuthorizedSignatory;

    private Boolean invoiceIsRecurring;
    private String invoiceRecurringFrequency;

    private Long invoiceConvertedFromProformaId;
    private String invoiceTemplateId;

    private List<InvoiceItemResponseDto> invoiceItems;

    private LocalDateTime invoiceCreatedAt;
    private LocalDateTime invoiceUpdatedAt;

    public InvoiceResponseDto(){}

    public InvoiceResponseDto(Long invoicePrimeId, String invoiceStrId, LocalDate invoiceDate, LocalDate invoiceDueDate, Long invoiceCustomerId, String invoiceCustomerName, String invoiceCustomerGst, String invoiceCustomerEmail, String invoiceCustomerState, String invoiceCustomerStateCode, String invoiceCustomerAddress, String invoiceShippingAddress, CurrencyType invoiceCurrency, BigDecimal invoiceExchangeRate, BigDecimal invoiceSubTotal, BigDecimal invoiceDiscountPercent, BigDecimal invoiceTaxPercent, BigDecimal invoiceShippingCharges, BigDecimal invoiceOtherCharges, BigDecimal invoiceRoundOff, BigDecimal invoiceTotalAmount, BigDecimal invoiceTotalAmountInr, BigDecimal invoicePaidAmount, BigDecimal invoiceBalanceDue, InvoiceStatus invoiceStatus, String invoicePlaceOfSupply, String invoiceTerms, String invoiceNotes, String invoiceReferenceNumber, String invoicePoNumber, String invoiceEwayBillNumber, PaymentMode invoicePaymentMode, String invoicePdfUrl, String invoiceAttachmentUrl, String invoiceSignatureUrl, String invoiceAuthorizedSignatory, Boolean invoiceIsRecurring, String invoiceRecurringFrequency, Long invoiceConvertedFromProformaId, String invoiceTemplateId, List<InvoiceItemResponseDto> invoiceItems, LocalDateTime invoiceCreatedAt, LocalDateTime invoiceUpdatedAt) {
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
        this.invoiceBalanceDue = invoiceBalanceDue;
        this.invoiceStatus = invoiceStatus;
        this.invoicePlaceOfSupply = invoicePlaceOfSupply;
        this.invoiceTerms = invoiceTerms;
        this.invoiceNotes = invoiceNotes;
        this.invoiceReferenceNumber = invoiceReferenceNumber;
        this.invoicePoNumber = invoicePoNumber;
        this.invoiceEwayBillNumber = invoiceEwayBillNumber;
        this.invoicePaymentMode = invoicePaymentMode;
        this.invoicePdfUrl = invoicePdfUrl;
        this.invoiceAttachmentUrl = invoiceAttachmentUrl;
        this.invoiceSignatureUrl = invoiceSignatureUrl;
        this.invoiceAuthorizedSignatory = invoiceAuthorizedSignatory;
        this.invoiceIsRecurring = invoiceIsRecurring;
        this.invoiceRecurringFrequency = invoiceRecurringFrequency;
        this.invoiceConvertedFromProformaId = invoiceConvertedFromProformaId;
        this.invoiceTemplateId = invoiceTemplateId;
        this.invoiceItems = invoiceItems;
        this.invoiceCreatedAt = invoiceCreatedAt;
        this.invoiceUpdatedAt = invoiceUpdatedAt;
    }

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

    public BigDecimal getInvoiceBalanceDue() {
        return invoiceBalanceDue;
    }

    public void setInvoiceBalanceDue(BigDecimal invoiceBalanceDue) {
        this.invoiceBalanceDue = invoiceBalanceDue;
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

    public List<InvoiceItemResponseDto> getInvoiceItems() {
        return invoiceItems;
    }

    public void setInvoiceItems(List<InvoiceItemResponseDto> invoiceItems) {
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
