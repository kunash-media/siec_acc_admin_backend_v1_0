package com.siec_acc.dto.request;


import jakarta.validation.constraints.NotBlank;

public record CrmLeadOutcomeRequest(@NotBlank(message = "leadOutcome is required") String leadOutcome,   // won / lost / open
                                    String lostReason) {}