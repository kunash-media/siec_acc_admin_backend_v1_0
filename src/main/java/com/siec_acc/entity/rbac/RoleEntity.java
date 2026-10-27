package com.siec_acc.entity.rbac;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admin_roles")
@Builder
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Value stored in admin_table.admin_role, e.g. SUPER_ADMIN, SALES_USER
    @Column(name = "role_key", nullable = false, unique = true, length = 50)
    private String roleKey;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "role_description", length = 255)
    private String roleDescription;

    @Column(name = "role_color", length = 10)
    private String roleColor;

    // true = seeded role (SUPER_ADMIN), cannot be deleted or renamed
    @Builder.Default
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Builder.Default
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RolePermissionEntity> permissions = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void onUpdate() { updatedAt = LocalDateTime.now(); }

    public RoleEntity(){}

    public RoleEntity(Long id, String roleKey, String roleName, String roleDescription, String roleColor, Boolean isSystem, List<RolePermissionEntity> permissions, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.roleKey = roleKey;
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.roleColor = roleColor;
        this.isSystem = isSystem;
        this.permissions = permissions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public String getRoleColor() {
        return roleColor;
    }

    public void setRoleColor(String roleColor) {
        this.roleColor = roleColor;
    }

    public Boolean getSystem() {
        return isSystem;
    }

    public void setSystem(Boolean system) {
        isSystem = system;
    }

    public List<RolePermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<RolePermissionEntity> permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


}