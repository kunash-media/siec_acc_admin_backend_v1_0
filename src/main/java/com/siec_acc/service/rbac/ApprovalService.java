package com.siec_acc.service.rbac;

import com.siec_acc.dto.rbac.ApprovalDto;
import com.siec_acc.dto.rbac.RuleDto;
import com.siec_acc.dto.rbac.RuleUpdateDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.entity.rbac.ActivityLogEntity;
import com.siec_acc.entity.rbac.AdminEntity;
import com.siec_acc.entity.rbac.ApprovalRequestEntity;
import com.siec_acc.entity.rbac.ApprovalRuleEntity;
import com.siec_acc.enum_status.AppModule;
import com.siec_acc.enum_status.PermissionAction;
import com.siec_acc.repository.ActivityLogRepository;
import com.siec_acc.repository.ApprovalRequestRepository;
import com.siec_acc.repository.ApprovalRuleRepository;
import com.siec_acc.security.AdminDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApprovalService {

    private final ApprovalRequestRepository requests;
    private final ApprovalRuleRepository rules;
    private final ActivityLogRepository logs;
    private final PermissionService permissionService;
    private final ApprovalExecutor executor;
    private final ActivityLogService activityLogService;
    private final boolean enabled;
    private final boolean approverBypass;

    public ApprovalService(ApprovalRequestRepository requests, ApprovalRuleRepository rules,
                           ActivityLogRepository logs, PermissionService permissionService,
                           ApprovalExecutor executor, ActivityLogService activityLogService,
                           @Value("${app.approval.enabled:false}") boolean enabled,
                           @Value("${app.approval.approver-bypass:true}") boolean approverBypass) {
        this.requests = requests;
        this.rules = rules;
        this.logs = logs;
        this.permissionService = permissionService;
        this.executor = executor;
        this.activityLogService = activityLogService;
        this.enabled = enabled;
        this.approverBypass = approverBypass;
    }

    // ---- gate decision (called by the interceptor) ----

    public boolean shouldGate(AdminDetails admin, AppModule.Match match) {
        if (!enabled) return false;
        if (approverBypass && permissionService.isAllowed(admin.getRole(), match.module(), PermissionAction.APPROVE)) return false;
        return rules.existsByEntityTypeAndActionAndModeAndActiveTrue(match.entityType(), "DELETE", "GATE");
    }

    @Transactional
    public ApprovalRequestEntity submitDeleteRequest(AdminDetails admin, AppModule.Match match, String entityId) {
        // Same record, same pending request: a repeated click never creates duplicates
        return requests.findFirstByEntityTypeAndEntityIdAndStatus(match.entityType(), entityId, "PENDING")
                .orElseGet(() -> {
                    String ref = logs.findTopByEntityTypeAndEntityIdAndEntityRefIsNotNullOrderByIdDesc(match.entityType(), entityId)
                            .map(ActivityLogEntity::getEntityRef).orElse(null);
                    AdminEntity a = admin.getAdminEntity();
                    return requests.save(ApprovalRequestEntity.builder()
                            .moduleKey(match.module().getKey())
                            .entityType(match.entityType())
                            .entityId(entityId)
                            .entityRef(ref)
                            .requestType("DELETE_REQUEST")
                            .description("Delete " + label(match.entityType()) + " " + (ref != null ? ref : "#" + entityId))
                            .requestedByAdminId(a.getAdminId())
                            .requestedByName(fullName(a))
                            .build());
                });
    }

    // ---- read ----

    @Transactional(readOnly = true)
    public PagedResponseDto<ApprovalDto> list(AdminDetails admin, String status, int pageNumber, int pageSize) {
        List<String> mods = new ArrayList<>();
        for (AppModule m : AppModule.values())
            if (permissionService.isAllowed(admin.getRole(), m, PermissionAction.APPROVE)) mods.add(m.getKey());
        if (mods.isEmpty()) mods.add("-");

        int size = pageSize <= 0 ? 25 : Math.min(pageSize, 100);
        String st = (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) ? null : status.trim().toUpperCase();
        Page<ApprovalRequestEntity> page = requests.visible(st, admin.getAdminId(), mods,
                PageRequest.of(Math.max(pageNumber, 1) - 1, size, Sort.by(Sort.Direction.DESC, "requestedAt")));

        return PagedResponseDto.<ApprovalDto>builder()
                .content(page.getContent().stream().map(r -> toDto(admin, r)).toList())
                .pageNumber(pageNumber)
                .pageSize(size)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    // ---- decisions ----

    @Transactional
    public ApprovalDto approve(AdminDetails admin, Long id, String note, HttpServletRequest http) {
        ApprovalRequestEntity r = pending(id);
        requireApprover(admin, r);
        if (r.getRequestedByAdminId().equals(admin.getAdminId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot approve your own request");

        try {
            executor.delete(r.getEntityType(), r.getEntityId());
        } catch (RuntimeException e) {
            // whole transaction rolls back: the request stays PENDING, the approver sees why
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Could not delete the record: " + (e.getMessage() == null ? "unknown error" : e.getMessage()));
        }

        stamp(r, admin, "APPROVED", note);
        ApprovalRequestEntity saved = requests.save(r);

        activityLogService.record(admin, http, r.getModuleKey(), "DELETE", r.getEntityType(), r.getEntityId(), r.getEntityRef(),
                "Deleted " + label(r.getEntityType()) + " " + nz(r.getEntityRef())
                        + " (approved request #" + r.getId() + ", requested by " + r.getRequestedByName() + ")", 200);
        activityLogService.record(admin, http, r.getModuleKey(), "APPROVE", r.getEntityType(), r.getEntityId(), r.getEntityRef(),
                "Approved request #" + r.getId(), 200);
        return toDto(admin, saved);
    }

    @Transactional
    public ApprovalDto reject(AdminDetails admin, Long id, String note, HttpServletRequest http) {
        ApprovalRequestEntity r = pending(id);
        requireApprover(admin, r);
        if (note == null || note.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please give a reason for rejecting");

        stamp(r, admin, "REJECTED", note);
        ApprovalRequestEntity saved = requests.save(r);
        activityLogService.record(admin, http, r.getModuleKey(), "REJECT", r.getEntityType(), r.getEntityId(), r.getEntityRef(),
                "Rejected request #" + r.getId() + ": " + note.trim(), 200);
        return toDto(admin, saved);
    }

    @Transactional
    public ApprovalDto cancel(AdminDetails admin, Long id, HttpServletRequest http) {
        ApprovalRequestEntity r = pending(id);
        if (!r.getRequestedByAdminId().equals(admin.getAdminId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the requester can cancel this request");

        stamp(r, admin, "CANCELLED", null);
        ApprovalRequestEntity saved = requests.save(r);
        activityLogService.record(admin, http, r.getModuleKey(), "APPROVAL_CANCELLED", r.getEntityType(), r.getEntityId(),
                r.getEntityRef(), "Cancelled request #" + r.getId(), 200);
        return toDto(admin, saved);
    }

    // ---- rules ----

    @Transactional(readOnly = true)
    public List<RuleDto> listRules() {
        return rules.findAll(Sort.by("id")).stream().map(this::toRuleDto).toList();
    }

    @Transactional
    public RuleDto updateRule(Long id, RuleUpdateDto body) {
        ApprovalRuleEntity rule = rules.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found"));
        if (body.minAmount() != null) {
            if (body.minAmount().signum() < 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount cannot be negative");
            rule.setMinAmount(body.minAmount());
        }
        if (body.active() != null) rule.setActive(body.active());
        return toRuleDto(rules.save(rule));
    }

    // ---- helpers ----

    private ApprovalRequestEntity pending(Long id) {
        ApprovalRequestEntity r = requests.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!"PENDING".equals(r.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This request is already " + r.getStatus().toLowerCase());
        return r;
    }

    private void requireApprover(AdminDetails admin, ApprovalRequestEntity r) {
        AppModule m = AppModule.byKey(r.getModuleKey());
        if (m == null || !permissionService.isAllowed(admin.getRole(), m, PermissionAction.APPROVE))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have approve permission for this module");
    }

    private void stamp(ApprovalRequestEntity r, AdminDetails admin, String status, String note) {
        r.setStatus(status);
        r.setDecidedByAdminId(admin.getAdminId());
        r.setDecidedByName(fullName(admin.getAdminEntity()));
        r.setDecidedAt(LocalDateTime.now());
        r.setDecisionNote(note == null || note.isBlank() ? null : note.trim());
    }

    private ApprovalDto toDto(AdminDetails viewer, ApprovalRequestEntity r) {
        AppModule m = AppModule.byKey(r.getModuleKey());
        boolean canDecide = "PENDING".equals(r.getStatus())
                && !r.getRequestedByAdminId().equals(viewer.getAdminId())
                && m != null && permissionService.isAllowed(viewer.getRole(), m, PermissionAction.APPROVE);
        return new ApprovalDto(r.getId(), r.getModuleKey(), r.getEntityType(), r.getEntityId(), r.getEntityRef(),
                r.getRequestType(), r.getAmount(), r.getDescription(), r.getStatus(),
                r.getRequestedByAdminId(), r.getRequestedByName(), r.getRequestedAt(),
                r.getDecidedByAdminId(), r.getDecidedByName(), r.getDecidedAt(), r.getDecisionNote(), canDecide);
    }

    private RuleDto toRuleDto(ApprovalRuleEntity e) {
        return new RuleDto(e.getId(), e.getModuleKey(), e.getEntityType(), e.getAction(), e.getMode(), e.getMinAmount(), e.getActive());
    }

    private static String fullName(AdminEntity a) {
        return ((a.getAdminFirstName() == null ? "" : a.getAdminFirstName()) + " "
                + (a.getAdminLastName() == null ? "" : a.getAdminLastName())).trim();
    }

    private static String label(String type) { return type.toLowerCase().replace('_', ' '); }
    private static String nz(String s) { return s == null ? "" : s; }
}