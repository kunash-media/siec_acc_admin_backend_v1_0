package com.siec_acc.repository;

import com.siec_acc.entity.CustomerPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPaymentEntity, Long> {
    Optional<CustomerPaymentEntity> findByPaymentId(String paymentId);
    Optional<CustomerPaymentEntity> findByReceiptNumber(String receiptNumber);
    List<CustomerPaymentEntity> findAllByOrderByPaymentDateDescPaymentPrimeIdDesc();
    boolean existsByReceiptNumberIgnoreCase(String receiptNumber);
    List<CustomerPaymentEntity> findAllByOrganization_OrganizationIdOrderByPaymentDateDescPaymentPrimeIdDesc(String organizationId);
    List<CustomerPaymentEntity> findAllByOrganization_OrganizationIdAndClientStrIdOrderByPaymentDateDescPaymentPrimeIdDesc(String organizationId, String clientStrId);
    List<CustomerPaymentEntity> findAllByInvoicePrimeId(Long invoicePrimeId);
}