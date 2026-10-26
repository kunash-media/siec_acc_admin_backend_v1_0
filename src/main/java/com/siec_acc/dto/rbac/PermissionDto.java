package com.siec_acc.dto.rbac;

public record PermissionDto(String moduleKey, boolean view, boolean create, boolean edit, boolean delete, boolean approve) {}
