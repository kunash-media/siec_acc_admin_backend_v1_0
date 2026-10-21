package com.siec_acc.dto.response;

import com.siec_acc.enum_status.DcStatus;
import com.siec_acc.enum_status.DcType;
import com.siec_acc.enum_status.QuoteGstType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public class DeliveryChallanResponseDto {
    private Long dcPrimeId;
    private String dcStrId;
    private LocalDate dcDate;
    private DcType dcType;
    private Long dcClientPrimeId;
    private String dcClientName;
    private String dcClientCompanyName;
    private String dcClientGstin;
    private String dcClientEmail;
    private String dcClientState;
    private String dcClientStateCode;
    private String dcClientCountry;
    private String dcClientBillingAddress;
    private String dcShippingAddress;
    private String dcCompanyStateCode;
    private String dcPlaceOfSupply;
    private String dcPlaceOfSupplyStateCode;
    private QuoteGstType dcGstType;
    private String dcSoNumber;
    private String dcReferenceNumber;
    private String dcProjectCode;
    private String dcWarehouseStrId;
    private String dcWarehouseName;
    private String dcDestWarehouseStrId;
    private String dcDestWarehouseName;
    private BigDecimal dcSubTotal;
    private BigDecimal dcCgstAmount;
    private BigDecimal dcSgstAmount;
    private BigDecimal dcIgstAmount;
    private BigDecimal dcRoundOff;
    private BigDecimal dcTotalAmount;
    private DcStatus dcStatus;
    private String dcNotes;
    private String dcPdfUrl;
    private List<DeliveryChallanItemResponseDto> dcItems;
    private LocalDateTime dcCreatedAt;
    private LocalDateTime dcUpdatedAt;

    public DeliveryChallanResponseDto(Long dcPrimeId, String dcStrId, LocalDate dcDate, DcType dcType, Long dcClientPrimeId, String dcClientName, String dcClientCompanyName, String dcClientGstin, String dcClientEmail, String dcClientState, String dcClientStateCode, String dcClientCountry, String dcClientBillingAddress, String dcShippingAddress, String dcCompanyStateCode, String dcPlaceOfSupply, String dcPlaceOfSupplyStateCode, QuoteGstType dcGstType, String dcSoNumber, String dcReferenceNumber, String dcProjectCode, String dcWarehouseStrId, String dcWarehouseName, String dcDestWarehouseStrId, String dcDestWarehouseName, BigDecimal dcSubTotal, BigDecimal dcCgstAmount, BigDecimal dcSgstAmount, BigDecimal dcIgstAmount, BigDecimal dcRoundOff, BigDecimal dcTotalAmount, DcStatus dcStatus, String dcNotes, String dcPdfUrl, List<DeliveryChallanItemResponseDto> dcItems, LocalDateTime dcCreatedAt, LocalDateTime dcUpdatedAt) {
        this.dcPrimeId = dcPrimeId;
        this.dcStrId = dcStrId;
        this.dcDate = dcDate;
        this.dcType = dcType;
        this.dcClientPrimeId = dcClientPrimeId;
        this.dcClientName = dcClientName;
        this.dcClientCompanyName = dcClientCompanyName;
        this.dcClientGstin = dcClientGstin;
        this.dcClientEmail = dcClientEmail;
        this.dcClientState = dcClientState;
        this.dcClientStateCode = dcClientStateCode;
        this.dcClientCountry = dcClientCountry;
        this.dcClientBillingAddress = dcClientBillingAddress;
        this.dcShippingAddress = dcShippingAddress;
        this.dcCompanyStateCode = dcCompanyStateCode;
        this.dcPlaceOfSupply = dcPlaceOfSupply;
        this.dcPlaceOfSupplyStateCode = dcPlaceOfSupplyStateCode;
        this.dcGstType = dcGstType;
        this.dcSoNumber = dcSoNumber;
        this.dcReferenceNumber = dcReferenceNumber;
        this.dcProjectCode = dcProjectCode;
        this.dcWarehouseStrId = dcWarehouseStrId;
        this.dcWarehouseName = dcWarehouseName;
        this.dcDestWarehouseStrId = dcDestWarehouseStrId;
        this.dcDestWarehouseName = dcDestWarehouseName;
        this.dcSubTotal = dcSubTotal;
        this.dcCgstAmount = dcCgstAmount;
        this.dcSgstAmount = dcSgstAmount;
        this.dcIgstAmount = dcIgstAmount;
        this.dcRoundOff = dcRoundOff;
        this.dcTotalAmount = dcTotalAmount;
        this.dcStatus = dcStatus;
        this.dcNotes = dcNotes;
        this.dcPdfUrl = dcPdfUrl;
        this.dcItems = dcItems;
        this.dcCreatedAt = dcCreatedAt;
        this.dcUpdatedAt = dcUpdatedAt;
    }

    public DeliveryChallanResponseDto(){}


    public Long getDcPrimeId() {
        return dcPrimeId;
    }

    public void setDcPrimeId(Long dcPrimeId) {
        this.dcPrimeId = dcPrimeId;
    }

    public String getDcStrId() {
        return dcStrId;
    }

    public void setDcStrId(String dcStrId) {
        this.dcStrId = dcStrId;
    }

    public LocalDate getDcDate() {
        return dcDate;
    }

    public void setDcDate(LocalDate dcDate) {
        this.dcDate = dcDate;
    }

    public DcType getDcType() {
        return dcType;
    }

    public void setDcType(DcType dcType) {
        this.dcType = dcType;
    }

    public Long getDcClientPrimeId() {
        return dcClientPrimeId;
    }

    public void setDcClientPrimeId(Long dcClientPrimeId) {
        this.dcClientPrimeId = dcClientPrimeId;
    }

    public String getDcClientName() {
        return dcClientName;
    }

    public void setDcClientName(String dcClientName) {
        this.dcClientName = dcClientName;
    }

    public String getDcClientCompanyName() {
        return dcClientCompanyName;
    }

    public void setDcClientCompanyName(String dcClientCompanyName) {
        this.dcClientCompanyName = dcClientCompanyName;
    }

    public String getDcClientGstin() {
        return dcClientGstin;
    }

    public void setDcClientGstin(String dcClientGstin) {
        this.dcClientGstin = dcClientGstin;
    }

    public String getDcClientEmail() {
        return dcClientEmail;
    }

    public void setDcClientEmail(String dcClientEmail) {
        this.dcClientEmail = dcClientEmail;
    }

    public String getDcClientState() {
        return dcClientState;
    }

    public void setDcClientState(String dcClientState) {
        this.dcClientState = dcClientState;
    }

    public String getDcClientStateCode() {
        return dcClientStateCode;
    }

    public void setDcClientStateCode(String dcClientStateCode) {
        this.dcClientStateCode = dcClientStateCode;
    }

    public String getDcClientCountry() {
        return dcClientCountry;
    }

    public void setDcClientCountry(String dcClientCountry) {
        this.dcClientCountry = dcClientCountry;
    }

    public String getDcClientBillingAddress() {
        return dcClientBillingAddress;
    }

    public void setDcClientBillingAddress(String dcClientBillingAddress) {
        this.dcClientBillingAddress = dcClientBillingAddress;
    }

    public String getDcShippingAddress() {
        return dcShippingAddress;
    }

    public void setDcShippingAddress(String dcShippingAddress) {
        this.dcShippingAddress = dcShippingAddress;
    }

    public String getDcCompanyStateCode() {
        return dcCompanyStateCode;
    }

    public void setDcCompanyStateCode(String dcCompanyStateCode) {
        this.dcCompanyStateCode = dcCompanyStateCode;
    }

    public String getDcPlaceOfSupply() {
        return dcPlaceOfSupply;
    }

    public void setDcPlaceOfSupply(String dcPlaceOfSupply) {
        this.dcPlaceOfSupply = dcPlaceOfSupply;
    }

    public String getDcPlaceOfSupplyStateCode() {
        return dcPlaceOfSupplyStateCode;
    }

    public void setDcPlaceOfSupplyStateCode(String dcPlaceOfSupplyStateCode) {
        this.dcPlaceOfSupplyStateCode = dcPlaceOfSupplyStateCode;
    }

    public QuoteGstType getDcGstType() {
        return dcGstType;
    }

    public void setDcGstType(QuoteGstType dcGstType) {
        this.dcGstType = dcGstType;
    }

    public String getDcSoNumber() {
        return dcSoNumber;
    }

    public void setDcSoNumber(String dcSoNumber) {
        this.dcSoNumber = dcSoNumber;
    }

    public String getDcReferenceNumber() {
        return dcReferenceNumber;
    }

    public void setDcReferenceNumber(String dcReferenceNumber) {
        this.dcReferenceNumber = dcReferenceNumber;
    }

    public String getDcProjectCode() {
        return dcProjectCode;
    }

    public void setDcProjectCode(String dcProjectCode) {
        this.dcProjectCode = dcProjectCode;
    }

    public String getDcWarehouseStrId() {
        return dcWarehouseStrId;
    }

    public void setDcWarehouseStrId(String dcWarehouseStrId) {
        this.dcWarehouseStrId = dcWarehouseStrId;
    }

    public String getDcWarehouseName() {
        return dcWarehouseName;
    }

    public void setDcWarehouseName(String dcWarehouseName) {
        this.dcWarehouseName = dcWarehouseName;
    }

    public String getDcDestWarehouseStrId() {
        return dcDestWarehouseStrId;
    }

    public void setDcDestWarehouseStrId(String dcDestWarehouseStrId) {
        this.dcDestWarehouseStrId = dcDestWarehouseStrId;
    }

    public String getDcDestWarehouseName() {
        return dcDestWarehouseName;
    }

    public void setDcDestWarehouseName(String dcDestWarehouseName) {
        this.dcDestWarehouseName = dcDestWarehouseName;
    }

    public BigDecimal getDcSubTotal() {
        return dcSubTotal;
    }

    public void setDcSubTotal(BigDecimal dcSubTotal) {
        this.dcSubTotal = dcSubTotal;
    }

    public BigDecimal getDcCgstAmount() {
        return dcCgstAmount;
    }

    public void setDcCgstAmount(BigDecimal dcCgstAmount) {
        this.dcCgstAmount = dcCgstAmount;
    }

    public BigDecimal getDcSgstAmount() {
        return dcSgstAmount;
    }

    public void setDcSgstAmount(BigDecimal dcSgstAmount) {
        this.dcSgstAmount = dcSgstAmount;
    }

    public BigDecimal getDcIgstAmount() {
        return dcIgstAmount;
    }

    public void setDcIgstAmount(BigDecimal dcIgstAmount) {
        this.dcIgstAmount = dcIgstAmount;
    }

    public BigDecimal getDcRoundOff() {
        return dcRoundOff;
    }

    public void setDcRoundOff(BigDecimal dcRoundOff) {
        this.dcRoundOff = dcRoundOff;
    }

    public BigDecimal getDcTotalAmount() {
        return dcTotalAmount;
    }

    public void setDcTotalAmount(BigDecimal dcTotalAmount) {
        this.dcTotalAmount = dcTotalAmount;
    }

    public DcStatus getDcStatus() {
        return dcStatus;
    }

    public void setDcStatus(DcStatus dcStatus) {
        this.dcStatus = dcStatus;
    }

    public String getDcNotes() {
        return dcNotes;
    }

    public void setDcNotes(String dcNotes) {
        this.dcNotes = dcNotes;
    }

    public String getDcPdfUrl() {
        return dcPdfUrl;
    }

    public void setDcPdfUrl(String dcPdfUrl) {
        this.dcPdfUrl = dcPdfUrl;
    }

    public List<DeliveryChallanItemResponseDto> getDcItems() {
        return dcItems;
    }

    public void setDcItems(List<DeliveryChallanItemResponseDto> dcItems) {
        this.dcItems = dcItems;
    }

    public LocalDateTime getDcCreatedAt() {
        return dcCreatedAt;
    }

    public void setDcCreatedAt(LocalDateTime dcCreatedAt) {
        this.dcCreatedAt = dcCreatedAt;
    }

    public LocalDateTime getDcUpdatedAt() {
        return dcUpdatedAt;
    }

    public void setDcUpdatedAt(LocalDateTime dcUpdatedAt) {
        this.dcUpdatedAt = dcUpdatedAt;
    }
}