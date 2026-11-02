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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
//    private static final ObjectMapper JSON = new ObjectMapper();
//    private static final Set<String> BODY_STATUS_ENTITIES = Set.of("EXPENSE", "PURCHASE", "PURCHASE_ORDER");

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
        if (!mutating && match.sharedLookup() && !path.contains("/documents")) return true;

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
        if ("UPDATE".equals(action) && bodyStatus != null && "PATCH".equals(request.getMethod())
                && (path.contains("/patch-sales-order/") || path.contains("/patch-purchase-order/"))) {
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

        if (action.startsWith("STOCK_")) {          // the item id is in the URL; the response is a stock record
            id = path.substring(path.lastIndexOf('/') + 1);
            number = null;
        }
        if (id == null && "DELETE".equals(action) && !path.contains("/bulk-")) {
            id = path.substring(path.lastIndexOf('/') + 1);
        }

        String st = request.getParameter("status");
        String summary = action + " " + match.entityType().toLowerCase()
                + (number != null ? " " + number : (id != null ? " #" + id : ""))
                + (st != null && !st.isBlank() ? " → " + st.trim() : "")
                + (convertedTo != null ? " → invoice " + convertedTo : "")
                + (path.contains("/bulk-") ? " (bulk)" : "");

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


    // "/approve-expense/ID", "/bulk-approve", "/approve" all count; "/approvals" does not
    private static boolean hasVerb(String path, String verb) {
        return path.matches(".*/(bulk-)?" + verb + "(-[A-Za-z-]+)?(/.*)?");
    }

//    private static boolean approvalStatusInBody(HttpServletRequest request) {
//        if (!(request instanceof CachedBodyRequest c)) return false;
//        try {
//            JsonNode n = JSON.readTree(c.getBody());
//            String s = (n != null && n.hasNonNull("status")) ? n.get("status").asText("") : "";
//            return "approved".equalsIgnoreCase(s) || "rejected".equalsIgnoreCase(s);
//        } catch (Exception e) {
//            return false;
//        }
//    }


    private static Set<PermissionAction> neededActions(String path, String method) {
        if (path.endsWith("/upload-pdf") || path.endsWith("/upload-receipt"))
            return EnumSet.of(PermissionAction.CREATE, PermissionAction.EDIT);
        // documents and links change an existing project; they are not new or deleted projects
        if (isProjectPart(path)) return EnumSet.of(PermissionAction.EDIT);
        if (hasVerb(path, "approve") || hasVerb(path, "reject") || path.contains("/patch-sales-order/"))
            return EnumSet.of(PermissionAction.APPROVE);
        if (hasVerb(path, "delete")) return EnumSet.of(PermissionAction.DELETE);
        return switch (method) {
            case "POST" -> EnumSet.of(PermissionAction.CREATE);
            case "PUT", "PATCH" -> EnumSet.of(PermissionAction.EDIT);
            case "DELETE" -> EnumSet.of(PermissionAction.DELETE);
            default -> EnumSet.of(PermissionAction.VIEW);
        };
    }

    private static boolean isProjectPart(String path) {
        return path.contains("/upload-document") || path.contains("/delete-document")
                || path.contains("/link-record") || path.contains("/unlink-record");
    }

    private static String logActionFor(String path, String method) {
        if (path.endsWith("/upload-pdf") || path.endsWith("/upload-receipt")) return null;
        if (path.contains("/convert-to-invoice")) return "CONVERT";
        if (isProjectPart(path)) return "UPDATE";
        if (path.contains("/add-stock") || path.contains("/add-variant-stock")) return "STOCK_IN";
        if (path.contains("/reduce-stock") || path.contains("/reduce-variant-stock")) return "STOCK_OUT";
        if (path.contains("/adjust-stock") || path.contains("/adjust-variant-stock")) return "STOCK_ADJUST";
        if (hasVerb(path, "approve")) return "APPROVE";
        if (hasVerb(path, "reject")) return "REJECT";
        if (hasVerb(path, "delete")) return "DELETE";
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