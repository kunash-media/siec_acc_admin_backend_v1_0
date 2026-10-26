package com.siec_acc.repository;

import com.siec_acc.entity.ActivityLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long> {

    @Query("select l from ActivityLogEntity l where " +
            "(:adminId is null or l.adminId = :adminId) and " +
            "(:moduleKey is null or l.moduleKey = :moduleKey) and " +
            "(:action is null or l.action = :action) and " +
            "(:entityType is null or l.entityType = :entityType) and " +
            "(:entityId is null or l.entityId = :entityId) and " +
            "(:fromTs is null or l.createdAt >= :fromTs) and " +
            "(:toTs is null or l.createdAt < :toTs) and " +
            "(:search is null or lower(l.summary) like lower(concat('%', :search, '%')) " +
            "  or lower(l.entityRef) like lower(concat('%', :search, '%')) " +
            "  or lower(l.adminNameSnapshot) like lower(concat('%', :search, '%')))")
    Page<ActivityLogEntity> search(@Param("adminId") String adminId,
                                   @Param("moduleKey") String moduleKey,
                                   @Param("action") String action,
                                   @Param("entityType") String entityType,
                                   @Param("entityId") String entityId,
                                   @Param("fromTs") LocalDateTime fromTs,
                                   @Param("toTs") LocalDateTime toTs,
                                   @Param("search") String search,
                                   Pageable pageable);

    List<ActivityLogEntity> findByActionAndEntityTypeAndEntityIdIn(String action, String entityType, Collection<String> entityIds);
}