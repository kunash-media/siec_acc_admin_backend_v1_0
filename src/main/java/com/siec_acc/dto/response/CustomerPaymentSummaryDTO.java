package com.siec_acc.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerPaymentSummaryDTO {
    private Long clientPrimeId;
    private String clientStrId;
    private String customerName;
    private String customerCompanyName;
    private long totalTransactions;
    private BigDecimal totalPayment;
    private BigDecimal totalAllocated;
    private BigDecimal outstanding;
    private LocalDate lastPaymentDate;

    public CustomerPaymentSummaryDTO() {}
    public Long getClientPrimeId() { return clientPrimeId; }
    public void setClientPrimeId(Long value) { clientPrimeId = value; }
    public String getClientStrId() { return clientStrId; }
    public void setClientStrId(String value) { clientStrId = value; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String value) { customerName = value; }
    public String getCustomerCompanyName() { return customerCompanyName; }
    public void setCustomerCompanyName(String value) { customerCompanyName = value; }
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long value) { totalTransactions = value; }
    public BigDecimal getTotalPayment() { return totalPayment; }
    public void setTotalPayment(BigDecimal value) { totalPayment = value; }
    public BigDecimal getTotalAllocated() { return totalAllocated; }
    public void setTotalAllocated(BigDecimal value) { totalAllocated = value; }
    public BigDecimal getOutstanding() { return outstanding; }
    public void setOutstanding(BigDecimal value) { outstanding = value; }
    public LocalDate getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(LocalDate value) { lastPaymentDate = value; }
}