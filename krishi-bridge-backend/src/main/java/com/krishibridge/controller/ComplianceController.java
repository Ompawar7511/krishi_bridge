package com.krishibridge.controller;

import com.krishibridge.dto.response.DocumentResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.entity.TransporterDocument;
import com.krishibridge.enums.DocumentType;
import com.krishibridge.service.ComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @PostMapping("/transporter/documents/upload")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<StandardResponse<DocumentResponse>> uploadDocument(
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam("file") MultipartFile file) {
        
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        TransporterDocument document = complianceService.uploadDocument(transporterId, documentType, file);
        
        DocumentResponse responseDto = mapToDto(document);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Document uploaded successfully"));
    }

    @GetMapping("/transporter/documents/status")
    @PreAuthorize("hasRole('TRANSPORTER')")
    public ResponseEntity<StandardResponse<List<DocumentResponse>>> getDocumentsStatus() {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<TransporterDocument> documents = complianceService.getTransporterDocumentsStatus(transporterId);
        
        List<DocumentResponse> responseList = documents.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    @GetMapping("/admin/documents/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<List<DocumentResponse>>> getPendingDocuments() {
        List<TransporterDocument> documents = complianceService.getPendingDocuments();
        
        List<DocumentResponse> responseList = documents.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    @PutMapping("/admin/documents/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<DocumentResponse>> approveDocument(@PathVariable("id") Long id) {
        TransporterDocument document = complianceService.approveDocument(id);
        DocumentResponse responseDto = mapToDto(document);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Document approved successfully"));
    }

    @PutMapping("/admin/documents/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<DocumentResponse>> rejectDocument(@PathVariable("id") Long id) {
        TransporterDocument document = complianceService.rejectDocument(id);
        DocumentResponse responseDto = mapToDto(document);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Document rejected successfully"));
    }

    private DocumentResponse mapToDto(TransporterDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getTransporterId(),
                document.getDocumentType(),
                document.getDocumentUrl(),
                document.getVerificationStatus(),
                document.getUploadedAt()
        );
    }
}
