package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.response.CrmDtos;
import com.siec_acc.enum_status.CrmDealStage;
import com.siec_acc.repository.CrmActivityRepository;
import com.siec_acc.repository.CrmDealRepository;
import com.siec_acc.repository.CrmLeadRepository;
import com.siec_acc.service.CrmDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class CrmDashboardServiceImpl implements CrmDashboardService {

    private final CrmLeadRepository leadRepository;
    private final CrmDealRepository dealRepository;
    private final CrmActivityRepository activityRepository;

    public CrmDashboardServiceImpl(CrmLeadRepository leadRepository, CrmDealRepository dealRepository, CrmActivityRepository activityRepository) {
        this.leadRepository = leadRepository;
        this.dealRepository = dealRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public CrmDtos.DashboardSummary summary() {
        long pending = leadRepository.countPendingFollowups();
        long loggedFollowups = activityRepository.countByAction("Follow-up");
        long lostLeads = leadRepository.countByDeletedLeadFalseAndLeadConvertedFalseAndLeadOutcome("lost");
        long lostDeals = dealRepository.countByStage(CrmDealStage.CLOSED_LOST);

        List<CrmDtos.ServiceCount> byService = leadRepository.countByRequirementCategory().stream()
                .map(r -> new CrmDtos.ServiceCount(String.valueOf(r[0]), ((Number) r[1]).longValue()))
                .sorted(Comparator.comparingLong(CrmDtos.ServiceCount::count).reversed())
                .toList();

        return new CrmDtos.DashboardSummary(
                leadRepository.countByDeletedLeadFalse(),
                leadRepository.countPendingFollowupsOn(LocalDate.now()),
                pending + loggedFollowups,
                dealRepository.countByStage(CrmDealStage.CLOSED_WON),
                lostLeads + lostDeals,
                byService);
    }
}