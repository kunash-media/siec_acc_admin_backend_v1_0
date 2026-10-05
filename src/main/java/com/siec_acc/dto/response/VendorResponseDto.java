package com.siec_acc.dto.response;

import com.siec_acc.dto.request.VendorRequestDto.SecondaryContactDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;

public class VendorResponseDto {
    private Long vendorPrimeId;
    private String vendorStrId;
    private String  vendorId;
    private String  vendorName;
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
    private LocalDate createdAt;
    private List<SecondaryContactDto> secondaryContacts;
    private List<DocumentDto> documents;

    public VendorResponseDto() {}
    public Long getVendorPrimeId(){
        return vendorPrimeId;
    }
    public void setVendorPrimeId(Long v){
        vendorPrimeId=v;
    }
    public String getVendorStrId(){
        return vendorStrId;
    }
    public void setVendorStrId(String v){
        vendorStrId=v;
    }
    public String getVendorId(){
        return vendorId;
    }
    public void setVendorId(String v){
        vendorId=v;
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
    public LocalDate getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(LocalDate v){
        createdAt=v;
    }
    public List<SecondaryContactDto> getSecondaryContacts(){
        return secondaryContacts;
    }
    public void setSecondaryContacts(List<SecondaryContactDto> v){
        secondaryContacts=v;
    }
    public List<DocumentDto> getDocuments(){
        return documents;
    }
    public void setDocuments(List<DocumentDto> v){
        documents=v;
    }

    public static class DocumentDto {
        private Long documentId;
        private String documentType;
        private String documentUrl;
        private Long fileSize;

        public DocumentDto() {}
        public DocumentDto(Long documentId, String documentType, String documentUrl, Long fileSize) {
            this.documentId = documentId;
            this.documentType = documentType;
            this.documentUrl = documentUrl;
            this.fileSize = fileSize;
        }
        public Long getDocumentId(){
            return documentId;
        }
        public void setDocumentId(Long v){
            documentId=v;
        }
        public String getDocumentType(){
            return documentType;
        }
        public void setDocumentType(String v){
            documentType=v;
        }
        public String getDocumentUrl(){
            return documentUrl;
        }
        public void setDocumentUrl(String v){
            documentUrl=v;
        }
        public Long getFileSize(){
            return fileSize;
        }
        public void setFileSize(Long v){
            fileSize=v;
        }
    }

    public static class PagedResponseDto<T> {
        private List<T> content;
        private int pageNumber;
        private int pageSize;
        private int totalPages;
        private long totalElements;
        private boolean first;
        private boolean last;
        public PagedResponseDto(){}
        public PagedResponseDto(List<T> content,int pageNumber,int pageSize,long totalElements,int totalPages,boolean first,boolean last){
            this.content=content;
            this.pageNumber=pageNumber;
            this.pageSize=pageSize;
            this.totalElements=totalElements;
            this.totalPages=totalPages;
            this.first=first;
            this.last=last;
        }
        public static <T> PagedResponseDto<T> from(Page<T> p){
            return new PagedResponseDto<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages(),p.isFirst(),p.isLast());
        }
        public List<T> getContent() {
            return content;
        }
        public void setContent(List<T> v){
            content=v;
        }
        public int getPageNumber(){
            return pageNumber;
        }
        public void setPageNumber(int v){
            pageNumber=v;
        }
        public int getPageSize(){
            return pageSize;
        }
        public void setPageSize(int v){
            pageSize=v;
        }
        public long getTotalElements(){
            return totalElements;
        }
        public void setTotalElements(long v){
            totalElements=v;
        }
        public int getTotalPages(){
            return totalPages;
        }
        public void setTotalPages(int v){
            totalPages=v;
        }
        public boolean isFirst(){
            return first;
        }
        public void setFirst(boolean v){
            first=v;
        }
        public boolean isLast(){
            return last;
        }
        public void setLast(boolean v){
            last=v;
        }
    }
}
