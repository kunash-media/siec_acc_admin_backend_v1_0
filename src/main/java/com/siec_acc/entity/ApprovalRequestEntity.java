package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_approval_request", indexes = {
        @Index(name = "idx_appr_status", columnList = "status, requested_at"),
        @Index(name = "idx_appr_entity", columnList = "entity_type, entity_id")
})
@Builder
public class ApprovalRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_key", nullable = false, length = 50)
    private String moduleKey;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 50)
    private String entityId;

    @Column(name = "entity_ref", length = 100)
    private String entityRef;

    // e.g. APPROVE_DOCUMENT, DELETE_REQUEST, PAYMENT_RELEASE
    @Column(name = "request_type", nullable = false, length = 40)
    private String requestType;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 500)
    private String description;

    // PENDING, APPROVED, REJECTED, CANCELLED
    @Builder.Default
    @Column(name = "status", nullable = false, length = 15)
    private String status = "PENDING";

    @Column(name = "requested_by_admin_id", nullable = false, length = 30)
    private String requestedByAdminId;

    @Column(name = "requested_by_name", length = 150)
    private String requestedByName;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "decided_by_admin_id", length = 30)
    private String decidedByAdminId;

    @Column(name = "decided_by_name", length = 150)
    private String decidedByName;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @PrePersist void onCreate() { requestedAt = LocalDateTime.now(); }

    public ApprovalRequestEntity(){}

    public ApprovalRequestEntity(Long id, String moduleKey, String entityType, String entityId, String entityRef, String requestType, BigDecimal amount, String description, String status, String requestedByAdminId, String requestedByName, LocalDateTime requestedAt, String decidedByAdminId, String decidedByName, LocalDateTime decidedAt, String decisionNote) {
        this.id = id;
        this.moduleKey = moduleKey;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityRef = entityRef;
        this.requestType = requestType;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.requestedByAdminId = requestedByAdminId;
        this.requestedByName = requestedByName;
        this.requestedAt = requestedAt;
        this.decidedByAdminId = decidedByAdminId;
        this.decidedByName = decidedByName;
        this.decidedAt = decidedAt;
        this.decisionNote = decisionNote;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityRef() {
        return entityRef;
    }

    public void setEntityRef(String entityRef) {
        this.entityRef = entityRef;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestedByAdminId() {
        return requestedByAdminId;
    }

    public void setRequestedByAdminId(String requestedByAdminId) {
        this.requestedByAdminId = requestedByAdminId;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getDecidedByAdminId() {
        return decidedByAdminId;
    }

    public void setDecidedByAdminId(String decidedByAdminId) {
        this.decidedByAdminId = decidedByAdminId;
    }

    public String getDecidedByName() {
        return decidedByName;
    }

    public void setDecidedByName(String decidedByName) {
        this.decidedByName = decidedByName;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }
}