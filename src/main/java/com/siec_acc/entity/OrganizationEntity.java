package com.siec_acc.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization")
public class OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_prime_id")
    private Long organizationPrimeId;

    @Column(name = "organization_id", unique = true, nullable = false, length = 40)
    private String organizationId;
    @Column(name = "company_name") private String companyName;
    @Column(name = "legal_name") private String legalName;
    @Column(name = "business_type") private String businessType;
    @Column(name = "address", columnDefinition = "TEXT") private String address;
    @Column(name = "city") private String city;
    @Column(name = "state") private String state;
    @Column(name = "country") private String country;
    @Column(name = "pincode") private String pincode;
    @Column(name = "phone") private String phone;
    @Column(name = "email") private String email;
    @Column(name = "website") private String website;

    @Lob
    @Column(name = "logo_blob", columnDefinition = "LONGBLOB")
    private byte[] logoBlob;

    @Column(name = "logo_url", length = 255) private String logoUrl;
    @Column(name = "bank_name") private String bankName;
    @Column(name = "bank_account_number") private String bankAccountNumber;
    @Column(name = "bank_ifsc_code") private String bankIfscCode;
    @Column(name = "gst_registered") private Boolean gstRegistered;
    @Column(name = "gst_number") private String gstNumber;
    @Column(name = "tax_registration_type") private String taxRegistrationType;
    @Column(name = "tax_state") private String taxState;
    @Column(name = "default_tax_rate") private String defaultTaxRate;
    @Column(name = "tax_inclusive") private Boolean taxInclusive;
    @Column(name = "default_unit") private String defaultUnit;
    @Column(name = "unit_system") private String unitSystem;
    @Column(name = "decimal_precision") private Integer decimalPrecision;
    @Column(name = "financial_year_start") private String financialYearStart;
    @Column(name = "timezone") private String timezone;
    @Column(name = "organization_status") private String organizationStatus;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    public OrganizationEntity() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.organizationStatus == null) this.organizationStatus = "ACTIVE";
        if (this.gstRegistered == null) this.gstRegistered = false;
        if (this.taxInclusive == null) this.taxInclusive = false;
        if (this.decimalPrecision == null) this.decimalPrecision = 2;
    }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getOrganizationPrimeId() { return organizationPrimeId; }
    public void setOrganizationPrimeId(Long value) { this.organizationPrimeId = value; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String value) { this.organizationId = value; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String value) { this.companyName = value; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String value) { this.legalName = value; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String value) { this.businessType = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { this.address = value; }
    public String getCity() { return city; }
    public void setCity(String value) { this.city = value; }
    public String getState() { return state; }
    public void setState(String value) { this.state = value; }
    public String getCountry() { return country; }
    public void setCountry(String value) { this.country = value; }
    public String getPincode() { return pincode; }
    public void setPincode(String value) { this.pincode = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    public String getWebsite() { return website; }
    public void setWebsite(String value) { this.website = value; }
    public byte[] getLogoBlob() { return logoBlob; }
    public void setLogoBlob(byte[] value) { this.logoBlob = value; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String value) { this.logoUrl = value; }
    public String getBankName() { return bankName; }
    public void setBankName(String value) { this.bankName = value; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String value) { this.bankAccountNumber = value; }
    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String value) { this.bankIfscCode = value; }
    public Boolean getGstRegistered() { return gstRegistered; }
    public void setGstRegistered(Boolean value) { this.gstRegistered = value; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String value) { this.gstNumber = value; }
    public String getTaxRegistrationType() { return taxRegistrationType; }
    public void setTaxRegistrationType(String value) { this.taxRegistrationType = value; }
    public String getTaxState() { return taxState; }
    public void setTaxState(String value) { this.taxState = value; }
    public String getDefaultTaxRate() { return defaultTaxRate; }
    public void setDefaultTaxRate(String value) { this.defaultTaxRate = value; }
    public Boolean getTaxInclusive() { return taxInclusive; }
    public void setTaxInclusive(Boolean value) { this.taxInclusive = value; }
    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String value) { this.defaultUnit = value; }
    public String getUnitSystem() { return unitSystem; }
    public void setUnitSystem(String value) { this.unitSystem = value; }
    public Integer getDecimalPrecision() { return decimalPrecision; }
    public void setDecimalPrecision(Integer value) { this.decimalPrecision = value; }
    public String getFinancialYearStart() { return financialYearStart; }
    public void setFinancialYearStart(String value) { this.financialYearStart = value; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String value) { this.timezone = value; }
    public String getOrganizationStatus() { return organizationStatus; }
    public void setOrganizationStatus(String value) { this.organizationStatus = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}