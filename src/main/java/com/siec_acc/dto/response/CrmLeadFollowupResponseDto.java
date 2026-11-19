package com.siec_acc.dto.response;


import java.time.LocalDate;
import java.time.LocalDateTime;

public record CrmLeadFollowupResponseDto(Long followupPrimeId, LocalDate followupDate, String followupStatus,
                                         String followupNotes, String meetingType, Long takenByStaffId,
                                         String takenByStaffName, LocalDateTime createdAt) {}