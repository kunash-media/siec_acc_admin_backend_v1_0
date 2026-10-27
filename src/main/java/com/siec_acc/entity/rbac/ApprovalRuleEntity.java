package com.siec_acc.entity.rbac;

import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "admin_approval_rule",
        uniqueConstraints = @UniqueConstraint(columnNames = {"entity_type", "action"}))
@Builder
public class ApprovalRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_key", nullable = false, length = 50)
    private String moduleKey;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    // DELETE (gate) or CREATE (review flag)
    @Column(name = "action", nullable = false, length = 20)
    private String action;

    // GATE = blocked until approved, REVIEW = executes, then a PENDING review is opened
    @Column(name = "mode", nullable = false, length = 10)
    private String mode;

    // null = applies at any amount
    @Column(name = "min_amount", precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;


    public ApprovalRuleEntity(){}

    public ApprovalRuleEntity(Long id, String moduleKey, String entityType, String action, String mode, BigDecimal minAmount, Boolean active) {
        this.id = id;
        this.moduleKey = moduleKey;
        this.entityType = entityType;
        this.action = action;
        this.mode = mode;
        this.minAmount = minAmount;
        this.active = active;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}