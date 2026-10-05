package com.siec_acc.dto.request;

import java.math.BigDecimal;
import java.util.List;

public class VendorRequestDto {
    private String vendorName;
    private String companyName;
    private String vendorType;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String stateCode;
    private String pincode;
    private String gstin;
    private String pan;
    private Boolean gstRegistered;
    private String bankName;
    private String bankAccount;
    private String bankIfsc;
    private String paymentTerms;
    private String status;
    private BigDecimal outstanding;
    private List<SecondaryContactDto> secondaryContacts;
    private List<String> documentTypes;

    private Boolean clearDocument;

    public VendorRequestDto() {}

    public VendorRequestDto(String vendorName, String companyName, String vendorType, String email, String phone,
                            String address, String city, String state, String stateCode, String pincode,
                            String gstin, String pan, Boolean gstRegistered, String bankName, String bankAccount,
                            String bankIfsc, String paymentTerms, String status, BigDecimal outstanding,
                            List<SecondaryContactDto> secondaryContacts) {
        this.vendorName = vendorName;
        this.companyName = companyName;
        this.vendorType = vendorType;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.stateCode = stateCode;
        this.pincode = pincode;
        this.gstin = gstin;
        this.pan = pan;
        this.gstRegistered = gstRegistered;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.bankIfsc = bankIfsc;
        this.paymentTerms = paymentTerms;
        this.status = status;
        this.outstanding = outstanding;
        this.secondaryContacts = secondaryContacts;
    }

    public String getVendorName(){
        return vendorName;
    }
    public void setVendorName(String v){
        vendorName=v;
    }
    public String getCompanyName(){
        return companyName;
    }
    public void setCompanyName(String v){
        companyName=v;
    }
    public String getVendorType(){
        return vendorType;
    }
    public void setVendorType(String v){
        vendorType=v;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String v){
        email=v;
    }
    public String getPhone(){
        return phone;
    }
    public void setPhone(String v){
        phone=v;
    }
    public String getAddress(){
        return address;
    }
    public void setAddress(String v){
        address=v;
    }
    public String getCity(){
        return city;
    }
    public void setCity(String v){
        city=v;
    }
    public String getState(){
        return state;
    }
    public void setState(String v){
        state=v;
    }
    public String getStateCode(){
        return stateCode;
    }
    public void setStateCode(String v){
        stateCode=v;
    }
    public String getPincode(){
        return pincode;
    }
    public void setPincode(String v){
        pincode=v;
    }
    public String getGstin(){
        return gstin;
    }
    public void setGstin(String v){
        gstin=v;
    }
    public String getPan(){
        return pan;
    }
    public void setPan(String v){
        pan=v;
    }
    public Boolean getGstRegistered(){
        return gstRegistered;
    }
    public void setGstRegistered(Boolean v){
        gstRegistered=v;
    }
    public String getBankName(){
        return bankName;
    }
    public void setBankName(String v){
        bankName=v;
    }
    public String getBankAccount(){
        return bankAccount;
    }
    public void setBankAccount(String v){
        bankAccount=v;
    }
    public String getBankIfsc(){
        return bankIfsc;
    }
    public void setBankIfsc(String v){
        bankIfsc=v;
    }
    public String getPaymentTerms(){
        return paymentTerms;
    }
    public void setPaymentTerms(String v){
        paymentTerms=v;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String v){
        status=v;
    }
    public BigDecimal getOutstanding(){
        return outstanding;
    }
    public void setOutstanding(BigDecimal v){
        outstanding=v;
    }
    public List<String> getDocumentTypes(){
        return documentTypes;
    }
    public void setDocumentTypes(List<String> v){
        documentTypes=v;
    }

    public List<SecondaryContactDto> getSecondaryContacts(){
        return secondaryContacts;
    }
    public void setSecondaryContacts(List<SecondaryContactDto> v){
        secondaryContacts=v;
    }
    public Boolean getClearDocument(){
        return clearDocument;
    }
    public void setClearDocument(Boolean v){
        clearDocument=v;
    }

    public static class SecondaryContactDto {
        private String name;
        private String designation;
        private String mobile;
        private String email;
        public SecondaryContactDto() {}
        public SecondaryContactDto(String name,String designation,String mobile,String email){
            this.name=name;this.designation=designation;this.mobile=mobile;this.email=email;
        }
        public String getName(){
            return name;
        }
        public void setName(String v){
            name=v;
        }
        public String getDesignation(){
            return designation;
        }
        public void setDesignation(String v){
            designation=v;
        }
        public String getMobile(){
            return mobile;
        }
        public void setMobile(String v){
            mobile=v;
        }
        public String getEmail(){
            return email;
        }
        public void setEmail(String v){
            email=v;
        }
    }
}
