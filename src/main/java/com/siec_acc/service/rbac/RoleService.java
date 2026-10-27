package com.siec_acc.service.rbac;

import com.siec_acc.dto.rbac.ModuleInfoDto;
import com.siec_acc.dto.rbac.PermissionDto;
import com.siec_acc.dto.rbac.RoleDto;
import com.siec_acc.dto.rbac.RoleRequestDto;
import com.siec_acc.entity.rbac.RoleEntity;
import com.siec_acc.entity.rbac.RolePermissionEntity;
import com.siec_acc.enum_status.AppModule;
import com.siec_acc.repository.AdminRepository;
import com.siec_acc.repository.RoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,49}$");
    private static final Set<String> VALID_MODULES = Arrays.stream(AppModule.values())
            .map(AppModule::getKey).collect(Collectors.toSet());

    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final PermissionService permissionService;

    public RoleService(RoleRepository roleRepository, AdminRepository adminRepository,
                       PermissionService permissionService) {
        this.roleRepository = roleRepository;
        this.adminRepository = adminRepository;
        this.permissionService = permissionService;
    }

    @Transactional(readOnly = true)
    public List<RoleDto> list() {
        return roleRepository.findAll(Sort.by("id")).stream().map(r -> toDto(r, false)).toList();
    }

    @Transactional(readOnly = true)
    public List<ModuleInfoDto> modules() {
        return Arrays.stream(AppModule.values())
                .map(m -> new ModuleInfoDto(m.getKey(), label(m.getKey()))).toList();
    }

    @Transactional(readOnly = true)
    public RoleDto get(String roleKey) {
        return toDto(find(roleKey), true);
    }

    @Transactional
    public RoleDto create(RoleRequestDto req) {
        if (req == null || isBlank(req.roleName())) throw bad("Role name is required");

        String key = !isBlank(req.roleKey())
                ? req.roleKey().trim().toUpperCase()
                : req.roleName().trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (!KEY_PATTERN.matcher(key).matches())
            throw bad("Role key must be 2-50 characters: letters, digits and underscore, starting with a letter");
        if (roleRepository.existsByRoleKey(key))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role already exists: " + key);

        RoleEntity role = RoleEntity.builder()
                .roleKey(key)
                .roleName(req.roleName().trim())
                .roleDescription(trimOrNull(req.roleDescription()))
                .roleColor(isBlank(req.roleColor()) ? "#6da84c" : req.roleColor().trim())
                .isSystem(false)
                .build();
        applyPermissions(role, req.permissions());

        RoleEntity saved = roleRepository.save(role);
        evictAfterCommit(key);
        return toDto(saved, true);
    }

    @Transactional
    public RoleDto update(String roleKey, RoleRequestDto req) {
        RoleEntity role = find(roleKey);
        if (Boolean.TRUE.equals(role.getSystem()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "System roles cannot be modified");
        if (req == null || isBlank(req.roleName())) throw bad("Role name is required");

        role.setRoleName(req.roleName().trim());
        role.setRoleDescription(trimOrNull(req.roleDescription()));
        if (!isBlank(req.roleColor())) role.setRoleColor(req.roleColor().trim());
        applyPermissions(role, req.permissions());

        RoleEntity saved = roleRepository.save(role);
        evictAfterCommit(role.getRoleKey());
        return toDto(saved, true);
    }

    @Transactional
    public void delete(String roleKey) {
        RoleEntity role = find(roleKey);
        if (Boolean.TRUE.equals(role.getSystem()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "System roles cannot be deleted");
        long users = adminRepository.countByAdminRoleIgnoreCase(role.getRoleKey());
        if (users > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Role is assigned to " + users + " user(s). Reassign them first.");
        roleRepository.delete(role);
        evictAfterCommit(role.getRoleKey());
    }

    // ---- helpers ----

    private RoleEntity find(String roleKey) {
        return roleRepository.findWithPermissionsByRoleKey(roleKey == null ? "" : roleKey.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleKey));
    }

    // Updates rows in place (never delete + re-insert) so the (role_id, module_key) unique key is never violated
    private void applyPermissions(RoleEntity role, List<PermissionDto> incomingList) {
        Map<String, PermissionDto> incoming = new HashMap<>();
        if (incomingList != null) {
            for (PermissionDto d : incomingList) {
                if (d == null || !VALID_MODULES.contains(d.moduleKey()))
                    throw bad("Unknown module: " + (d == null ? null : d.moduleKey()));
                incoming.put(d.moduleKey(), d);
            }
        }
        Map<String, RolePermissionEntity> existing = new HashMap<>();
        for (RolePermissionEntity p : role.getPermissions()) existing.put(p.getModuleKey(), p);

        for (AppModule m : AppModule.values()) {
            RolePermissionEntity row = existing.get(m.getKey());
            if (row == null) {
                row = RolePermissionEntity.builder().role(role).moduleKey(m.getKey()).build();
                role.getPermissions().add(row);
            }
            PermissionDto d = incoming.get(m.getKey());
            if (d != null) {
                row.setCanView(d.view());
                row.setCanCreate(d.create());
                row.setCanEdit(d.edit());
                row.setCanDelete(d.delete());
                row.setCanApprove(d.approve());
            }
        }
    }

    private RoleDto toDto(RoleEntity r, boolean withPermissions) {
        List<PermissionDto> perms = List.of();
        if (withPermissions) {
            Map<String, RolePermissionEntity> byKey = new HashMap<>();
            for (RolePermissionEntity p : r.getPermissions()) byKey.put(p.getModuleKey(), p);
            perms = Arrays.stream(AppModule.values()).map(m -> {
                RolePermissionEntity p = byKey.get(m.getKey());
                return p == null
                        ? new PermissionDto(m.getKey(), false, false, false, false, false)
                        : new PermissionDto(m.getKey(), Boolean.TRUE.equals(p.getCanView()), Boolean.TRUE.equals(p.getCanCreate()),
                        Boolean.TRUE.equals(p.getCanEdit()), Boolean.TRUE.equals(p.getCanDelete()), Boolean.TRUE.equals(p.getCanApprove()));
            }).toList();
        }
        return new RoleDto(r.getRoleKey(), r.getRoleName(), r.getRoleDescription(), r.getRoleColor(),
                Boolean.TRUE.equals(r.getSystem()), adminRepository.countByAdminRoleIgnoreCase(r.getRoleKey()), perms);
    }

    private void evictAfterCommit(String key) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { permissionService.evict(key); }
            });
        } else {
            permissionService.evict(key);
        }
    }

    private static String label(String key) {
        StringBuilder sb = new StringBuilder();
        for (String w : key.split("-")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }

    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String trimOrNull(String s) { return isBlank(s) ? null : s.trim(); }
}