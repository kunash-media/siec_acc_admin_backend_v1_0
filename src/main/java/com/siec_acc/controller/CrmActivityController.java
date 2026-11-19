package com.siec_acc.controller;


import com.siec_acc.dto.response.CrmDtos;
import com.siec_acc.service.CrmActivityService;
import com.siec_acc.service.CrmDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm/v1")
public class CrmActivityController {

    private final CrmActivityService activityService;
    private final CrmDashboardService dashboardService;

    public CrmActivityController(CrmActivityService activityService, CrmDashboardService dashboardService) {
        this.activityService = activityService;
        this.dashboardService = dashboardService;
    }

    /** q also matches the lead no — this powers "View Journey". */
    @GetMapping("/activities")
    public ResponseEntity<CrmDtos.PageResponse<CrmDtos.ActivityResponse>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(activityService.search(type, action, q, page, size));
    }

    @GetMapping("/activities/stats")
    public ResponseEntity<CrmDtos.ActivityStats> stats() { return ResponseEntity.ok(activityService.stats()); }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<CrmDtos.DashboardSummary> dashboard() { return ResponseEntity.ok(dashboardService.summary()); }
}