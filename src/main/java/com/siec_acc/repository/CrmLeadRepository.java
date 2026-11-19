package com.siec_acc.repository;


import com.siec_acc.entity.CrmLeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CrmLeadRepository extends JpaRepository<CrmLeadEntity, Long>, JpaSpecificationExecutor<CrmLeadEntity> {

    String PENDING_FOLLOWUP =
            "l.deletedLead = false AND l.leadConverted = false AND l.leadOutcome IS NULL " +
                    "AND l.followUpDate IS NOT NULL AND (l.followupStatus IS NULL OR l.followupStatus <> 'done')";

    Optional<CrmLeadEntity> findByLeadPrimeIdAndDeletedLeadFalse(Long leadPrimeId);
    Optional<CrmLeadEntity> findByPhoneAndDeletedLeadFalse(String phone);

    boolean existsByPhoneAndDeletedLeadFalse(String phone);
    boolean existsByEmailAndDeletedLeadFalse(String email);
    boolean existsByPhoneAndDeletedLeadFalseAndLeadPrimeIdNot(String phone, Long leadPrimeId);
    boolean existsByEmailAndDeletedLeadFalseAndLeadPrimeIdNot(String email, Long leadPrimeId);

    long countByDeletedLeadFalse();
    long countByDeletedLeadFalseAndLeadConvertedFalseAndLeadOutcome(String leadOutcome);

    List<CrmLeadEntity> findByDeletedLeadFalseAndCreatedAtGreaterThanEqual(LocalDateTime from);

    @Query("SELECT l FROM CrmLeadEntity l WHERE " + PENDING_FOLLOWUP + " ORDER BY l.followUpDate ASC")
    List<CrmLeadEntity> findPendingFollowups();

    @Query("SELECT COUNT(l) FROM CrmLeadEntity l WHERE " + PENDING_FOLLOWUP)
    long countPendingFollowups();

    @Query("SELECT COUNT(l) FROM CrmLeadEntity l WHERE " + PENDING_FOLLOWUP + " AND l.followUpDate = :date")
    long countPendingFollowupsOn(@Param("date") LocalDate date);

    @Query("SELECT c.category, COUNT(DISTINCT l.leadPrimeId) FROM CrmLeadRequirementCategoryEntity c " +
            "JOIN c.lead l WHERE l.deletedLead = false GROUP BY c.category")
    List<Object[]> countByRequirementCategory();
}