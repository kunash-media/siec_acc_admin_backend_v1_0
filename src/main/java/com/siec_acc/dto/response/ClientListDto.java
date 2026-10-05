package com.siec_acc.dto.response;

public class ClientListDto {

    private Long clientPrimeId;
    private String clientStrId;
    private String clientName;
    private String clientCompanyName;
    private String clientCustomerType;
    private String clientEmail;
    private String clientPhone;
    private String clientCountry;

    public ClientListDto() {
    }

    public ClientListDto(Long clientPrimeId, String clientStrId, String clientName,
                         String clientCompanyName, String clientCustomerType,
                         String clientEmail, String clientPhone, String clientCountry) {
        this.clientPrimeId = clientPrimeId;
        this.clientStrId = clientStrId;
        this.clientName = clientName;
        this.clientCompanyName = clientCompanyName;
        this.clientCustomerType = clientCustomerType;
        this.clientEmail = clientEmail;
        this.clientPhone = clientPhone;
        this.clientCountry = clientCountry;
    }

    public Long getClientPrimeId() { return clientPrimeId; }
    public void setClientPrimeId(Long clientPrimeId) { this.clientPrimeId = clientPrimeId; }

    public String getClientStrId() { return clientStrId; }
    public void setClientStrId(String clientStrId) { this.clientStrId = clientStrId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientCompanyName() { return clientCompanyName; }
    public void setClientCompanyName(String clientCompanyName) { this.clientCompanyName = clientCompanyName; }

    public String getClientCustomerType() { return clientCustomerType; }
    public void setClientCustomerType(String clientCustomerType) { this.clientCustomerType = clientCustomerType; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getClientCountry() {
        return clientCountry;
    }

    public void setClientCountry(String clientCountry) {
        this.clientCountry = clientCountry;
    }

}