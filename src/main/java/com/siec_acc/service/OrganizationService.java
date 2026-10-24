package com.siec_acc.service;

import com.siec_acc.dto.request.OrganizationRequestDTO;
import com.siec_acc.dto.response.OrganizationResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface OrganizationService {

    OrganizationResponseDTO createOrganization(OrganizationRequestDTO request);
    OrganizationResponseDTO getOrganization(String organizationId);
    List<OrganizationResponseDTO> getAllOrganizations();
    OrganizationResponseDTO updateOrganization(String organizationId, OrganizationRequestDTO request);
    OrganizationResponseDTO patchOrganization(String organizationId, OrganizationRequestDTO request);
    void deleteOrganization(String organizationId);
    ResponseEntity<byte[]> getOrganizationLogo(String organizationId);
    OrganizationResponseDTO uploadDocument(String organizationId, String documentName, MultipartFile file);
    List<Map<String, Object>> getDocuments(String organizationId);
    ResponseEntity<byte[]> getDocumentContent(String organizationId, String documentId);
    void deleteDocument(String organizationId, String documentId);

    OrganizationResponseDTO getOrganization();


}