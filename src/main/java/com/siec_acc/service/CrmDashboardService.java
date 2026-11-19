package com.siec_acc.service;


import com.siec_acc.dto.response.CrmDtos;

/** Numbers for the CRM dashboard tab. Implementation: impl/CrmDashboardServiceImpl. */
public interface CrmDashboardService {

    CrmDtos.DashboardSummary summary();
}