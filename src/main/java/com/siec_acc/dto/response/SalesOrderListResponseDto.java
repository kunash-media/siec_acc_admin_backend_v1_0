package com.siec_acc.dto.response;

public class SalesOrderListResponseDto {

    private String id;
    private String clientId;
    private String expectedDelivery;
    private String status;
    private String number;

    public SalesOrderListResponseDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getExpectedDelivery() { return expectedDelivery; }
    public void setExpectedDelivery(String expectedDelivery) { this.expectedDelivery = expectedDelivery; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
}