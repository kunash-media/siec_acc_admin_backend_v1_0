package com.siec_acc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CrmLeadFollowupRequestDto(
        @NotNull(message = "Follow-up date is required") LocalDate followupDate,
        @NotBlank(message = "Follow-up status is required") String followupStatus,   // pending / done
        String followupNotes, String meetingType, Long takenByStaffId) {}