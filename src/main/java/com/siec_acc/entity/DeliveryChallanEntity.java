package com.siec_acc.entity;

import com.siec_acc.enum_status.DcStatus;
import com.siec_acc.enum_status.DcType;
import com.siec_acc.enum_status.QuoteGstType;
import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery_challans")
@Builder
public class DeliveryChallanEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dc_prime_id")
    private Long dcPrimeId;

    @Column(name = "dc_str_id", nullable = false, unique = true)   // e.g. DC-26-27-001
    private String dcStrId;

    @Column(name = "dc_date", nullable = false)
    private LocalDate dcDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "dc_type", nullable = false)
    private DcType dcType;

    // client snapshot
    @Column(name = "dc_client_prime_id", nullable = false) private Long dcClientPrimeId;
    @Column(name = "dc_client_name") private String dcClientName;
    @Column(name = "dc_client_company_name") private String dcClientCompanyName;
    @Column(name = "dc_client_gstin", length = 20) private String dcClientGstin;
    @Column(name = "dc_client_email") private String dcClientEmail;
    @Column(name = "dc_client_state") private String dcClientState;
    @Column(name = "dc_client_state_code", length = 10) private String dcClientStateCode;
    @Column(name = "dc_client_country") private String dcClientCountry;
    @Column(name = "dc_client_billing_address", columnDefinition = "TEXT") private String dcClientBillingAddress;
    @Column(name = "dc_shipping_address", columnDefinition = "TEXT") private String dcShippingAddress;

    // GST context
    @Column(name = "dc_company_state_code", length = 10) private String dcCompanyStateCode;
    @Column(name = "dc_place_of_supply") private String dcPlaceOfSupply;
    @Column(name = "dc_place_of_supply_state_code", length = 10) private String dcPlaceOfSupplyStateCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "dc_gst_type", nullable = false) private QuoteGstType dcGstType;

    // references
    @Column(name = "dc_so_number") private String dcSoNumber;
    @Column(name = "dc_reference_number") private String dcReferenceNumber;
    @Column(name = "dc_project_code") private String dcProjectCode;

    // warehouses
    @Column(name = "dc_warehouse_str_id", length = 30) private String dcWarehouseStrId;
    @Column(name = "dc_warehouse_name") private String dcWarehouseName;
    @Column(name = "dc_dest_warehouse_str_id", length = 30) private String dcDestWarehouseStrId;
    @Column(name = "dc_dest_warehouse_name") private String dcDestWarehouseName;

    // totals
    @Column(name = "dc_sub_total", precision = 15, scale = 2) private BigDecimal dcSubTotal;
    @Column(name = "dc_cgst_amount", precision = 15, scale = 2) private BigDecimal dcCgstAmount;
    @Column(name = "dc_sgst_amount", precision = 15, scale = 2) private BigDecimal dcSgstAmount;
    @Column(name = "dc_igst_amount", precision = 15, scale = 2) private BigDecimal dcIgstAmount;
    @Column(name = "dc_round_off", precision = 10, scale = 2) private BigDecimal dcRoundOff;
    @Column(name = "dc_total_amount", precision = 15, scale = 2) private BigDecimal dcTotalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "dc_status", nullable = false) private DcStatus dcStatus;

    @Column(name = "dc_notes", columnDefinition = "TEXT") private String dcNotes;

    // guard so stock is applied only once
    @Column(name = "dc_stock_applied") private Boolean dcStockApplied;

    // stored PDF
    @Column(name = "dc_pdf_url") private String dcPdfUrl;
    @Column(name = "dc_pdf_content_type") private String dcPdfContentType;
    @Column(name = "dc_pdf_file_name") private String dcPdfFileName;
    @Lob
    @Column(name = "dc_pdf_blob", columnDefinition = "LONGBLOB") private byte[] dcPdfBlob;

    @OneToMany(mappedBy = "dc", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeliveryChallanItemEntity> dcItems = new ArrayList<>();

    @Column(name = "dc_created_at", updatable = false) private LocalDateTime dcCreatedAt;
    @Column(name = "dc_updated_at") private LocalDateTime dcUpdatedAt;

    @PrePersist protected void onCreate() { dcCreatedAt = LocalDateTime.now(); dcUpdatedAt = dcCreatedAt; }
    @PreUpdate protected void onUpdate() { dcUpdatedAt = LocalDateTime.now(); }


    public DeliveryChallanEntity(Long dcPrimeId, String dcStrId, LocalDate dcDate, DcType dcType, Long dcClientPrimeId, String dcClientName, String dcClientCompanyName, String dcClientGstin, String dcClientEmail, String dcClientState, String dcClientStateCode, String dcClientCountry, String dcClientBillingAddress, String dcShippingAddress, String dcCompanyStateCode, String dcPlaceOfSupply, String dcPlaceOfSupplyStateCode, QuoteGstType dcGstType, String dcSoNumber, String dcReferenceNumber, String dcProjectCode, String dcWarehouseStrId, String dcWarehouseName, String dcDestWarehouseStrId, String dcDestWarehouseName, BigDecimal dcSubTotal, BigDecimal dcCgstAmount, BigDecimal dcSgstAmount, BigDecimal dcIgstAmount, BigDecimal dcRoundOff, BigDecimal dcTotalAmount, DcStatus dcStatus, String dcNotes, Boolean dcStockApplied, String dcPdfUrl, String dcPdfContentType, String dcPdfFileName, byte[] dcPdfBlob, List<DeliveryChallanItemEntity> dcItems, LocalDateTime dcCreatedAt, LocalDateTime dcUpdatedAt) {
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
        this.dcStockApplied = dcStockApplied;
        this.dcPdfUrl = dcPdfUrl;
        this.dcPdfContentType = dcPdfContentType;
        this.dcPdfFileName = dcPdfFileName;
        this.dcPdfBlob = dcPdfBlob;
        this.dcItems = dcItems;
        this.dcCreatedAt = dcCreatedAt;
        this.dcUpdatedAt = dcUpdatedAt;
    }

    public DeliveryChallanEntity(){}


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

    public Boolean getDcStockApplied() {
        return dcStockApplied;
    }

    public void setDcStockApplied(Boolean dcStockApplied) {
        this.dcStockApplied = dcStockApplied;
    }

    public String getDcPdfUrl() {
        return dcPdfUrl;
    }

    public void setDcPdfUrl(String dcPdfUrl) {
        this.dcPdfUrl = dcPdfUrl;
    }

    public String getDcPdfContentType() {
        return dcPdfContentType;
    }

    public void setDcPdfContentType(String dcPdfContentType) {
        this.dcPdfContentType = dcPdfContentType;
    }

    public String getDcPdfFileName() {
        return dcPdfFileName;
    }

    public void setDcPdfFileName(String dcPdfFileName) {
        this.dcPdfFileName = dcPdfFileName;
    }

    public byte[] getDcPdfBlob() {
        return dcPdfBlob;
    }

    public void setDcPdfBlob(byte[] dcPdfBlob) {
        this.dcPdfBlob = dcPdfBlob;
    }

    public List<DeliveryChallanItemEntity> getDcItems() {
        return dcItems;
    }

    public void setDcItems(List<DeliveryChallanItemEntity> dcItems) {
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