package com.siec_acc.repository;

import com.siec_acc.dto.response.VendorListDto;
import com.siec_acc.entity.VendorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<VendorEntity, Long> {

    Optional<VendorEntity> findByVendorStrId(String vendorStrId);

    Optional<VendorEntity> findByPhone(String phone);

    boolean existsByVendorStrId(String vendorStrId);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByGstin(String gstin);

    @Query("select new com.siec_acc.dto.response.VendorListDto(" +
            "v.vendorPrimeId, v.vendorStrId, v.vendorName, v.companyName) " +
            "from VendorEntity v order by v.vendorPrimeId asc")
    List<VendorListDto> findAllVendorList();
}
