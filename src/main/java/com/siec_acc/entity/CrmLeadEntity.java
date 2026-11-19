package com.siec_acc.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter
@Table(name = "crm_lead", indexes = {
        @Index(name = "idx_lead_phone", columnList = "phone"),
        @Index(name = "idx_lead_follow_up_date", columnList = "follow_up_date")
})
public class CrmLeadEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leadPrimeId;

    @Column(unique = true, nullable = false, updatable = false)
    private String leadStrId;               // LD-1001 (set by CrmLeadService after the first insert)

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String company;
    private String designation;
    private String city;

    private String status;                  // temperature: hot / warm / cold
    private String priority;                // P1 / P2 / P3
    private String source;
    private String referralDetails;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrmLeadRequirementCategoryEntity> requirementCategories = new ArrayList<>();

    private String tags;
    private LocalDate followUpDate;
    private String followupStatus;          // pending / done

    @Column(length = 2000)
    private String notes;

    private Boolean deletedLead = false;
    private Boolean leadConverted = false;
    private Boolean contacted = false;

    private String leadOutcome;             // null = open, "won", "lost"
    private String lostReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private CrmStaffEntity assignedStaff;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        if (leadStrId == null) leadStrId = "TMP-" + UUID.randomUUID();   // replaced right after insert
    }

    @PreUpdate
    void onUpdate() { updatedAt = LocalDateTime.now(); }

    /** UI status: converted > lost > contacted > registered (derived, not stored). */
    public String getPipelineStatus() {
        if (Boolean.TRUE.equals(leadConverted)) return "converted";
        if ("lost".equalsIgnoreCase(leadOutcome)) return "lost";
        if (Boolean.TRUE.equals(contacted)) return "contacted";
        return "registered";
    }
}