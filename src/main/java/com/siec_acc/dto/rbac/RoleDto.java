package com.siec_acc.dto.rbac;

import java.util.List;

public record RoleDto(String roleKey, String roleName, String roleDescription, String roleColor,
                      boolean system, long userCount, List<PermissionDto> permissions) {}

