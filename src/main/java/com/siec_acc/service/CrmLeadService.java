package com.siec_acc.service;


import com.siec_acc.dto.request.CrmLeadFollowupRequestDto;
import com.siec_acc.dto.request.CrmLeadRequestDto;
import com.siec_acc.dto.response.CrmLeadFollowupResponseDto;
import com.siec_acc.dto.response.CrmLeadResponseDto;
import com.siec_acc.dto.response.CrmMonthlyLeadCountDto;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Lead registration, pipeline views, follow-ups and outcome. Implementation: impl/CrmLeadServiceImpl. */
public interface CrmLeadService {

    CrmLeadResponseDto createLead(CrmLeadRequestDto dto);

    CrmLeadResponseDto updateLead(Long id, CrmLeadRequestDto dto);

    CrmLeadResponseDto getLead(Long id);

    /** soft delete; also removes the lead's activity history */
    void deleteLead(Long id);

    /** returns the existing lead if the phone is already registered, else null */
    CrmLeadResponseDto checkPhoneExists(String phone);

    /** stage = registered | contacted | converted | lost | all */
    Page<CrmLeadResponseDto> searchPipeline(String stage, String source, String q, int page, int size);

    Map<String, Long> pipelineCounts();

    List<CrmLeadResponseDto> getPendingFollowups();

    CrmLeadResponseDto markContacted(Long id, String note, LocalDate nextFollowup);

    CrmLeadResponseDto addFollowup(Long id, CrmLeadFollowupRequestDto dto);

    List<CrmLeadFollowupResponseDto> getFollowupHistory(Long id);

    /** outcome = won | lost | open */
    void updateLeadOutcome(Long id, String outcome, String lostReason);

    List<CrmMonthlyLeadCountDto> getMonthlyLeadCounts(int monthsBack);
}