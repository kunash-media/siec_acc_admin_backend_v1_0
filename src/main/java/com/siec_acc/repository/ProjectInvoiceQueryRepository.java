package com.siec_acc.repository;


import com.siec_acc.dto.response.ProjectSaleDto;
import com.siec_acc.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/** Read-only queries on invoices for the Projects module (separate from InvoiceRepository so that file stays untouched). */
@Repository
public interface ProjectInvoiceQueryRepository extends JpaRepository<InvoiceEntity, Long> {

    boolean existsByInvoiceStrId(String invoiceStrId);

    // Projection - avoids loading the PDF blobs.
    @Query("select new com.siec_acc.dto.response.ProjectSaleDto(i.invoiceStrId, i.invoiceDate, i.invoiceCustomerName, " +
            "i.invoiceTotalAmountInr, i.invoicePaidAmount, i.invoiceStatus, i.invoicePdfFileName, i.invoicePdfUrl) " +
            "from InvoiceEntity i where i.invoiceStrId in :ids order by i.invoiceDate desc")
    List<ProjectSaleDto> findSalesByStrIds(@Param("ids") Collection<String> ids);
}