package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "crm_lead_req_category")
public class CrmLeadRequirementCategoryEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requirementCategoryPrimeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_prime_id", nullable = false)
    private CrmLeadEntity lead;

    private String category;

    public CrmLeadRequirementCategoryEntity() {}

    public CrmLeadRequirementCategoryEntity(CrmLeadEntity lead, String category) {
        this.lead = lead;
        this.category = category;
    }
}