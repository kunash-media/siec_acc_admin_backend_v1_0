package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.ProjectLinkRequestDto;
import com.siec_acc.dto.request.ProjectRequestDto;
import com.siec_acc.dto.response.*;
import com.siec_acc.dto.response.ProjectBudgetDto;
import com.siec_acc.dto.response.ProjectExpenseDto;
import com.siec_acc.dto.response.ProjectPurchaseDto;
import com.siec_acc.entity.ClientEntity;
import com.siec_acc.entity.ExpenseEntity;
import com.siec_acc.entity.ProjectDocumentEntity;
import com.siec_acc.entity.ProjectEntity;
import com.siec_acc.entity.ProjectLinkEntity;
import com.siec_acc.exceptions.InvalidOperationException;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.*;
import com.siec_acc.service.ProjectService;
import com.siec_acc.service.PurchaseBillService;
import com.siec_acc.utils.StrIdGenerator;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");
    // Project code = <FY end year, 2 digits> + CODE_TAG + <3-digit running no.>  e.g. 26LW073 (resets every financial year).
    private static final String CODE_TAG = "LW";
    private static final List<String> STATUS_OPTIONS = List.of("active", "pending", "onhold", "completed", "cancelled");
    private static final Set<String> STATUSES = new HashSet<>(STATUS_OPTIONS);
    private static final String LINK_INVOICE = "INVOICE";
    private static final String LINK_BILL = "PURCHASE_BILL";
    private static final Set<String> LINK_TYPES = Set.of(LINK_INVOICE, LINK_BILL);
    // Only expenses in this status count towards the project's actual cost (pending / rejected are listed but not counted).
    private static final String COST_EXPENSE_STATUS = "approved";
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ProjectRepository projectRepository;
    private final ProjectLinkRepository linkRepository;
    private final ProjectDocumentRepository documentRepository;
    private final ProjectInvoiceQueryRepository invoiceQueryRepository;
    private final ClientRepository clientRepository;
    private final PurchaseBillService purchaseBillService;
    private final ExpenseRepository expenseRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectLinkRepository linkRepository,
                              ProjectDocumentRepository documentRepository,
                              ProjectInvoiceQueryRepository invoiceQueryRepository,
                              ClientRepository clientRepository,
                              PurchaseBillService purchaseBillService,
                              ExpenseRepository expenseRepository) {
        this.projectRepository = projectRepository;
        this.linkRepository = linkRepository;
        this.documentRepository = documentRepository;
        this.invoiceQueryRepository = invoiceQueryRepository;
        this.clientRepository = clientRepository;
        this.purchaseBillService = purchaseBillService;
        this.expenseRepository = expenseRepository;
    }

    // ------------------------------------------------------------------
    // CREATE / UPDATE / DELETE
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto dto) {
        logger.info("Creating project: {}", dto.getName());
        validate(dto);

        ProjectEntity p = new ProjectEntity();
        apply(p, dto);
        p.setProjectCode(generateProjectCode());

        ProjectEntity saved = projectRepository.save(p);
        saved.setProjectStrId(StrIdGenerator.generate("PRJ", saved.getProjectPrimeId()));
        saved = projectRepository.save(saved);

        logger.info("Project created: {} ({})", saved.getProjectStrId(), saved.getProjectCode());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProject(String projectStrId, ProjectRequestDto dto) {
        logger.info("Updating project: {}", projectStrId);
        ProjectEntity p = getOrThrow(projectStrId);
        validate(dto);
        apply(p, dto);   // project code and id never change
        return toResponse(projectRepository.save(p));
    }

    // Refused while invoices / bills are linked (they would silently drop out of the project books).
    @Override
    @Transactional
    public void deleteProject(String projectStrId) {
        logger.info("Deleting project: {}", projectStrId);
        ProjectEntity p = getOrThrow(projectStrId);
        if (linkRepository.existsByProjectStrId(projectStrId) || expenseRepository.existsByExpenseProjectId(projectStrId)) {
            throw new InvalidOperationException("Cannot delete " + p.getProjectCode()
                    + ": invoices / bills / expenses are linked to it. Unlink them first.");
        }
        documentRepository.deleteByProjectStrId(projectStrId);
        projectRepository.delete(p);
    }

    // ------------------------------------------------------------------
    // READS
    // ------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProject(String projectStrId) {
        return toResponse(getOrThrow(projectStrId));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllProjects(String status, String search, Integer page, Integer size) {
        String statusFilter = (status == null || status.isBlank()) ? "all" : status.trim().toLowerCase();
        String searchFilter = search == null ? "" : search.trim();
        Specification<ProjectEntity> spec = buildSpecification(statusFilter, searchFilter);

        boolean paged = page != null || size != null;
        List<ProjectResponseDto> projects;
        int pageNo, pageSize, totalPages;
        long totalItems;

        if (paged) {
            pageNo = (page == null || page < 1) ? 1 : page;
            pageSize = (size == null || size < 1) ? DEFAULT_PAGE_SIZE : size;
            Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(Sort.Direction.DESC, "projectPrimeId"));
            Page<ProjectEntity> result = projectRepository.findAll(spec, pageable);
            projects = result.getContent().stream().map(this::toResponse).collect(Collectors.toList());
            totalItems = result.getTotalElements();
            totalPages = Math.max(1, result.getTotalPages());
        } else {
            List<ProjectEntity> all = projectRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "projectPrimeId"));
            projects = all.stream().map(this::toResponse).collect(Collectors.toList());
            totalItems = all.size();
            pageNo = 1;
            pageSize = Math.max((int) totalItems, 1);
            totalPages = 1;
        }

        Map<String, Object> pagination = new LinkedHashMap<>();
        pagination.put("paged", paged);
        pagination.put("page", pageNo);
        pagination.put("size", pageSize);
        pagination.put("totalItems", totalItems);
        pagination.put("totalPages", totalPages);
        pagination.put("hasNext", pageNo < totalPages);
        pagination.put("hasPrevious", pageNo > 1);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("projects", projects);
        response.put("pagination", pagination);
        response.put("meta", getMeta());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMeta() {
        List<ProjectEntity> all = projectRepository.findAll();
        BigDecimal totalBudget = BigDecimal.ZERO, totalProfit = BigDecimal.ZERO;
        for (ProjectEntity p : all) {
            totalBudget = totalBudget.add(nz(p.getTotalBudget()));
            Fin f = finance(p.getProjectStrId());
            totalProfit = totalProfit.add(f.revenue().subtract(f.cost()));
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("totalProjects", all.size());
        meta.put("activeProjects", all.stream().filter(p -> "active".equals(p.getStatus())).count());
        meta.put("completedProjects", all.stream().filter(p -> "completed".equals(p.getStatus())).count());
        meta.put("totalBudget", totalBudget);
        meta.put("totalProfit", totalProfit);
        meta.put("statusOptions", STATUS_OPTIONS);
        return meta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectSaleDto> getSales(String projectStrId) {
        getOrThrow(projectStrId);
        return loadSales(projectStrId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectPurchaseDto> getPurchases(String projectStrId) {
        getOrThrow(projectStrId);
        return loadPurchases(projectStrId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectExpenseDto> getExpenses(String projectStrId) {
        getOrThrow(projectStrId);
        return loadExpenses(projectStrId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectBudgetDto getBudget(String projectStrId) {
        ProjectEntity p = getOrThrow(projectStrId);
        BigDecimal cost = finance(projectStrId).cost();
        ProjectBudgetDto b = new ProjectBudgetDto();
        b.setProjectId(p.getProjectStrId());
        b.setTotalBudget(nz(p.getTotalBudget()));
        b.setPurchaseBudget(nz(p.getPurchaseBudget()));
        b.setExpenseBudget(nz(p.getExpenseBudget()));
        b.setHoursBudget(nz(p.getHoursBudget()));
        b.setActualCost(cost);
        b.setVariance(nz(p.getTotalBudget()).subtract(cost));
        b.setUsagePct(pct(cost, p.getTotalBudget()));
        return b;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDocumentDto> getDocuments(String projectStrId) {
        getOrThrow(projectStrId);
        List<ProjectDocumentDto> out = new ArrayList<>();
        for (ProjectDocumentDto d : documentRepository.findListByProject(projectStrId)) {
            d.setDownloadUrl("/api/projects/v1/download-document/" + d.getId());
            out.add(d);
        }
        // Invoice PDFs of linked invoices appear automatically.
        for (ProjectSaleDto s : loadSales(projectStrId)) {
            if (s.getPdfFileName() == null) continue;
            ProjectDocumentDto d = new ProjectDocumentDto();
            d.setId(s.getNumber());
            d.setType("Invoice");
            d.setName(s.getPdfFileName());
            d.setDate(s.getDate());
            d.setUploadedBy("System");
            d.setSource("AUTO");
            d.setDownloadUrl(s.getPdfUrl());
            out.add(d);
        }
        out.sort(Comparator.comparing(ProjectDocumentDto::getDate, Comparator.nullsLast(Comparator.reverseOrder())));
        return out;
    }

    // ------------------------------------------------------------------
    // LINKS (invoice / purchase bill -> project)
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public void linkRecord(String projectStrId, ProjectLinkRequestDto dto) {
        getOrThrow(projectStrId);
        String type = dto.getLinkType() == null ? "" : dto.getLinkType().trim().toUpperCase(Locale.ROOT);
        String ref = dto.getRefStrId() == null ? "" : dto.getRefStrId().trim();
        if (!LINK_TYPES.contains(type)) throw new InvalidOperationException("Invalid linkType. Allowed: INVOICE, PURCHASE_BILL.");
        if (ref.isEmpty()) throw new InvalidOperationException("refStrId is required.");

        if (LINK_INVOICE.equals(type)) {
            if (!invoiceQueryRepository.existsByInvoiceStrId(ref))
                throw new ResourceNotFoundException("No invoice found with ID '" + ref + "'.");
        } else {
            purchaseBillService.getPurchaseBillByStrId(ref);   // throws ResourceNotFoundException if missing
        }

        Optional<ProjectLinkEntity> existing = linkRepository.findByLinkTypeAndRefStrId(type, ref);
        if (existing.isPresent()) {
            if (existing.get().getProjectStrId().equals(projectStrId)) return;   // already linked here
            throw new InvalidOperationException(ref + " is already linked to project " + existing.get().getProjectStrId() + ". Unlink it first.");
        }
        ProjectLinkEntity link = new ProjectLinkEntity();
        link.setProjectStrId(projectStrId);
        link.setLinkType(type);
        link.setRefStrId(ref);
        linkRepository.save(link);
        logger.info("Linked {} {} to project {}", type, ref, projectStrId);
    }

    @Override
    @Transactional
    public void unlinkRecord(String projectStrId, String linkType, String refStrId) {
        getOrThrow(projectStrId);
        String type = linkType == null ? "" : linkType.trim().toUpperCase(Locale.ROOT);
        long removed = linkRepository.deleteByProjectStrIdAndLinkTypeAndRefStrId(projectStrId, type, refStrId == null ? "" : refStrId.trim());
        if (removed == 0) throw new ResourceNotFoundException("That record is not linked to project " + projectStrId + ".");
    }

    // ------------------------------------------------------------------
    // DOCUMENTS
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public ProjectDocumentDto uploadDocument(String projectStrId, String documentType, String uploadedBy,
                                             byte[] data, String contentType, String fileName) {
        getOrThrow(projectStrId);
        if (data == null || data.length == 0) throw new InvalidOperationException("The uploaded file is empty.");
        if (data.length > MAX_FILE_BYTES) throw new InvalidOperationException("File is too large (max 10 MB).");

        ProjectDocumentEntity d = new ProjectDocumentEntity();
        d.setProjectStrId(projectStrId);
        d.setDocumentType(blank(documentType) ? "Other" : documentType.trim());
        d.setFileName(blank(fileName) ? "document" : fileName.trim());
        d.setContentType(blank(contentType) ? "application/octet-stream" : contentType);
        d.setFileSize((long) data.length);
        d.setUploadedBy(blank(uploadedBy) ? "System" : uploadedBy.trim());
        d.setFileData(data);

        ProjectDocumentEntity saved = documentRepository.save(d);
        saved.setPdocStrId(StrIdGenerator.generate("PDOC", saved.getPdocPrimeId()));
        saved = documentRepository.save(saved);

        ProjectDocumentDto dto = new ProjectDocumentDto(saved.getPdocStrId(), saved.getDocumentType(), saved.getFileName(),
                saved.getFileSize(), saved.getUploadedBy(), saved.getCreatedAt());
        dto.setDownloadUrl("/api/projects/v1/download-document/" + saved.getPdocStrId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto downloadDocument(String pdocStrId) {
        ProjectDocumentEntity d = documentRepository.findByPdocStrId(pdocStrId)
                .orElseThrow(() -> new ResourceNotFoundException("No project document found with ID '" + pdocStrId + "'."));
        return FileDownloadDto.builder().data(d.getFileData()).contentType(d.getContentType()).fileName(d.getFileName()).build();
    }

    @Override
    @Transactional
    public void deleteDocument(String pdocStrId) {
        ProjectDocumentEntity d = documentRepository.findByPdocStrId(pdocStrId)
                .orElseThrow(() -> new ResourceNotFoundException("No project document found with ID '" + pdocStrId + "'."));
        documentRepository.delete(d);
    }

    // ------------------------------------------------------------------
    // EXPORT (CSV - opens in Excel)
    // ------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("Project ID,Code,Name,Customer,Project Manager,Status,Start Date,End Date,Progress %,Budget,Revenue,Actual Cost,Profit\n");
        for (ProjectEntity p : projectRepository.findAll(Sort.by("projectPrimeId"))) {
            ProjectResponseDto r = toResponse(p);
            sb.append(String.join(",",
                    csv(r.getId()), csv(r.getCode()), csv(r.getName()), csv(r.getCustomer()), csv(r.getPm()), csv(r.getStatus()),
                    csv(r.getStartDate()), csv(r.getEndDate()), csv(r.getProgress()),
                    csv(r.getBudget()), csv(r.getRevenue()), csv(r.getActualCost()), csv(r.getProfit()))).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ==================================================================
    // helpers
    // ==================================================================
    private record Fin(List<ProjectSaleDto> sales, List<ProjectPurchaseDto> purchases, List<ProjectExpenseDto> expenses,
                       BigDecimal revenue, BigDecimal cost) {}

    private Fin finance(String pid) {
        List<ProjectSaleDto> sales = loadSales(pid);
        List<ProjectPurchaseDto> purchases = loadPurchases(pid);
        List<ProjectExpenseDto> expenses = loadExpenses(pid);
        BigDecimal revenue = sales.stream().map(ProjectSaleDto::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cost = purchases.stream().map(ProjectPurchaseDto::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(expenses.stream().filter(e -> COST_EXPENSE_STATUS.equalsIgnoreCase(e.getStatus()))
                        .map(ProjectExpenseDto::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return new Fin(sales, purchases, expenses, revenue, cost);
    }

    private List<ProjectSaleDto> loadSales(String pid) {
        List<String> ids = linkRepository.findByProjectStrIdAndLinkType(pid, LINK_INVOICE).stream()
                .map(ProjectLinkEntity::getRefStrId).collect(Collectors.toList());
        return ids.isEmpty() ? new ArrayList<>() : invoiceQueryRepository.findSalesByStrIds(ids);
    }

    private List<ProjectPurchaseDto> loadPurchases(String pid) {
        List<ProjectPurchaseDto> out = new ArrayList<>();
        for (ProjectLinkEntity l : linkRepository.findByProjectStrIdAndLinkType(pid, LINK_BILL)) {
            try {
                PurchaseBillResponseDto b = purchaseBillService.getPurchaseBillByStrId(l.getRefStrId());
                ProjectPurchaseDto row = new ProjectPurchaseDto(b.getPbNumber(), b.getBillDate(), b.getVendorName(),
                        bd(b.getAmount()), bd(b.getPaidAmount()), b.getStatus());
                row.setRefId(l.getRefStrId());
                out.add(row);
            } catch (ResourceNotFoundException ex) {
                logger.warn("Linked purchase bill {} no longer exists (project {})", l.getRefStrId(), pid);
            }
        }
        out.sort(Comparator.comparing(ProjectPurchaseDto::getDate, Comparator.nullsLast(Comparator.reverseOrder())));
        return out;
    }

    // Expenses are tied to a project through ExpenseEntity.expenseProjectId (= project's projectStrId, e.g. PRJ-001).
    // Amount shown = expense amount + its tax %, same GST-inclusive basis as invoices and bills.
    private List<ProjectExpenseDto> loadExpenses(String pid) {
        List<ProjectExpenseDto> out = new ArrayList<>();
        for (ExpenseEntity e : expenseRepository.findByExpenseProjectIdOrderByExpenseDateDesc(pid)) {
            BigDecimal amount = nz(e.getExpenseAmount());
            BigDecimal tax = amount.multiply(nz(e.getExpenseTax())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            out.add(new ProjectExpenseDto(e.getExpenseDate(), e.getExpenseCategory(), e.getExpenseVendor(),
                    amount.add(tax), e.getExpenseStatus()));
        }
        return out;
    }

    private ProjectResponseDto toResponse(ProjectEntity p) {
        Fin f = finance(p.getProjectStrId());
        BigDecimal profit = f.revenue().subtract(f.cost());
        ProjectResponseDto r = new ProjectResponseDto();
        r.setId(p.getProjectStrId());
        r.setPrimeId(p.getProjectPrimeId());
        r.setCode(p.getProjectCode());
        r.setName(p.getProjectName());
        r.setClientStrId(p.getClientStrId());
        r.setCustomer(p.getCustomerName());
        r.setPm(p.getProjectManager());
        r.setStatus(p.getStatus());
        r.setStartDate(p.getStartDate());
        r.setEndDate(p.getEndDate());
        r.setProgress(p.getProgress());
        r.setBudget(nz(p.getTotalBudget()));
        r.setPurchaseBudget(nz(p.getPurchaseBudget()));
        r.setExpenseBudget(nz(p.getExpenseBudget()));
        r.setHoursBudget(nz(p.getHoursBudget()));
        r.setDescription(p.getDescription());
        r.setHoldReason(p.getHoldReason());
        r.setRevenue(f.revenue());
        r.setActualCost(f.cost());
        r.setProfit(profit);
        r.setMargin(pct(profit, f.revenue()));
        r.setBudgetVariance(nz(p.getTotalBudget()).subtract(f.cost()));
        r.setBudgetUsagePct(pct(f.cost(), p.getTotalBudget()));
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }

    private void validate(ProjectRequestDto dto) {
        if (blank(dto.getName())) throw new InvalidOperationException("Project name is required.");
        if (blank(dto.getClientStrId()) && blank(dto.getCustomer())) throw new InvalidOperationException("Customer is required.");
        String status = blank(dto.getStatus()) ? "active" : dto.getStatus().trim().toLowerCase(Locale.ROOT);
        if (!STATUSES.contains(status)) throw new InvalidOperationException("Invalid status '" + dto.getStatus() + "'. Allowed: " + STATUS_OPTIONS + ".");
        if ("onhold".equals(status) && blank(dto.getHoldReason())) throw new InvalidOperationException("Hold reason is required when the project is On Hold.");
        if (dto.getProgress() != null && (dto.getProgress() < 0 || dto.getProgress() > 100)) throw new InvalidOperationException("Progress must be between 0 and 100.");
        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate()))
            throw new InvalidOperationException("End date cannot be before the start date.");
        BigDecimal total = nz(dto.getBudget());
        BigDecimal parts = nz(dto.getPurchaseBudget()).add(nz(dto.getExpenseBudget())).add(nz(dto.getHoursBudget()));
        if (total.signum() < 0 || nz(dto.getPurchaseBudget()).signum() < 0 || nz(dto.getExpenseBudget()).signum() < 0 || nz(dto.getHoursBudget()).signum() < 0)
            throw new InvalidOperationException("Budget amounts cannot be negative.");
        if (parts.compareTo(total) > 0) throw new InvalidOperationException("Purchase + expense + hours budget (" + parts + ") cannot exceed the total budget (" + total + ").");
    }

    private void apply(ProjectEntity p, ProjectRequestDto dto) {
        String status = blank(dto.getStatus()) ? "active" : dto.getStatus().trim().toLowerCase(Locale.ROOT);
        p.setProjectName(dto.getName().trim());
        p.setProjectManager(blank(dto.getPm()) ? null : dto.getPm().trim());
        p.setStatus(status);
        p.setStartDate(dto.getStartDate());
        p.setEndDate(dto.getEndDate());
        if (dto.getProgress() != null) p.setProgress(dto.getProgress());
        p.setTotalBudget(nz(dto.getBudget()));
        p.setPurchaseBudget(nz(dto.getPurchaseBudget()));
        p.setExpenseBudget(nz(dto.getExpenseBudget()));
        p.setHoursBudget(nz(dto.getHoursBudget()));
        p.setDescription(blank(dto.getDescription()) ? null : dto.getDescription().trim());
        p.setHoldReason("onhold".equals(status) ? dto.getHoldReason().trim() : null);

        if (!blank(dto.getClientStrId())) {
            ClientEntity c = clientRepository.findByClientStrId(dto.getClientStrId().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("No customer found with ID '" + dto.getClientStrId() + "'."));
            p.setClientStrId(c.getClientStrId());
            p.setCustomerName(!blank(c.getClientCompanyName()) ? c.getClientCompanyName() : c.getClientName());
        } else {
            p.setClientStrId(null);
            p.setCustomerName(dto.getCustomer().trim());
        }
    }

    /** <FY end year 2 digits><CODE_TAG><3-digit running no>, e.g. 26LW073. The unique DB constraint guards against races. */
    private String generateProjectCode() {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        int fyEnd = today.getMonthValue() >= 4 ? today.getYear() + 1 : today.getYear();
        String prefix = String.format("%02d", fyEnd % 100) + CODE_TAG;
        int next = projectRepository.findTopByProjectCodeStartingWithOrderByProjectCodeDesc(prefix)
                .map(p -> {
                    try { return Integer.parseInt(p.getProjectCode().substring(prefix.length())) + 1; }
                    catch (NumberFormatException e) { return 1; }
                }).orElse(1);
        return prefix + String.format("%03d", next);
    }

    private Specification<ProjectEntity> buildSpecification(String statusFilter, String searchFilter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!"all".equals(statusFilter)) predicates.add(cb.equal(cb.lower(root.get("status")), statusFilter));
            if (!searchFilter.isBlank()) {
                String like = "%" + searchFilter.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("projectStrId")), like),
                        cb.like(cb.lower(root.get("projectCode")), like),
                        cb.like(cb.lower(root.get("projectName")), like),
                        cb.like(cb.lower(root.get("customerName")), like),
                        cb.like(cb.lower(root.get("projectManager")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ProjectEntity getOrThrow(String projectStrId) {
        return projectRepository.findByProjectStrId(projectStrId)
                .orElseThrow(() -> {
                    logger.warn("Project not found: {}", projectStrId);
                    return new ResourceNotFoundException("No project found with ID '" + projectStrId + "'.");
                });
    }

    private BigDecimal pct(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.signum() <= 0) return BigDecimal.ZERO;
        return part.multiply(BigDecimal.valueOf(100)).divide(whole, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private BigDecimal bd(Double v) { return v == null ? BigDecimal.ZERO : BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP); }

    private boolean blank(String s) { return s == null || s.isBlank(); }

    private String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}