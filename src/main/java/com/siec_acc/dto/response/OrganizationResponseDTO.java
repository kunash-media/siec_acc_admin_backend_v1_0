package com.siec_acc.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationResponseDTO {

    private String organizationId;
    private String companyName;
    private String legalName;
    private String businessType;
    private String address;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
    private String bankName;
    private String bankAccountNumber;
    private String bankIfscCode;
    private Boolean gstRegistered;
    private String gstNumber;
    private String taxRegistrationType;
    private String taxState;
    private String defaultTaxRate;
    private Boolean taxInclusive;
    private String defaultUnit;
    private String unitSystem;
    private Integer decimalPrecision;
    private String financialYearStart;
    private String timezone;
    private String organizationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrganizationResponseDTO() {
    }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }
    public Boolean getGstRegistered() { return gstRegistered; }
    public void setGstRegistered(Boolean gstRegistered) { this.gstRegistered = gstRegistered; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    public String getTaxRegistrationType() { return taxRegistrationType; }
    public void setTaxRegistrationType(String taxRegistrationType) { this.taxRegistrationType = taxRegistrationType; }
    public String getTaxState() { return taxState; }
    public void setTaxState(String taxState) { this.taxState = taxState; }
    public String getDefaultTaxRate() { return defaultTaxRate; }
    public void setDefaultTaxRate(String defaultTaxRate) { this.defaultTaxRate = defaultTaxRate; }
    public Boolean getTaxInclusive() { return taxInclusive; }
    public void setTaxInclusive(Boolean taxInclusive) { this.taxInclusive = taxInclusive; }
    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String defaultUnit) { this.defaultUnit = defaultUnit; }
    public String getUnitSystem() { return unitSystem; }
    public void setUnitSystem(String unitSystem) { this.unitSystem = unitSystem; }
    public Integer getDecimalPrecision() { return decimalPrecision; }
    public void setDecimalPrecision(Integer decimalPrecision) { this.decimalPrecision = decimalPrecision; }
    public String getFinancialYearStart() { return financialYearStart; }
    public void setFinancialYearStart(String financialYearStart) { this.financialYearStart = financialYearStart; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getOrganizationStatus() { return organizationStatus; }
    public void setOrganizationStatus(String organizationStatus) { this.organizationStatus = organizationStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}