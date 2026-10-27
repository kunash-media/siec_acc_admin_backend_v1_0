package com.siec_acc.repository;

import com.siec_acc.entity.rbac.ApprovalRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequestEntity, Long> {

    Optional<ApprovalRequestEntity> findFirstByEntityTypeAndEntityIdAndStatus(String entityType, String entityId, String status);

    @Query("select r from ApprovalRequestEntity r where (:status is null or r.status = :status) " +
            "and (r.requestedByAdminId = :me or r.moduleKey in :mods)")
    Page<ApprovalRequestEntity> visible(@Param("status") String status, @Param("me") String me,
                                        @Param("mods") Collection<String> mods, Pageable pageable);
}