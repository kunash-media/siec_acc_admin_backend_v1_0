package com.siec_acc.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Links an existing record (invoice, purchase bill) to a project WITHOUT touching that module's table.
 * linkType: INVOICE | PURCHASE_BILL (expenses use ExpenseEntity.expenseProjectId instead).  A record can belong to only one project.
 */
@Entity
@Table(name = "project_links",
        uniqueConstraints = @UniqueConstraint(name = "uk_project_link_ref", columnNames = {"link_type", "ref_str_id"}),
        indexes = @Index(name = "idx_project_link_project", columnList = "project_str_id"))
public class ProjectLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_prime_id")
    private Long linkPrimeId;

    @Column(name = "project_str_id", nullable = false, length = 30)
    private String projectStrId;

    @Column(name = "link_type", nullable = false, length = 30)
    private String linkType;

    @Column(name = "ref_str_id", nullable = false, length = 40)
    private String refStrId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ProjectLinkEntity() {}

    public Long getLinkPrimeId() {
        return linkPrimeId;
    }

    public void setLinkPrimeId(Long linkPrimeId) {
        this.linkPrimeId = linkPrimeId;
    }

    public String getProjectStrId() {
        return projectStrId;
    }

    public void setProjectStrId(String projectStrId) {
        this.projectStrId = projectStrId;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getRefStrId() {
        return refStrId;
    }

    public void setRefStrId(String refStrId) {
        this.refStrId = refStrId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}