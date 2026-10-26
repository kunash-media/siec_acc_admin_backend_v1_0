package com.siec_acc.dto.rbac;

import java.util.List;

public record RoleRequestDto(String roleKey, String roleName, String roleDescription, String roleColor,
                             List<PermissionDto> permissions) {}