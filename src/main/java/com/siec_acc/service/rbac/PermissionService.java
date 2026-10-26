package com.siec_acc.service.rbac;

import com.siec_acc.entity.RoleEntity;
import com.siec_acc.entity.RolePermissionEntity;
import com.siec_acc.enum_status.AppModule;
import com.siec_acc.enum_status.PermissionAction;
import com.siec_acc.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PermissionService {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final RoleRepository roleRepository;
    private final Map<String, Map<String, EnumSet<PermissionAction>>> cache = new ConcurrentHashMap<>();

    public PermissionService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public boolean isAllowed(String roleKey, AppModule module, PermissionAction action) {
        if (roleKey == null || roleKey.isBlank()) return false;
        String key = roleKey.trim().toUpperCase();
        if (SUPER_ADMIN.equals(key)) return true;

        Map<String, EnumSet<PermissionAction>> perms = cache.get(key);
        if (perms == null) {
            perms = load(key);
            if (perms == null) return false;      // unknown role: never cached, picked up once created
            cache.put(key, perms);
        }
        return perms.getOrDefault(module.getKey(), EnumSet.noneOf(PermissionAction.class)).contains(action);
    }

    // Call after any role / permission save or delete
    public void evict(String roleKey) {
        if (roleKey != null) cache.remove(roleKey.trim().toUpperCase());
    }

    public void evictAll() { cache.clear(); }

    private Map<String, EnumSet<PermissionAction>> load(String roleKey) {
        RoleEntity role = roleRepository.findWithPermissionsByRoleKey(roleKey).orElse(null);
        if (role == null) return null;

        Map<String, EnumSet<PermissionAction>> map = new HashMap<>();
        for (RolePermissionEntity p : role.getPermissions()) {
            EnumSet<PermissionAction> s = EnumSet.noneOf(PermissionAction.class);
            if (Boolean.TRUE.equals(p.getCanView()))    s.add(PermissionAction.VIEW);
            if (Boolean.TRUE.equals(p.getCanCreate()))  s.add(PermissionAction.CREATE);
            if (Boolean.TRUE.equals(p.getCanEdit()))    s.add(PermissionAction.EDIT);
            if (Boolean.TRUE.equals(p.getCanDelete()))  s.add(PermissionAction.DELETE);
            if (Boolean.TRUE.equals(p.getCanApprove())) s.add(PermissionAction.APPROVE);
            map.put(p.getModuleKey(), s);
        }
        return map;
    }

    private EnumSet<PermissionAction> actionsFor(String roleKey, AppModule module) {
        if (roleKey == null || roleKey.isBlank()) return EnumSet.noneOf(PermissionAction.class);
        String key = roleKey.trim().toUpperCase();
        Map<String, EnumSet<PermissionAction>> perms = cache.get(key);
        if (perms == null) {
            perms = load(key);
            if (perms == null) return EnumSet.noneOf(PermissionAction.class);
            cache.put(key, perms);
        }
        return perms.getOrDefault(module.getKey(), EnumSet.noneOf(PermissionAction.class));
    }

    // Used by /me: moduleKey -> allowed action names. SUPER_ADMIN gets everything.
    public Map<String, List<String>> permissionMap(String roleKey) {
        boolean superAdmin = roleKey != null && SUPER_ADMIN.equalsIgnoreCase(roleKey.trim());
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (AppModule m : AppModule.values()) {
            EnumSet<PermissionAction> s = superAdmin ? EnumSet.allOf(PermissionAction.class) : actionsFor(roleKey, m);
            out.put(m.getKey(), s.stream().map(Enum::name).toList());
        }
        return out;
    }
}