package com.siec_acc.repository;


import com.siec_acc.entity.CrmLeadFollowupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmLeadFollowupRepository extends JpaRepository<CrmLeadFollowupEntity, Long> {
    List<CrmLeadFollowupEntity> findByLead_LeadPrimeIdAndDeletedFollowupFalseOrderByFollowupDateDescFollowupPrimeIdDesc(Long leadPrimeId);
}