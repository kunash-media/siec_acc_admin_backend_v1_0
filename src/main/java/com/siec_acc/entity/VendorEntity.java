package com.siec_acc.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendor_table")
public class VendorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vendor_prime_id")
    private Long vendorPrimeId;

    @Column(name = "vendor_str_id", unique = true, length = 30)
    private String vendorStrId;

    private String vendorName;
    private String companyName;
    private String vendorType;
    private String gstin;
    private String pan;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String stateCode;
    private String pincode;
    private String paymentTerms;
    private String bankName;
    private String bankAccount;
    private String bankIfsc;
    private Boolean gstRegistered = false;
    private Boolean vendorIsActive = true;
    private BigDecimal outstanding = BigDecimal.ZERO;
    private LocalDate createdAt;

    @ElementCollection
    @CollectionTable(name = "vendor_secondary_contacts", joinColumns = @JoinColumn(name = "vendor_table_id"))
    private List<SecondaryContact> secondaryContacts = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "vendor_table_id", nullable = false)
    private List<VendorDocument> documents = new ArrayList<>();

    public VendorEntity() {}

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDate.now();
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
    public String getVendorType() {
        return vendorType;
    }
    public void setVendorType(String v) {
        vendorType = v;
    }
    public String getGstin() {
        return gstin;
    }
    public void setGstin(String v) {
        gstin = v;
    }
    public String getPan() {
        return pan;
    }
    public void setPan(String v) {
        pan = v;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String v) {
        email = v;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String v) {
        phone = v;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String v) {
        address = v;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String v) {
        city = v;
    }
    public String getState() {
        return state;
    }
    public void setState(String v) {
        state = v;
    }
    public String getStateCode() {
        return stateCode;
    }
    public void setStateCode(String v) {
        stateCode = v;
    }
    public String getPincode() {
        return pincode;
    }
    public void setPincode(String v) {
        pincode = v;
    }
    public String getPaymentTerms() {
        return paymentTerms;
    }
    public void setPaymentTerms(String v) {
        paymentTerms = v;
    }
    public String getBankName() {
        return bankName;
    }
    public void setBankName(String v) {
        bankName = v;
    }
    public String getBankAccount() {
        return bankAccount;
    }
    public void setBankAccount(String v) {
        bankAccount = v;
    }
    public String getBankIfsc() {
        return bankIfsc;
    }
    public void setBankIfsc(String v) {
        bankIfsc = v;
    }
    public Boolean getGstRegistered() {
        return gstRegistered;
    }
    public void setGstRegistered(Boolean v) {
        gstRegistered = v;
    }
    public Boolean getVendorIsActive() {
        return vendorIsActive;
    }
    public void setVendorIsActive(Boolean v) {
        vendorIsActive = v;
    }
    public BigDecimal getOutstanding() {
        return outstanding;
    }
    public void setOutstanding(BigDecimal v) {
        outstanding = v;
    }
    public LocalDate getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDate v) {
        createdAt = v;
    }

    public List<SecondaryContact> getSecondaryContacts() {
        return secondaryContacts;
    }
    public void setSecondaryContacts(List<SecondaryContact> v) {
        secondaryContacts = v;
    }

    public List<VendorDocument> getDocuments() {
        return documents;
    }
    public void setDocuments(List<VendorDocument> v) {
        documents = v;
    }
    @Embeddable
    public static class SecondaryContact {
        @Column(name = "contact_name")
        private String name;
        @Column(name = "contact_designation")
        private String designation;
        @Column(name = "contact_mobile")
        private String mobile;
        @Column(name = "contact_email")
        private String email;

        public SecondaryContact() {}
        public SecondaryContact(String name, String designation, String mobile, String email) {
            this.name = name;
            this.designation = designation;
            this.mobile = mobile;
            this.email = email;
        }
        public String getName() {
            return name;
        }
        public void setName(String v) {
            name = v;
        }
        public String getDesignation() {
            return designation;
        }
        public void setDesignation(String v) {
            designation = v;
        }
        public String getMobile() {
            return mobile;
        }
        public void setMobile(String v) {
            mobile = v;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String v) {
            email = v;
        }
    }

    @Entity
    @Table(name = "vendor_document_table")
    public static class VendorDocument {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long id;

        @Column(name = "document_id", nullable = false)
        private Long documentId;

        @Column(name = "document_type", length = 100)
        private String documentType;

        @Column(name = "document_url", length = 500)
        private String documentUrl;

        @Column(name = "file_name", length = 255)
        private String fileName;

        @Column(name = "content_type", length = 150)
        private String contentType;

        @Column(name = "file_size")
        private Long fileSize;

        @Lob
        @Column(name = "document_data", columnDefinition = "LONGBLOB")
        private byte[] documentData;

        public VendorDocument() {}

        public VendorDocument(Long documentId, String documentType, String documentUrl, String fileName,
                              String contentType, Long fileSize, byte[] documentData) {
            this.documentId = documentId;
            this.documentType = documentType;
            this.documentUrl = documentUrl;
            this.fileName = fileName;
            this.contentType = contentType;
            this.fileSize = fileSize;
            this.documentData = documentData;
        }

        public Long getId() { return id; }
        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long v) { documentId = v; }
        public String getDocumentType() { return documentType; }
        public void setDocumentType(String v) { documentType = v; }
        public String getDocumentUrl() { return documentUrl; }
        public void setDocumentUrl(String v) { documentUrl = v; }
        public String getFileName() { return fileName; }
        public void setFileName(String v) { fileName = v; }
        public String getContentType() { return contentType; }
        public void setContentType(String v) { contentType = v; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long v) { fileSize = v; }
        public byte[] getDocumentData() { return documentData; }
        public void setDocumentData(byte[] v) { documentData = v; }
    }
}