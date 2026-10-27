package com.siec_acc.controller.rbac;

import com.siec_acc.dto.rbac.ApprovalDto;
import com.siec_acc.dto.rbac.DecisionRequestDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.security.AdminDetails;
import com.siec_acc.service.rbac.ApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/approvals")
@PreAuthorize("isAuthenticated()")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) { this.approvalService = approvalService; }

    @GetMapping
    public PagedResponseDto<ApprovalDto> list(@AuthenticationPrincipal AdminDetails admin,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") int pageNumber,
                                              @RequestParam(defaultValue = "25") int pageSize) {
        return approvalService.list(admin, status, pageNumber, pageSize);
    }

    @PostMapping("/{id}/approve")
    public ApprovalDto approve(@AuthenticationPrincipal AdminDetails admin, @PathVariable Long id,
                               @RequestBody(required = false) DecisionRequestDto body, HttpServletRequest http) {
        return approvalService.approve(admin, id, body == null ? null : body.note(), http);
    }

    @PostMapping("/{id}/reject")
    public ApprovalDto reject(@AuthenticationPrincipal AdminDetails admin, @PathVariable Long id,
                              @RequestBody(required = false) DecisionRequestDto body, HttpServletRequest http) {
        return approvalService.reject(admin, id, body == null ? null : body.note(), http);
    }

    @PostMapping("/{id}/cancel")
    public ApprovalDto cancel(@AuthenticationPrincipal AdminDetails admin, @PathVariable Long id, HttpServletRequest http) {
        return approvalService.cancel(admin, id, http);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handle(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("message", e.getReason() == null ? "Request failed" : e.getReason()));
    }
}