package com.siec_acc.dto.response;


import lombok.Builder;

@Builder
public class VendorQuickCreateResponseDto {
    private Long vendorPrimeId;
    private String vendorStrId;
    private String vendorName;
    private String companyName;
    private String phone;

    public VendorQuickCreateResponseDto(){}

    public VendorQuickCreateResponseDto(Long vendorPrimeId, String vendorStrId, String vendorName, String companyName, String phone) {
        this.vendorPrimeId = vendorPrimeId;
        this.vendorStrId = vendorStrId;
        this.vendorName = vendorName;
        this.companyName = companyName;
        this.phone = phone;
    }

    public Long getVendorPrimeId() {
        return vendorPrimeId;
    }

    public void setVendorPrimeId(Long vendorPrimeId) {
        this.vendorPrimeId = vendorPrimeId;
    }

    public String getVendorStrId() {
        return vendorStrId;
    }

    public void setVendorStrId(String vendorStrId) {
        this.vendorStrId = vendorStrId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
