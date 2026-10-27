package com.siec_acc.config;

import com.siec_acc.entity.rbac.RoleEntity;
import com.siec_acc.entity.rbac.RolePermissionEntity;
import com.siec_acc.enum_status.AppModule;
import com.siec_acc.repository.AdminRepository;
import com.siec_acc.repository.RoleRepository;
import com.siec_acc.service.rbac.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RbacSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RbacSeeder.class);

    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;

    public RbacSeeder(RoleRepository roleRepository, AdminRepository adminRepository) {
        this.roleRepository = roleRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureRole(PermissionService.SUPER_ADMIN, "Super Admin", "Full system access", "#6da84c", true);

        for (String raw : adminRepository.findDistinctRoles()) {
            String key = raw == null ? "" : raw.trim().toUpperCase();
            if (key.isEmpty()) continue;
            ensureRole(key, toTitle(key), "Migrated from existing admins", "#1985bf", false);
        }
    }

    private void ensureRole(String key, String name, String desc, String color, boolean system) {
        if (roleRepository.existsByRoleKey(key)) return;

        RoleEntity role = RoleEntity.builder()
                .roleKey(key).roleName(name).roleDescription(desc).roleColor(color).isSystem(system)
                .build();

        // Migrated roles keep today's access (everything except Users); SUPER_ADMIN bypasses checks anyway
        for (AppModule m : AppModule.values()) {
            boolean open = !system && m != AppModule.USERS;
            role.getPermissions().add(RolePermissionEntity.builder()
                    .role(role).moduleKey(m.getKey())
                    .canView(open).canCreate(open).canEdit(open).canDelete(open).canApprove(open)
                    .build());
        }
        roleRepository.save(role);
        logger.info("[rbac] seeded role {}", key);
    }

    private static String toTitle(String key) {
        StringBuilder sb = new StringBuilder();
        for (String w : key.toLowerCase().split("_")) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }
}