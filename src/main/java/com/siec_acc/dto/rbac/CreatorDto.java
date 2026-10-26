package com.siec_acc.dto.rbac;

import java.time.LocalDateTime;

public record CreatorDto(String adminId, String adminName, LocalDateTime createdAt) {}