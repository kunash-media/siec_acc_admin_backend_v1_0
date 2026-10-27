package com.siec_acc.service.serviceImpl;

import com.siec_acc.config.BcryptEncoderConfig;
import com.siec_acc.dto.request.AdminRequestDto;
import com.siec_acc.dto.response.AdminResponseDto;
import com.siec_acc.entity.rbac.AdminEntity;
import com.siec_acc.repository.AdminRepository;
import com.siec_acc.repository.RoleRepository;
import com.siec_acc.service.AdminService;
import com.siec_acc.utils.AdminIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final AdminRepository adminRepository;
    private final AdminIdGenerator adminIdGenerator;
    private final BcryptEncoderConfig passwordEncoder;
    private final RoleRepository roleRepository;

    public AdminServiceImpl(AdminRepository adminRepository, AdminIdGenerator adminIdGenerator,
                            BcryptEncoderConfig passwordEncoder, RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        this.adminRepository = adminRepository;
        this.adminIdGenerator = adminIdGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AdminResponseDto createAdmin(AdminRequestDto dto) {
        logger.info("Creating new admin: {} {}", dto.getAdminFirstName(), dto.getAdminLastName());

        if (dto.getAdminFirstName() == null || dto.getAdminFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (dto.getAdminRole() == null || dto.getAdminRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }
        if (dto.getAdminPassword() == null || dto.getAdminPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (dto.getAdminMobileNumber() != null &&
                adminRepository.existsByAdminMobileNumber(dto.getAdminMobileNumber())) {
            throw new IllegalArgumentException("Mobile number already in use");
        }

        String uniqueAdminId = adminIdGenerator.generateUniqueAdminId();

        AdminEntity entity = AdminEntity.builder()
                .adminId(uniqueAdminId)
                .adminFirstName(dto.getAdminFirstName().trim())
                .adminLastName(dto.getAdminLastName() != null ? dto.getAdminLastName().trim() : null)
                .adminMobileNumber(dto.getAdminMobileNumber() != null ? dto.getAdminMobileNumber().trim() : null)
                .adminAddress(dto.getAdminAddress() != null ? dto.getAdminAddress().trim() : null)
                .adminEmail(dto.getAdminEmail() != null ? dto.getAdminEmail().trim() : null)
                .adminRole(dto.getAdminRole().trim())
                .adminPassword(passwordEncoder.encode(dto.getAdminPassword()))
                .adminDepartment(dto.getAdminDepartment())
                .adminIsActive(true)
                .adminIsLocked(false)
                .build();

        AdminEntity saved = adminRepository.save(entity);
        logger.info("Admin created successfully with adminId: {}", saved.getAdminId());

        return mapToResponseDto(saved);
    }

    @Override
    public AdminResponseDto getAdminByAdminId(String adminId) {
        logger.info("Fetching admin with adminId: {}", adminId);
        AdminEntity entity = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));
        return mapToResponseDto(entity);
    }

    @Override
    public List<AdminResponseDto> getAllAdmins() {
        logger.info("Fetching all admins");
        return adminRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminResponseDto updateAdmin(String adminId, AdminRequestDto dto) {
        logger.info("Full update requested for adminId: {}", adminId);
        AdminEntity entity = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        if (dto.getAdminFirstName() != null) entity.setAdminFirstName(dto.getAdminFirstName().trim());
        if (dto.getAdminLastName() != null) entity.setAdminLastName(dto.getAdminLastName().trim());
        if (dto.getAdminMobileNumber() != null) {
            if (!dto.getAdminMobileNumber().equals(entity.getAdminMobileNumber()) &&
                    adminRepository.existsByAdminMobileNumber(dto.getAdminMobileNumber())) {
                throw new IllegalArgumentException("Mobile number already in use");
            }
            entity.setAdminMobileNumber(dto.getAdminMobileNumber().trim());
        }
        if (dto.getAdminAddress() != null) entity.setAdminAddress(dto.getAdminAddress().trim());
        if (dto.getAdminEmail() != null) entity.setAdminEmail(dto.getAdminEmail().trim());  // ✅ Added
        if (dto.getAdminRole() != null) {
            String newRole = normalizeRole(dto.getAdminRole());
            guardRoleChange(entity, newRole);
            entity.setAdminRole(newRole);
        }
        if (dto.getAdminPassword() != null) {
            entity.setAdminPassword(passwordEncoder.encode(dto.getAdminPassword()));
        }
        if (dto.getAdminDepartment() != null) entity.setAdminDepartment(dto.getAdminDepartment().trim());

        AdminEntity saved = adminRepository.save(entity);
        logger.info("Admin fully updated: {}", adminId);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public AdminResponseDto patchAdmin(String adminId, AdminRequestDto dto) {
        logger.info("Partial update (PATCH) requested for adminId: {}", adminId);
        AdminEntity entity = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        boolean changed = false;

        if (dto.getAdminFirstName() != null) {
            entity.setAdminFirstName(dto.getAdminFirstName().trim());
            changed = true;
        }
        if (dto.getAdminLastName() != null) {
            entity.setAdminLastName(dto.getAdminLastName().trim());
            changed = true;
        }
        if (dto.getAdminMobileNumber() != null) {
            if (!dto.getAdminMobileNumber().equals(entity.getAdminMobileNumber()) &&
                    adminRepository.existsByAdminMobileNumber(dto.getAdminMobileNumber())) {
                throw new IllegalArgumentException("Mobile number already in use");
            }
            entity.setAdminMobileNumber(dto.getAdminMobileNumber().trim());
            changed = true;
        }
        if (dto.getAdminAddress() != null) {
            entity.setAdminAddress(dto.getAdminAddress().trim());
            changed = true;
        }
        if (dto.getAdminEmail() != null) {
            entity.setAdminEmail(dto.getAdminEmail().trim());
            changed = true;
        }

        if (dto.getAdminRole() != null) {
            String newRole = normalizeRole(dto.getAdminRole());
            guardRoleChange(entity, newRole);
            entity.setAdminRole(newRole);
            changed = true;
        }

        if (dto.getAdminPassword() != null) {
            entity.setAdminPassword(passwordEncoder.encode(dto.getAdminPassword()));
            changed = true;
        }
        if (dto.getAdminDepartment() != null) {
            entity.setAdminDepartment(dto.getAdminDepartment().trim());
            changed = true;
        }
        if (dto.getAdminIsActive() != null) {
            if (!dto.getAdminIsActive()) {
                if (adminId.equals(actorAdminId())) throw new IllegalArgumentException("You cannot deactivate your own account");
                if (isLastSuperAdmin(entity)) throw new IllegalArgumentException("At least one active SUPER_ADMIN must remain");
            }
            entity.setAdminIsActive(dto.getAdminIsActive());
            changed = true;
        }

        if (!changed) {
            logger.info("No fields changed for PATCH on adminId: {}", adminId);
            return mapToResponseDto(entity);
        }

        AdminEntity saved = adminRepository.save(entity);
        logger.info("Admin partially updated: {}", adminId);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional
    public void deleteAdmin(String adminId) {
        logger.info("Deleting admin with adminId: {}", adminId);
        AdminEntity entity = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        if (adminId.equals(actorAdminId())) throw new IllegalArgumentException("You cannot delete your own account");
        if (isLastSuperAdmin(entity)) throw new IllegalArgumentException("At least one SUPER_ADMIN must remain");

        adminRepository.delete(entity);
        logger.info("Admin deleted successfully: {}", adminId);
    }

    private String normalizeRole(String role) {
        String key = role.trim().toUpperCase();
        if (!roleRepository.existsByRoleKey(key)) throw new IllegalArgumentException("Unknown role: " + role.trim());
        return key;
    }

    private String actorAdminId() {
        org.springframework.security.core.Authentication a =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return (a != null && a.getPrincipal() instanceof com.siec_acc.security.AdminDetails d) ? d.getAdminId() : null;
    }

    private boolean isLastSuperAdmin(AdminEntity e) {
        return "SUPER_ADMIN".equalsIgnoreCase(e.getAdminRole())
                && adminRepository.countByAdminRoleIgnoreCase("SUPER_ADMIN") <= 1;
    }

    private void guardRoleChange(AdminEntity target, String newRole) {
        if (newRole.equalsIgnoreCase(target.getAdminRole())) return;
        if (target.getAdminId().equals(actorAdminId())) throw new IllegalArgumentException("You cannot change your own role");
        if (isLastSuperAdmin(target)) throw new IllegalArgumentException("At least one SUPER_ADMIN must remain");
    }

    private AdminResponseDto mapToResponseDto(AdminEntity entity) {
        return AdminResponseDto.builder()
                .adminId(entity.getAdminId())
                .adminFirstName(entity.getAdminFirstName())
                .adminLastName(entity.getAdminLastName())
                .adminMobileNumber(entity.getAdminMobileNumber())
                .adminAddress(entity.getAdminAddress())
                .adminEmail(entity.getAdminEmail())
                .adminRole(entity.getAdminRole())
                .adminDepartment(entity.getAdminDepartment())
                .adminIsActive(entity.getAdminIsActive())
                .adminIsLocked(entity.getAdminIsLocked())
                .adminLastLoginAt(entity.getAdminLastLoginAt())
                .adminCreatedAt(entity.getAdminCreatedAt())
                .build();
    }
}
