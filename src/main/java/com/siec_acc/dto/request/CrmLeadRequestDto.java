package com.siec_acc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

/** `status` is the lead temperature (hot / warm / cold). */
public record CrmLeadRequestDto(
        @NotBlank(message = "First name is required") String firstName,
        String lastName,
        @Email(message = "Enter a valid email address") String email,
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number") String phone,
        String company, String designation, String city,
        String status, String priority, String source, String referralDetails,
        List<String> requirementCategory, String tags,
        LocalDate followUpDate, String followupStatus, String notes,
        Long assignedStaffId) {}