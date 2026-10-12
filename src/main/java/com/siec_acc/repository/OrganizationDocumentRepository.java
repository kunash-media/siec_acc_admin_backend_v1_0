package com.siec_acc.repository;

import com.siec_acc.entity.OrganizationDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationDocumentRepository extends JpaRepository<OrganizationDocumentEntity, Long> {
    List<OrganizationDocumentEntity> findAllByOrganization_OrganizationIdOrderByCreatedAtDesc(String organizationId);
    Optional<OrganizationDocumentEntity> findByDocumentIdAndOrganization_OrganizationId(String documentId, String organizationId);
    void deleteAllByOrganization_OrganizationPrimeId(Long organizationPrimeId);
}