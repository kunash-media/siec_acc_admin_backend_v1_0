package com.siec_acc.service;

import com.siec_acc.dto.stats.DashboardDto;

import java.util.List;

public interface DashboardService {
    DashboardDto.Summary getSummary();
    DashboardDto.IncomeExpense getIncomeExpense(String period);
    DashboardDto.TopExpenses getTopExpenses(String period);
    List<DashboardDto.ActiveProject> getActiveProjects(int limit);
    List<DashboardDto.Alert> getAlerts(int limit);
}