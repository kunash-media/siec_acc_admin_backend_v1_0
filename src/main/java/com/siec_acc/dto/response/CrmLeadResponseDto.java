package com.siec_acc.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CrmLeadResponseDto(
        Long leadPrimeId, String leadStrId, String firstName, String lastName, String email, String phone,
        String company, String designation, String city, String status, String priority, String source,
        String referralDetails, List<String> requirementCategory, String tags,
        LocalDate followUpDate, String followupStatus, String notes,
        Boolean leadConverted, Boolean contacted, String leadOutcome, String lostReason, String pipelineStatus,
        Long assignedStaffId, String assignedStaffName, LocalDateTime createdAt, LocalDateTime updatedAt) {}