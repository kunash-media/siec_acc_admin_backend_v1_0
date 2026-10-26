package com.siec_acc.dto.rbac;


import java.time.LocalDateTime;

public record ActivityLogDto(Long id, String adminId, String adminName, String role, String moduleKey, String action,
                             String entityType, String entityId, String entityRef, String summary,
                             String httpMethod, String path, Integer statusCode, String ipAddress,
                             LocalDateTime createdAt) {}