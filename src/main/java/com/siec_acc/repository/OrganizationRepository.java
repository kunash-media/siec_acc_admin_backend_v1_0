package com.siec_acc.repository;

import com.siec_acc.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {
    Optional<OrganizationEntity> findByOrganizationId(String organizationId);
    boolean existsByOrganizationId(String organizationId);
}