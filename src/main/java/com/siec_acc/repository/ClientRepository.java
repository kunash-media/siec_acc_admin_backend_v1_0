package com.siec_acc.repository;

import com.siec_acc.entity.ClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long>,
        JpaSpecificationExecutor<ClientEntity> {

    Optional<ClientEntity> findByClientStrId(String clientStrId);

    boolean existsByClientEmailIgnoreCase(String clientEmail);

    boolean existsByClientGstinIgnoreCase(String clientGstin);

    @Query("SELECT c FROM ClientEntity c WHERE " +
            ":search IS NULL OR :search = '' OR " +
            "LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.clientCompanyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.clientStrId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.clientEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "c.clientPhone LIKE CONCAT('%', :search, '%')")
    Page<ClientEntity> searchClientList(@Param("search") String search, Pageable pageable);
}