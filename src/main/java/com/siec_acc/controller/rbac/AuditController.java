package com.siec_acc.controller.rbac;

import com.siec_acc.dto.rbac.ActivityLogDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.entity.ActivityLogEntity;
import com.siec_acc.repository.ActivityLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditController {

    private final ActivityLogRepository repository;

    public AuditController(ActivityLogRepository repository) { this.repository = repository; }

    @GetMapping
    public PagedResponseDto<ActivityLogDto> list(
            @RequestParam(required = false) String adminId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "25") int pageSize) {

        int size = pageSize <= 0 ? 25 : Math.min(pageSize, 100);
        Page<ActivityLogEntity> page = repository.search(
                blank(adminId), blank(module), upper(action), upper(entityType), blank(entityId),
                from == null ? null : from.atStartOfDay(),
                to == null ? null : to.plusDays(1).atStartOfDay(),
                blank(search),
                PageRequest.of(Math.max(pageNumber, 1) - 1, size,
                        Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))));

        return PagedResponseDto.<ActivityLogDto>builder()
                .content(page.getContent().stream().map(l -> new ActivityLogDto(l.getId(), l.getAdminId(),
                        l.getAdminNameSnapshot(), l.getRoleSnapshot(), l.getModuleKey(), l.getAction(),
                        l.getEntityType(), l.getEntityId(), l.getEntityRef(), l.getSummary(), l.getHttpMethod(),
                        l.getPath(), l.getStatusCode(), l.getIpAddress(), l.getCreatedAt())).toList())
                .pageNumber(pageNumber)
                .pageSize(size)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private static String blank(String s) { return (s == null || s.trim().isEmpty()) ? null : s.trim(); }
    private static String upper(String s) { String b = blank(s); return b == null ? null : b.toUpperCase(); }
}