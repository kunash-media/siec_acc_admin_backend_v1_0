package com.siec_acc.repository;


import com.siec_acc.entity.CrmDealEntity;
import com.siec_acc.enum_status.CrmDealStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;

@Repository
public interface CrmDealRepository extends JpaRepository<CrmDealEntity, Long>, JpaSpecificationExecutor<CrmDealEntity> {

    long countByStageIn(Collection<CrmDealStage> stages);

    long countByStage(CrmDealStage stage);

    // returns null when there are no rows — callers must null-check
    @Query("select sum(d.amount) from CrmDealEntity d where d.stage in :stages")
    BigDecimal sumAmountByStageIn(@Param("stages") Collection<CrmDealStage> stages);
}