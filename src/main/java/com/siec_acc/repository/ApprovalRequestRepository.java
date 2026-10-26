package com.siec_acc.repository;

import com.siec_acc.entity.ApprovalRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequestEntity, Long> {}