package com.siec_acc.repository;

import com.siec_acc.dto.response.DeliveryChallanSummaryResponseDto;
import com.siec_acc.entity.DeliveryChallanEntity;
import com.siec_acc.enum_status.DcStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface DeliveryChallanRepository extends JpaRepository<DeliveryChallanEntity, Long> {

    Optional<DeliveryChallanEntity> findByDcStrId(String dcStrId);

    long countByDcStatus(DcStatus status);

    @Query("SELECT d FROM DeliveryChallanEntity d WHERE " +
            "(:status IS NULL OR d.dcStatus = :status) " +
            "AND (:clientPrimeId IS NULL OR d.dcClientPrimeId = :clientPrimeId) " +
            "AND (:search IS NULL OR LOWER(d.dcStrId) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(d.dcClientName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(d.dcClientCompanyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(d.dcSoNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(d.dcReferenceNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DeliveryChallanEntity> search(@Param("status") DcStatus status,
                                       @Param("clientPrimeId") Long clientPrimeId,
                                       @Param("search") String search,
                                       Pageable pageable);

    @Query("SELECT MAX(CAST(SUBSTRING(d.dcStrId, :startPos) AS integer)) FROM DeliveryChallanEntity d " +
            "WHERE d.dcStrId LIKE CONCAT(:prefix, '%')")
    Integer findMaxSequence(@Param("prefix") String prefix, @Param("startPos") int startPos);

    @Query("SELECT new com.siec_acc.dto.response.DeliveryChallanSummaryResponseDto(" +
            "d.dcPrimeId, d.dcStrId, d.dcDate, d.dcClientPrimeId, d.dcStatus) " +
            "FROM DeliveryChallanEntity d")
    Page<DeliveryChallanSummaryResponseDto> findDcSummaries(Pageable pageable);
}