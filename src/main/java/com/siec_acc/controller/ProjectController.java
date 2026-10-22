package com.siec_acc.controller;

import com.siec_acc.dto.request.ProjectLinkRequestDto;
import com.siec_acc.dto.request.ProjectRequestDto;
import com.siec_acc.dto.response.*;
import com.siec_acc.dto.response.ProjectBudgetDto;
import com.siec_acc.dto.response.ProjectExpenseDto;
import com.siec_acc.dto.response.ProjectPurchaseDto;
import com.siec_acc.exceptions.ApiResponse;
import com.siec_acc.exceptions.FileProcessingException;
import com.siec_acc.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/v1")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create-project")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> createProject(@RequestBody ProjectRequestDto dto) {
        logger.info("API HIT: POST /create-project | name={}", dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully.", projectService.createProject(dto)));
    }

    @PutMapping("/update-project/{projectStrId}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateProject(@PathVariable String projectStrId,
                                                                         @RequestBody ProjectRequestDto dto) {
        logger.info("API HIT: PUT /update-project/{}", projectStrId);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully.", projectService.updateProject(projectStrId, dto)));
    }

    @DeleteMapping("/delete-project/{projectStrId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable String projectStrId) {
        logger.info("API HIT: DELETE /delete-project/{}", projectStrId);
        projectService.deleteProject(projectStrId);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully.", null));
    }

    @GetMapping("/get-project/{projectStrId}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProject(@PathVariable String projectStrId) {
        return ResponseEntity.ok(ApiResponse.success("Project fetched successfully.", projectService.getProject(projectStrId)));
    }

    // List + stat cards. status = all|active|pending|onhold|completed|cancelled; page is 1-based and optional.
    @GetMapping("/get-all-projects")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllProjects(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        logger.info("API HIT: GET /get-all-projects | status={} search={} page={} size={}", status, search, page, size);
        return ResponseEntity.ok(ApiResponse.success("Projects fetched successfully.", projectService.getAllProjects(status, search, page, size)));
    }

    @GetMapping("/get-meta")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMeta() {
        return ResponseEntity.ok(ApiResponse.success("Meta fetched successfully.", projectService.getMeta()));
    }

    // ---- detail tabs ----
    @GetMapping("/get-sales/{projectStrId}")
    public ResponseEntity<ApiResponse<List<ProjectSaleDto>>> getSales(@PathVariable String projectStrId) {
        return ResponseEntity.ok(ApiResponse.success("Project sales fetched successfully.", projectService.getSales(projectStrId)));
    }

    @GetMapping("/get-purchases/{projectStrId}")
    public ResponseEntity<ApiResponse<List<ProjectPurchaseDto>>> getPurchases(@PathVariable String projectStrId) {
        return ResponseEntity.ok(ApiResponse.success("Project purchases fetched successfully.", projectService.getPurchases(projectStrId)));
    }

    @GetMapping("/get-expenses/{projectStrId}")
    public ResponseEntity<ApiResponse<List<ProjectExpenseDto>>> getExpenses(@PathVariable String projectStrId) {
        return ResponseEntity.ok(ApiResponse.success("Project expenses fetched successfully.", projectService.getExpenses(projectStrId)));
    }

    @GetMapping("/get-budget/{projectStrId}")
    public ResponseEntity<ApiResponse<ProjectBudgetDto>> getBudget(@PathVariable String projectStrId) {
        return ResponseEntity.ok(ApiResponse.success("Project budget fetched successfully.", projectService.getBudget(projectStrId)));
    }

    @GetMapping("/get-documents/{projectStrId}")
    public ResponseEntity<ApiResponse<List<ProjectDocumentDto>>> getDocuments(@PathVariable String projectStrId) {
        return ResponseEntity.ok(ApiResponse.success("Project documents fetched successfully.", projectService.getDocuments(projectStrId)));
    }

    // ---- link / unlink an existing invoice or purchase bill ----
    @PostMapping("/link-record/{projectStrId}")
    public ResponseEntity<ApiResponse<Void>> linkRecord(@PathVariable String projectStrId, @RequestBody ProjectLinkRequestDto dto) {
        logger.info("API HIT: POST /link-record/{} | {} {}", projectStrId, dto.getLinkType(), dto.getRefStrId());
        projectService.linkRecord(projectStrId, dto);
        return ResponseEntity.ok(ApiResponse.success("Record linked to project.", null));
    }

    @DeleteMapping("/unlink-record/{projectStrId}")
    public ResponseEntity<ApiResponse<Void>> unlinkRecord(@PathVariable String projectStrId,
                                                          @RequestParam String linkType, @RequestParam String refStrId) {
        logger.info("API HIT: DELETE /unlink-record/{} | {} {}", projectStrId, linkType, refStrId);
        projectService.unlinkRecord(projectStrId, linkType, refStrId);
        return ResponseEntity.ok(ApiResponse.success("Record unlinked from project.", null));
    }

    // ---- documents ----
    @PostMapping(value = "/upload-document/{projectStrId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProjectDocumentDto>> uploadDocument(
            @PathVariable String projectStrId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String uploadedBy) {
        logger.info("API HIT: POST /upload-document/{} | file={}", projectStrId, file.getOriginalFilename());
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            logger.error("Failed to read uploaded file for project {}", projectStrId, ex);
            throw new FileProcessingException("We couldn't read the uploaded file. Please try again.");
        }
        ProjectDocumentDto dto = projectService.uploadDocument(projectStrId, documentType, uploadedBy,
                bytes, file.getContentType(), file.getOriginalFilename());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Document uploaded successfully.", dto));
    }

    @GetMapping("/download-document/{pdocStrId}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String pdocStrId) {
        var file = projectService.downloadDocument(pdocStrId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(file.getData());
    }

    @DeleteMapping("/delete-document/{pdocStrId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable String pdocStrId) {
        logger.info("API HIT: DELETE /delete-document/{}", pdocStrId);
        projectService.deleteDocument(pdocStrId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully.", null));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"projects.csv\"")
                .body(projectService.exportCsv());
    }

    // GET /api/projects/v1/project-list?pageNumber=1&pageSize=25&status=active&search=odor
    @GetMapping("/project-list")
    public ResponseEntity<PagedResponseDto<ProjectSummaryResponseDto>> getProjectSummaries(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "25") int pageSize) {
        return ResponseEntity.ok(projectService.getProjectSummaries(status, search, pageNumber, pageSize));
    }
}