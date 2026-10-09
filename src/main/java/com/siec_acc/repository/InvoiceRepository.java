package com.siec_acc.repository;

import com.siec_acc.dto.response.InvoiceSummaryResponseDto;
import com.siec_acc.entity.InvoiceEntity;
import com.siec_acc.enum_status.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

    Optional<InvoiceEntity> findByInvoiceStrId(String invoiceStrId);

    boolean existsByInvoiceStrId(String invoiceStrId);

    @Query("select i from InvoiceEntity i where " +
           "(:status is null or i.invoiceStatus = :status) and " +
           "(:customerId is null or i.invoiceCustomerId = :customerId) and " +
           "(:search is null or lower(i.invoiceStrId) like lower(concat('%', :search, '%')) " +
           "  or lower(i.invoiceCustomerName) like lower(concat('%', :search, '%')))")
    Page<InvoiceEntity> search(@Param("status") InvoiceStatus status,
                                @Param("customerId") Long customerId,
                                @Param("search") String search,
                                Pageable pageable);

    long countByInvoiceStatus(InvoiceStatus status);

    long countByInvoiceStatusAndInvoiceDueDateBefore(InvoiceStatus status, LocalDate date);

    @Query("select coalesce(sum(i.invoiceTotalAmountInr), 0) from InvoiceEntity i")
    BigDecimal sumTotalValueInr();

    long countByInvoiceCustomerId(Long customerId);

    @Query(value = "select new com.siec_acc.dto.response.InvoiceSummaryResponseDto(" +
            "i.invoicePrimeId, i.invoiceStrId, i.invoiceDate, i.invoiceDueDate, " +
            "i.invoiceTotalAmountInr, i.invoicePaidAmount) " +
            "from InvoiceEntity i where " +
            "(:search is null or lower(i.invoiceStrId) like lower(concat('%', :search, '%')) " +
            "  or lower(i.invoiceCustomerName) like lower(concat('%', :search, '%'))) " +
            "order by i.invoiceCreatedAt desc",
            countQuery = "select count(i) from InvoiceEntity i where " +
                    "(:search is null or lower(i.invoiceStrId) like lower(concat('%', :search, '%')) " +
                    "  or lower(i.invoiceCustomerName) like lower(concat('%', :search, '%')))")
    Page<InvoiceSummaryResponseDto> searchInvoiceSummaries(@Param("search") String search, Pageable pageable);
}
