package com.siec_acc.service.rbac;

import com.siec_acc.entity.ActivityLogEntity;
import com.siec_acc.entity.AdminEntity;
import com.siec_acc.repository.ActivityLogRepository;
import com.siec_acc.security.AdminDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AdminDetails admin, HttpServletRequest request, String moduleKey, String action,
                       String entityType, String entityId, String entityRef, String summary, Integer status) {
        AdminEntity a = admin.getAdminEntity();
        String name = ((a.getAdminFirstName() == null ? "" : a.getAdminFirstName()) + " "
                + (a.getAdminLastName() == null ? "" : a.getAdminLastName())).trim();

        String fwd = request.getHeader("X-Forwarded-For");
        String ip = (fwd != null && !fwd.isBlank()) ? fwd.split(",")[0].trim() : request.getRemoteAddr();

        repository.save(ActivityLogEntity.builder()
                .adminId(a.getAdminId())
                .adminNameSnapshot(cut(name, 150))
                .roleSnapshot(cut(a.getAdminRole(), 50))
                .moduleKey(moduleKey)
                .action(action)
                .entityType(cut(entityType, 50))
                .entityId(cut(entityId, 50))
                .entityRef(cut(entityRef, 100))
                .summary(cut(summary, 500))
                .httpMethod(request.getMethod())
                .path(cut(request.getRequestURI(), 300))
                .statusCode(status)
                .ipAddress(cut(ip, 60))
                .userAgent(cut(request.getHeader("User-Agent"), 300))
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(HttpServletRequest request, String mobile, String reason) {
        String masked = (mobile == null || mobile.length() < 4) ? "****" : "******" + mobile.substring(mobile.length() - 4);
        String fwd = request.getHeader("X-Forwarded-For");
        String ip = (fwd != null && !fwd.isBlank()) ? fwd.split(",")[0].trim() : request.getRemoteAddr();

        repository.save(ActivityLogEntity.builder()
                .adminNameSnapshot(masked)
                .moduleKey("auth")
                .action("LOGIN_FAILED")
                .entityType("ADMIN")
                .summary(cut(reason + " for " + masked, 500))
                .httpMethod(request.getMethod())
                .path(cut(request.getRequestURI(), 300))
                .statusCode(reason.startsWith("Account") ? 403 : 401)
                .ipAddress(cut(ip, 60))
                .userAgent(cut(request.getHeader("User-Agent"), 300))
                .build());
    }

    private static String cut(String s, int max) {
        return s == null ? null : (s.length() > max ? s.substring(0, max) : s);
    }
}