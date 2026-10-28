package com.siec_acc.security.rbac;

import com.siec_acc.entity.rbac.ApprovalRequestEntity;
import com.siec_acc.enum_status.AppModule;
import com.siec_acc.enum_status.PermissionAction;
import com.siec_acc.security.AdminDetails;
import com.siec_acc.service.rbac.ActivityLogService;
import com.siec_acc.service.rbac.ApprovalService;
import com.siec_acc.service.rbac.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccessInterceptor.class);

    static final String ATTR_ADMIN  = "rbac.admin";
    static final String ATTR_MATCH  = "rbac.match";
    static final String ATTR_ACTION = "rbac.logAction";
    static final String ATTR_REF    = "rbac.entityRef";

    private final PermissionService permissionService;
    private final ActivityLogService activityLogService;
    private final boolean enforce;
    private final ApprovalService approvalService;

    public AdminAccessInterceptor(PermissionService permissionService,
                                  ActivityLogService activityLogService,
                                  ApprovalService approvalService,
                                  @Value("${app.rbac.enforce:false}") boolean enforce) {
        this.permissionService = permissionService;
        this.activityLogService = activityLogService;
        this.approvalService = approvalService;
        this.enforce = enforce;
    }


    static boolean isMutating(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if ("OPTIONS".equals(method)) return true;

        String path = request.getRequestURI();
       AppModule.Match match = AppModule.resolve(path);
        if (match == null) return true;                       // not a module route

        boolean mutating = isMutating(method);
        if (!mutating && match.sharedLookup()) return true;

        AdminDetails admin = currentAdmin();
        if (admin == null) {
            if (!enforce) return true;                        // shadow mode: behave exactly as today
            return deny(response, 401, "Login required");
        }

        request.setAttribute(ATTR_ADMIN, admin);
        request.setAttribute(ATTR_MATCH, match);
        if (mutating) request.setAttribute(ATTR_ACTION, logActionFor(path, method));

        boolean allowed = neededActions(path, method).stream()
                .anyMatch(a -> permissionService.isAllowed(admin.getRole(), match.module(), a));
        if (allowed) {
            if ("DELETE".equals(method) && approvalService.shouldGate(admin, match)) {
                return gateDelete(request, response, admin, match);
            }
            return true;
        }

        if (mutating) {
            safeLog(() -> activityLogService.record(admin, request, match.module().getKey(),
                    enforce ? "DENIED" : "WOULD_DENY", match.entityType(), null, null,
                    "Blocked " + method + " " + path, enforce ? 403 : null));
        } else {
            logger.warn("[rbac] {} would be denied: role={} module={} {} {}",
                    enforce ? "read" : "shadow read", admin.getRole(), match.module().getKey(), method, path);
        }
        request.removeAttribute(ATTR_ACTION);                 // a blocked/shadow-denied call is not a "done" action

        return enforce ? deny(response, 403, "You do not have permission to perform this action") : true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String action = (String) request.getAttribute(ATTR_ACTION);
        AdminDetails admin = (AdminDetails) request.getAttribute(ATTR_ADMIN);
        AppModule.Match match = (AppModule.Match) request.getAttribute(ATTR_MATCH);
        if (action == null || admin == null || match == null || ex != null) return;

        int status = response.getStatus();
        if (status < 200 || status >= 300) return;

        String path = request.getRequestURI();
        String[] ref = (String[]) request.getAttribute(ATTR_REF);     // id, number, amount, status (from the response body)
        String id = ref != null ? ref[0] : null;
        String number = ref != null ? ref[1] : null;
        String amount = ref != null ? ref[2] : null;
        String bodyStatus = ref != null ? ref[3] : null;

        // Sales order approve/reject is a PATCH whose new status comes back in the response
        if ("UPDATE".equals(action) && path.contains("/patch-sales-order/") && bodyStatus != null) {
            if ("approved".equalsIgnoreCase(bodyStatus)) action = "APPROVE";
            else if ("rejected".equalsIgnoreCase(bodyStatus)) action = "REJECT";
        }

        String convertedTo = null;
        if ("CONVERT".equals(action)) {
            String[] seg = path.split("/");
            if (seg.length >= 2) id = seg[seg.length - 2];            // the proforma id is in the URL
            convertedTo = number;                                     // the response carries the NEW invoice
            number = null;
        }

        if (id == null && "DELETE".equals(action) && !path.contains("/bulk-")) {
            id = path.substring(path.lastIndexOf('/') + 1);
        }

        String st = request.getParameter("status");
        String summary = action + " " + match.entityType().toLowerCase()
                + (number != null ? " " + number : (id != null ? " #" + id : ""))
                + (st != null && !st.isBlank() ? " → " + st.trim() : "")
                + (convertedTo != null ? " → invoice " + convertedTo : "");

        final String fAction = action, fId = id, fNumber = number, fAmount = amount;
        safeLog(() -> activityLogService.record(admin, request, match.module().getKey(), fAction,
                match.entityType(), fId, fNumber, summary, status));

        if ("CREATE".equals(fAction)) {
            safeLog(() -> approvalService.openReviewIfRequired(admin, match, fId, fNumber, fAmount));
        }
    }

    private boolean gateDelete(HttpServletRequest request, HttpServletResponse response,
                               AdminDetails admin, AppModule.Match match) throws IOException {
        String path = request.getRequestURI();
        String entityId = path.substring(path.lastIndexOf('/') + 1);

        ApprovalRequestEntity req = approvalService.submitDeleteRequest(admin, match, entityId);
        request.removeAttribute(ATTR_ACTION);                 // nothing was deleted, so no DELETE row

        safeLog(() -> activityLogService.record(admin, request, match.module().getKey(), "APPROVAL_REQUESTED",
                match.entityType(), entityId, req.getEntityRef(),
                "Delete request #" + req.getId() + " sent for approval", 202));

        response.setStatus(202);
        response.setContentType("application/json");
        response.getWriter().write("{\"approvalRequired\":true,\"approvalId\":" + req.getId()
                + ",\"message\":\"Delete request sent for approval\"}");
        return false;
    }

    private static Set<PermissionAction> neededActions(String path, String method) {
        if (path.endsWith("/upload-pdf")) return EnumSet.of(PermissionAction.CREATE, PermissionAction.EDIT);
        if (path.contains("/approve") || path.contains("/reject") || path.contains("/patch-sales-order/"))
            return EnumSet.of(PermissionAction.APPROVE);
        if (path.contains("/delete")) return EnumSet.of(PermissionAction.DELETE);
        return switch (method) {
            case "POST" -> EnumSet.of(PermissionAction.CREATE);
            case "PUT", "PATCH" -> EnumSet.of(PermissionAction.EDIT);
            case "DELETE" -> EnumSet.of(PermissionAction.DELETE);
            default -> EnumSet.of(PermissionAction.VIEW);
        };
    }

    private static String logActionFor(String path, String method) {
        if (path.endsWith("/upload-pdf")) return null;        // companion call of create/update, not a separate action
        if (path.contains("/convert-to-invoice")) return "CONVERT";
        if (path.contains("/approve")) return "APPROVE";
        if (path.contains("/reject")) return "REJECT";
        if (path.contains("/delete")) return "DELETE";
        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> null;
        };
    }

    private static AdminDetails currentAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return (a != null && a.getPrincipal() instanceof AdminDetails d) ? d : null;
    }

    private boolean deny(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
        return false;
    }

    // Logging must never break the business request
    private void safeLog(Runnable r) {
        try { r.run(); } catch (Exception e) { logger.warn("[rbac] activity log write failed: {}", e.getMessage()); }
    }
}