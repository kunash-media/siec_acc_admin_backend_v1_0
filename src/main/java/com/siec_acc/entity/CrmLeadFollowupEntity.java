package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "crm_lead_followup")
public class CrmLeadFollowupEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followupPrimeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_prime_id", nullable = false)
    private CrmLeadEntity lead;

    private LocalDate followupDate;
    private String followupStatus;          // pending / done

    @Column(length = 1000)
    private String followupNotes;

    private Boolean deletedFollowup = false;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taken_by_staff_id")
    private CrmStaffEntity takenBy;

    private String meetingType;             // In-office / Online / Phone call / Client visit

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}