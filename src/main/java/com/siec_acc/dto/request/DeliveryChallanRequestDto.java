package com.siec_acc.dto.request;

import com.siec_acc.enum_status.DcType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public class DeliveryChallanRequestDto {
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

    private String dcSoNumber;
    private String dcReferenceNumber;
    private String dcProjectCode;

    private String dcWarehouseStrId;
    private String dcDestWarehouseStrId;   // only for STOCK_TRANSFER

    private BigDecimal dcRoundOff;
    private String dcNotes;

    private List<DeliveryChallanItemRequestDto> dcItems;

    public DeliveryChallanRequestDto(){}

    public DeliveryChallanRequestDto(LocalDate dcDate, DcType dcType, Long dcClientPrimeId, String dcClientName, String dcClientCompanyName, String dcClientGstin, String dcClientEmail, String dcClientState, String dcClientStateCode, String dcClientCountry, String dcClientBillingAddress, String dcShippingAddress, String dcCompanyStateCode, String dcPlaceOfSupply, String dcPlaceOfSupplyStateCode, String dcSoNumber, String dcReferenceNumber, String dcProjectCode, String dcWarehouseStrId, String dcDestWarehouseStrId, BigDecimal dcRoundOff, String dcNotes, List<DeliveryChallanItemRequestDto> dcItems) {
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
        this.dcSoNumber = dcSoNumber;
        this.dcReferenceNumber = dcReferenceNumber;
        this.dcProjectCode = dcProjectCode;
        this.dcWarehouseStrId = dcWarehouseStrId;
        this.dcDestWarehouseStrId = dcDestWarehouseStrId;
        this.dcRoundOff = dcRoundOff;
        this.dcNotes = dcNotes;
        this.dcItems = dcItems;
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

    public String getDcDestWarehouseStrId() {
        return dcDestWarehouseStrId;
    }

    public void setDcDestWarehouseStrId(String dcDestWarehouseStrId) {
        this.dcDestWarehouseStrId = dcDestWarehouseStrId;
    }

    public BigDecimal getDcRoundOff() {
        return dcRoundOff;
    }

    public void setDcRoundOff(BigDecimal dcRoundOff) {
        this.dcRoundOff = dcRoundOff;
    }

    public String getDcNotes() {
        return dcNotes;
    }

    public void setDcNotes(String dcNotes) {
        this.dcNotes = dcNotes;
    }

    public List<DeliveryChallanItemRequestDto> getDcItems() {
        return dcItems;
    }

    public void setDcItems(List<DeliveryChallanItemRequestDto> dcItems) {
        this.dcItems = dcItems;
    }
}