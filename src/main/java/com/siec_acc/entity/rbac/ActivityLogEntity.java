package com.siec_acc.entity.rbac;

import jakarta.persistence.*;
import lombok.Builder;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "admin_activity_log", indexes = {
        @Index(name = "idx_log_admin_time",  columnList = "admin_id, created_at"),
        @Index(name = "idx_log_module_time", columnList = "module_key, created_at"),
        @Index(name = "idx_log_entity",      columnList = "entity_type, entity_id")
})
@Builder
public class ActivityLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plain business id (ADM3662), deliberately NOT a FK
    @Column(name = "admin_id", length = 30)
    private String adminId;

    @Column(name = "admin_name_snapshot", length = 150)
    private String adminNameSnapshot;

    @Column(name = "role_snapshot", length = 50)
    private String roleSnapshot;

    @Column(name = "module_key", length = 50)
    private String moduleKey;

    // CREATE, UPDATE, DELETE, APPROVE, REJECT, LOGIN, LOGOUT, DENIED, WOULD_DENY
    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 50)
    private String entityId;

    // Printed number, e.g. INV-25-26-004
    @Column(name = "entity_ref", length = 100)
    private String entityRef;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "path", length = 300)
    private String path;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "ip_address", length = 60)
    private String ipAddress;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }


    public ActivityLogEntity(){}

    public ActivityLogEntity(Long id, String adminId, String adminNameSnapshot, String roleSnapshot, String moduleKey, String action, String entityType, String entityId, String entityRef, String summary, String httpMethod, String path, Integer statusCode, String ipAddress, String userAgent, LocalDateTime createdAt) {
        this.id = id;
        this.adminId = adminId;
        this.adminNameSnapshot = adminNameSnapshot;
        this.roleSnapshot = roleSnapshot;
        this.moduleKey = moduleKey;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityRef = entityRef;
        this.summary = summary;
        this.httpMethod = httpMethod;
        this.path = path;
        this.statusCode = statusCode;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminNameSnapshot() {
        return adminNameSnapshot;
    }

    public void setAdminNameSnapshot(String adminNameSnapshot) {
        this.adminNameSnapshot = adminNameSnapshot;
    }

    public String getRoleSnapshot() {
        return roleSnapshot;
    }

    public void setRoleSnapshot(String roleSnapshot) {
        this.roleSnapshot = roleSnapshot;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}