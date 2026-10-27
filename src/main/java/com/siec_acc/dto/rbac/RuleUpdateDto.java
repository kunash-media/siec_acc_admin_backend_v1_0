package com.siec_acc.dto.rbac;

import java.math.BigDecimal;

public record RuleUpdateDto(BigDecimal minAmount, Boolean active) {}