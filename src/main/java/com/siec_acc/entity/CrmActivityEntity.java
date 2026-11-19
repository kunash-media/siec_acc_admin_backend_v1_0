package com.siec_acc.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Append-only audit trail shown in the "Activities Log" tab. */
@Entity
@Table(name = "crm_activity", indexes = {
        @Index(name = "idx_act_lead", columnList = "lead_str_id"),
        @Index(name = "idx_act_created", columnList = "created_at")
})
public class CrmActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    private String leadStrId;
    private String company;
    private String type;      // Lead / Deal
    private String refNo;     // lead no or deal no
    private String action;    // Registered, Updated, Contacted, Follow-up, Converted, Stage Changed, Closed Won, Closed Lost, Lead Lost

    @Column(length = 1000)
    private String details;

    @PrePersist
    protected void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getLeadStrId() { return leadStrId; }
    public void setLeadStrId(String v) { this.leadStrId = v; }
    public String getCompany() { return company; }
    public void setCompany(String v) { this.company = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getRefNo() { return refNo; }
    public void setRefNo(String v) { this.refNo = v; }
    public String getAction() { return action; }
    public void setAction(String v) { this.action = v; }
    public String getDetails() { return details; }
    public void setDetails(String v) { this.details = v; }
}