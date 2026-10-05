package com.siec_acc.repository;

import com.siec_acc.entity.ProformaEntity;
import com.siec_acc.enum_status.ProformaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProformaRepository extends JpaRepository<ProformaEntity, Long> {

    Optional<ProformaEntity> findByProformaStrId(String proformaStrId);

    boolean existsByProformaStrId(String proformaStrId);

    @Query("select p from ProformaEntity p where " +
           "(:status is null or p.proformaStatus = :status) and " +
           "(:customerId is null or p.proformaCustomerId = :customerId) and " +
           "(:search is null or lower(p.proformaStrId) like lower(concat('%', :search, '%')) " +
           "  or lower(p.proformaCustomerName) like lower(concat('%', :search, '%')))")
    Page<ProformaEntity> search(@Param("status") ProformaStatus status,
                                 @Param("customerId") Long customerId,
                                 @Param("search") String search,
                                 Pageable pageable);

    long countByProformaStatus(ProformaStatus status);

    @Query("select coalesce(sum(p.proformaTotalAmountInr), 0) from ProformaEntity p")
    BigDecimal sumTotalValueInr();

    long countByProformaCustomerId(Long customerId);
}
