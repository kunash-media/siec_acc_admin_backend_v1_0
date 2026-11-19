package com.siec_acc.repository;


import com.siec_acc.entity.CrmActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CrmActivityRepository extends JpaRepository<CrmActivityEntity, Long>, JpaSpecificationExecutor<CrmActivityEntity> {

    void deleteByLeadStrId(String leadStrId);

    long countByAction(String action);

    long countByCreatedAtGreaterThanEqual(LocalDateTime from);
}