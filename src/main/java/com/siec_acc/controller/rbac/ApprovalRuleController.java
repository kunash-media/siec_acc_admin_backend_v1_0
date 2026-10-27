package com.siec_acc.controller.rbac;

import com.siec_acc.dto.rbac.RuleDto;
import com.siec_acc.dto.rbac.RuleUpdateDto;
import com.siec_acc.service.rbac.ApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/approval-rules")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class ApprovalRuleController {

    private final ApprovalService approvalService;

    public ApprovalRuleController(ApprovalService approvalService) { this.approvalService = approvalService; }

    @GetMapping
    public List<RuleDto> list() { return approvalService.listRules(); }

    @PutMapping("/{id}")
    public RuleDto update(@PathVariable Long id, @RequestBody RuleUpdateDto body) {
        return approvalService.updateRule(id, body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handle(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("message", e.getReason() == null ? "Request failed" : e.getReason()));
    }
}