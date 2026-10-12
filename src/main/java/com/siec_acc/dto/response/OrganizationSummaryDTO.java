package com.siec_acc.dto.response;

public class OrganizationSummaryDTO {
    private String organizationId;
    private String companyName;
    private String legalName;
    private String logoUrl;
    private String address;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String phone;
    private String email;
    private String website;
    private Boolean gstRegistered;
    private String gstNumber;
    private String bankName;
    private String bankAccountNumber;
    private String bankIfscCode;

    public OrganizationSummaryDTO() {}
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String v) { organizationId = v; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String v) { companyName = v; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String v) { legalName = v; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String v) { logoUrl = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }
    public String getCity() { return city; }
    public void setCity(String v) { city = v; }
    public String getState() { return state; }
    public void setState(String v) { state = v; }
    public String getCountry() { return country; }
    public void setCountry(String v) { country = v; }
    public String getPincode() { return pincode; }
    public void setPincode(String v) { pincode = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getWebsite() { return website; }
    public void setWebsite(String v) { website = v; }
    public Boolean getGstRegistered() { return gstRegistered; }
    public void setGstRegistered(Boolean v) { gstRegistered = v; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String v) { gstNumber = v; }
    public String getBankName() { return bankName; }
    public void setBankName(String v) { bankName = v; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String v) { bankAccountNumber = v; }
    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String v) { bankIfscCode = v; }
}