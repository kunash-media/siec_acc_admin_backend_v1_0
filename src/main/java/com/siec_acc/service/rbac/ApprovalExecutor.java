package com.siec_acc.service.rbac;

import com.siec_acc.service.*;
import org.springframework.stereotype.Component;

@Component
public class ApprovalExecutor {

    private final QuoteService quoteService;
    private final InvoiceService invoiceService;
    private final SalesOrderService salesOrderService;
    private final ProformaService proformaService;
    private final DeliveryChallanService deliveryChallanService;

    public ApprovalExecutor(QuoteService quoteService, InvoiceService invoiceService,
                            SalesOrderService salesOrderService, ProformaService proformaService,
                            DeliveryChallanService deliveryChallanService) {
        this.quoteService = quoteService;
        this.invoiceService = invoiceService;
        this.salesOrderService = salesOrderService;
        this.proformaService = proformaService;
        this.deliveryChallanService = deliveryChallanService;
    }

    public void delete(String entityType, String entityId) {
        switch (entityType) {
            case "QUOTE" -> quoteService.deleteQuote(Long.valueOf(entityId));
            case "INVOICE" -> invoiceService.deleteInvoice(Long.valueOf(entityId));
            case "SALES_ORDER" -> salesOrderService.deleteSalesOrder(entityId);
            case "PROFORMA" -> proformaService.deleteProforma(Long.valueOf(entityId));
            case "DELIVERY_CHALLAN" -> deliveryChallanService.deleteDc(Long.valueOf(entityId));
            default -> throw new IllegalArgumentException("Delete is not supported for " + entityType);
        }
    }
}