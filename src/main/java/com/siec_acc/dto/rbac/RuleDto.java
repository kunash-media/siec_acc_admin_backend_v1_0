package com.siec_acc.dto.rbac;

import java.math.BigDecimal;

public record RuleDto(Long id, String moduleKey, String entityType, String action, String mode,
                      BigDecimal minAmount, Boolean active) {}