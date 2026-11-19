package com.siec_acc.repository;


import com.siec_acc.entity.CrmStaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmStaffRepository extends JpaRepository<CrmStaffEntity, Long> {
    List<CrmStaffEntity> findByActiveTrueOrderByStaffFirstNameAsc();
}