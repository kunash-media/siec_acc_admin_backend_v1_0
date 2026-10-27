package com.siec_acc.repository;

import com.siec_acc.entity.rbac.ApprovalRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRuleRepository extends JpaRepository<ApprovalRuleEntity, Long> {
    boolean existsByEntityTypeAndActionAndModeAndActiveTrue(String entityType, String action, String mode);
    boolean existsByEntityTypeAndAction(String entityType, String action);
}