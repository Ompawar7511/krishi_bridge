package com.krishibridge.dto.response;

import com.krishibridge.enums.DocumentType;
import com.krishibridge.enums.VerificationStatus;
import java.time.LocalDateTime;

public class DocumentResponse {
    private Long id;
    private Long transporterId;
    private DocumentType documentType;
    private String documentUrl;
    private VerificationStatus verificationStatus;
    private LocalDateTime uploadedAt;

    public DocumentResponse() {}

    public DocumentResponse(Long id, Long transporterId, DocumentType documentType, String documentUrl, VerificationStatus verificationStatus, LocalDateTime uploadedAt) {
        this.id = id;
        this.transporterId = transporterId;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
        this.verificationStatus = verificationStatus;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(Long transporterId) {
        this.transporterId = transporterId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
