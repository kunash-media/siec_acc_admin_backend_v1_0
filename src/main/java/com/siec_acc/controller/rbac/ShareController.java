package com.siec_acc.controller.rbac;


import com.siec_acc.dto.response.FileDownloadDto;
import com.siec_acc.service.*;
import com.siec_acc.service.rbac.ShareLinkService;
import com.siec_acc.service.rbac.ShareLinkService.DocType;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ShareController {

    private final ShareLinkService shareLinkService;
    private final QuoteService quoteService;
    private final InvoiceService invoiceService;
    private final ProformaService proformaService;
    private final DeliveryChallanService dcService;

    public ShareController(ShareLinkService shareLinkService, QuoteService quoteService,
                           InvoiceService invoiceService, ProformaService proformaService,
                           DeliveryChallanService dcService) {
        this.shareLinkService = shareLinkService;
        this.quoteService = quoteService;
        this.invoiceService = invoiceService;
        this.proformaService = proformaService;
        this.dcService = dcService;
    }

    // Authenticated (falls under /api/** rule). Frontend calls this instead of API_BASE + pdfUrl.
    // GET /api/v1/share/link?type=INVOICE&id=INV-25-26-001
    @GetMapping("/api/v1/share/link")
    public ResponseEntity<?> mint(@RequestParam String type, @RequestParam String id) {
        DocType t = parse(type);
        if (t == null || id.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid type or id"));
        return ResponseEntity.ok(Map.of("url", shareLinkService.buildUrl(t, id.trim())));
    }

    // Public. Only reachable with a valid, unexpired signature.
    @GetMapping("/api/public/share/{type}/{id:.+}")
    public ResponseEntity<?> open(@PathVariable String type, @PathVariable String id,
                                  @RequestParam long exp, @RequestParam String sig) {
        DocType t = parse(type);
        if (t == null || !shareLinkService.verify(t, id, exp, sig))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This link is invalid or has expired."));

        FileDownloadDto file = switch (t) {
            case QUOTE -> quoteService.downloadQuotePdf(id);
            case INVOICE -> invoiceService.downloadInvoicePdf(id);
            case PROFORMA -> proformaService.downloadProformaPdf(id);
            case DC -> dcService.downloadDcPdf(id);
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(file.getData());
    }

    private static DocType parse(String s) {
        try { return DocType.valueOf(s.trim().toUpperCase()); }
        catch (Exception e) { return null; }
    }
}