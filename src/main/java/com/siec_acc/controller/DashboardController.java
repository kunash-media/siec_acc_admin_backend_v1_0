package com.siec_acc.controller;

import com.siec_acc.dto.stats.DashboardDto;
import com.siec_acc.exceptions.ApiResponse;
import com.siec_acc.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardDto.Summary>> summary() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary fetched.", dashboardService.getSummary()));
    }

    @GetMapping("/income-expense")
    public ResponseEntity<ApiResponse<DashboardDto.IncomeExpense>> incomeExpense(
            @RequestParam(required = false) String period) {
        return ResponseEntity.ok(ApiResponse.success("Income and expense fetched.", dashboardService.getIncomeExpense(period)));
    }

    @GetMapping("/top-expenses")
    public ResponseEntity<ApiResponse<DashboardDto.TopExpenses>> topExpenses(
            @RequestParam(required = false) String period) {
        return ResponseEntity.ok(ApiResponse.success("Top expenses fetched.", dashboardService.getTopExpenses(period)));
    }

    @GetMapping("/active-projects")
    public ResponseEntity<ApiResponse<List<DashboardDto.ActiveProject>>> activeProjects(
            @RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Active projects fetched.", dashboardService.getActiveProjects(limit)));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<DashboardDto.Alert>>> alerts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Alerts fetched.", dashboardService.getAlerts(limit)));
    }

    @GetMapping("/projects/{projectStrId}/export")
    public ResponseEntity<ApiResponse<DashboardDto.ProjectExport>> projectExport(@PathVariable String projectStrId) {
        return ResponseEntity.ok(ApiResponse.success("Project export data fetched.",
                dashboardService.getProjectExport(projectStrId)));
    }
}