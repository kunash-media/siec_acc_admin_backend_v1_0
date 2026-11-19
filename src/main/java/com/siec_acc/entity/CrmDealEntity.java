package com.siec_acc.entity;


import com.siec_acc.enum_status.CrmDealStage;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_deal", indexes = {
        @Index(name = "idx_deal_stage", columnList = "stage"),
        @Index(name = "idx_deal_lead", columnList = "lead_prime_id")
})
public class CrmDealEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dealPrimeId;

    @Column(unique = true, nullable = false)
    private String dealNumber;          // DL-1001 ...

    private Long leadPrimeId;
    private String leadStrId;

    @Column(nullable = false)
    private String title;
    private String company;
    private String contact;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CrmDealStage stage = CrmDealStage.QUALIFICATION;

    private LocalDate expectedClose;
    private LocalDate closedAt;

    @Column(length = 2000)
    private String notes;
    private String lostReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getDealPrimeId() { return dealPrimeId; }
    public String getDealNumber() { return dealNumber; }
    public void setDealNumber(String v) { this.dealNumber = v; }
    public Long getLeadPrimeId() { return leadPrimeId; }
    public void setLeadPrimeId(Long v) { this.leadPrimeId = v; }
    public String getLeadStrId() { return leadStrId; }
    public void setLeadStrId(String v) { this.leadStrId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getCompany() { return company; }
    public void setCompany(String v) { this.company = v; }
    public String getContact() { return contact; }
    public void setContact(String v) { this.contact = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
    public CrmDealStage getStage() { return stage; }
    public void setStage(CrmDealStage v) { this.stage = v; }
    public LocalDate getExpectedClose() { return expectedClose; }
    public void setExpectedClose(LocalDate v) { this.expectedClose = v; }
    public LocalDate getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDate v) { this.closedAt = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
    public String getLostReason() { return lostReason; }
    public void setLostReason(String v) { this.lostReason = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}