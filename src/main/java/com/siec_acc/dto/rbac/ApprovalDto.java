package com.siec_acc.dto.rbac;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ApprovalDto(Long id, String moduleKey, String entityType, String entityId, String entityRef,
                          String requestType, BigDecimal amount, String description, String status,
                          String requestedByAdminId, String requestedByName, LocalDateTime requestedAt,
                          String decidedByAdminId, String decidedByName, LocalDateTime decidedAt,
                          String decisionNote, boolean canDecide) {}