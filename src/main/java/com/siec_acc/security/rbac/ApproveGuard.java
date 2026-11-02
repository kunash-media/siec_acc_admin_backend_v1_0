package com.siec_acc.security.rbac;

import com.siec_acc.enum_status.AppModule;
import com.siec_acc.enum_status.PermissionAction;
import com.siec_acc.security.AdminDetails;
import com.siec_acc.service.rbac.PermissionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ApproveGuard {

    private final PermissionService permissionService;
    private final boolean enforce;

    public ApproveGuard(PermissionService permissionService, @Value("${app.rbac.enforce:false}") boolean enforce) {
        this.permissionService = permissionService;
        this.enforce = enforce;
    }

    public void require(AppModule module, String what) {
        if (!enforce) return;
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof AdminDetails d)
                || !permissionService.isAllowed(d.getRole(), module, PermissionAction.APPROVE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You need Approve permission to " + what + ".");
        }
    }
}