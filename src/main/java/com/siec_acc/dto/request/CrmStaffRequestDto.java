package com.siec_acc.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CrmStaffRequestDto(@NotBlank(message = "First name is required") String staffFirstName,
                                 String staffLastName, String staffRole, String staffEmail, String staffMobile) {}