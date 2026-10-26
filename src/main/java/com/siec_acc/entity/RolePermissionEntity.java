package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.Builder;

@Entity
@Table(name = "admin_role_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "module_key"}))
@Builder
public class RolePermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(name = "module_key", nullable = false, length = 50)
    private String moduleKey;

    @Builder.Default
    @Column(name = "can_view",    nullable = false)
    private Boolean canView = false;

    @Builder.Default
    @Column(name = "can_create",  nullable = false)
    private Boolean canCreate = false;

    @Builder.Default
    @Column(name = "can_edit",    nullable = false)
    private Boolean canEdit = false;

    @Builder.Default
    @Column(name = "can_delete",  nullable = false)
    private Boolean canDelete = false;

    @Builder.Default
    @Column(name = "can_approve", nullable = false)
    private Boolean canApprove = false;

    public RolePermissionEntity(){}

    public RolePermissionEntity(Long id, RoleEntity role, String moduleKey, Boolean canView, Boolean canCreate, Boolean canEdit, Boolean canDelete, Boolean canApprove) {
        this.id = id;
        this.role = role;
        this.moduleKey = moduleKey;
        this.canView = canView;
        this.canCreate = canCreate;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canApprove = canApprove;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleEntity getRole() {
        return role;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public Boolean getCanView() {
        return canView;
    }

    public void setCanView(Boolean canView) {
        this.canView = canView;
    }

    public Boolean getCanCreate() {
        return canCreate;
    }

    public void setCanCreate(Boolean canCreate) {
        this.canCreate = canCreate;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }

    public Boolean getCanApprove() {
        return canApprove;
    }

    public void setCanApprove(Boolean canApprove) {
        this.canApprove = canApprove;
    }
}