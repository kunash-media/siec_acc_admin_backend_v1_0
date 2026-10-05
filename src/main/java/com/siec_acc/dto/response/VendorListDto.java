package com.siec_acc.dto.response;

public class VendorListDto {
    private Long vendorPrimeId;
    private String vendorStrId;
    private String vendorName;
    private String companyName;

    public VendorListDto() {}

    public VendorListDto(Long vendorPrimeId, String vendorStrId, String vendorName, String companyName) {
        this.vendorPrimeId = vendorPrimeId;
        this.vendorStrId = vendorStrId;
        this.vendorName = vendorName;
        this.companyName = companyName;
    }

    public Long getVendorPrimeId() {
        return vendorPrimeId;
    }
    public void setVendorPrimeId(Long v) {
        vendorPrimeId = v;
    }
    public String getVendorStrId() {
        return vendorStrId;
    }
    public void setVendorStrId(String v) {
        vendorStrId = v;
    }
    public String getVendorName() {
        return vendorName;
    }
    public void setVendorName(String v) {
        vendorName = v;
    }
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String v) {
        companyName = v;
    }
}
