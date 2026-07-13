package com.krishibridge.service;

import com.krishibridge.entity.TransporterDocument;
import com.krishibridge.entity.User;
import com.krishibridge.enums.DocumentType;
import com.krishibridge.enums.UserStatus;
import com.krishibridge.enums.VerificationStatus;
import com.krishibridge.exception.ComplianceException;
import com.krishibridge.exception.DocumentNotFoundException;
import com.krishibridge.repository.TransporterDocumentRepository;
import com.krishibridge.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ComplianceService {

    private static final Logger log = LoggerFactory.getLogger(ComplianceService.class);

    private final TransporterDocumentRepository documentRepository;
    private final UserRepository userRepository;

    public ComplianceService(TransporterDocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransporterDocument uploadDocument(Long transporterId, DocumentType documentType, MultipartFile file) {
        User transporter = userRepository.findById(transporterId)
                .orElseThrow(() -> new ComplianceException("Transporter not found"));

        if (transporter.getStatus() == UserStatus.BLOCKED) {
            throw new ComplianceException("Account is blocked. Compliance uploads disabled.");
        }

        if (file.isEmpty()) {
            throw new ComplianceException("Uploaded file cannot be empty");
        }

        // Production-grade S3/Object storage simulation URL
        String filename = transporterId + "_" + documentType.name().toLowerCase() + "_" + System.currentTimeMillis() + ".pdf";
        String documentUrl = "https://krishibridge-docs.s3.ap-south-1.amazonaws.com/transporters/" + transporterId + "/" + filename;

        // Check if document of this type was already uploaded. If yes, replace it.
        Optional<TransporterDocument> existingDocOpt = documentRepository.findByTransporterIdAndDocumentType(transporterId, documentType);
        
        TransporterDocument document;
        if (existingDocOpt.isPresent()) {
            document = existingDocOpt.get();
            document.setDocumentUrl(documentUrl);
            document.setVerificationStatus(VerificationStatus.PENDING);
        } else {
            document = new TransporterDocument(transporterId, documentType, documentUrl);
        }

        TransporterDocument savedDoc = documentRepository.save(document);

        // Audit Log Hook: Document Uploaded
        log.info("AUDIT_LOG | DOCUMENT_UPLOADED | TransporterId: {} | DocType: {} | Status: PENDING", transporterId, documentType);

        return savedDoc;
    }

    @Transactional(readOnly = true)
    public List<TransporterDocument> getTransporterDocumentsStatus(Long transporterId) {
        return documentRepository.findByTransporterId(transporterId);
    }

    @Transactional(readOnly = true)
    public List<TransporterDocument> getPendingDocuments() {
        return documentRepository.findByVerificationStatus(VerificationStatus.PENDING);
    }

    @Transactional
    public TransporterDocument approveDocument(Long documentId) {
        TransporterDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document with ID " + documentId + " not found"));

        if (document.getVerificationStatus() == VerificationStatus.APPROVED) {
            return document;
        }

        document.setVerificationStatus(VerificationStatus.APPROVED);
        TransporterDocument approvedDoc = documentRepository.save(document);

        // Audit Log Hook: Document Approved
        log.info("AUDIT_LOG | DOCUMENT_APPROVED | DocId: {} | TransporterId: {}", documentId, document.getTransporterId());

        // Check if all 5 required document types are approved for this transporter
        checkAndActivateTransporter(document.getTransporterId());

        return approvedDoc;
    }

    @Transactional
    public TransporterDocument rejectDocument(Long documentId) {
        TransporterDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document with ID " + documentId + " not found"));

        document.setVerificationStatus(VerificationStatus.REJECTED);
        TransporterDocument rejectedDoc = documentRepository.save(document);

        // Audit Log Hook: Document Rejected
        log.warn("AUDIT_LOG | DOCUMENT_REJECTED | DocId: {} | TransporterId: {}", documentId, document.getTransporterId());

        // Revert user back to PENDING status if they were somehow approved
        Optional<User> userOpt = userRepository.findById(document.getTransporterId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getStatus() == UserStatus.APPROVED) {
                user.setStatus(UserStatus.PENDING);
                userRepository.save(user);
                log.info("AUDIT_LOG | TRANSPORTER_DEACTIVATED | TransporterId: {} | Reason: Rejected document", user.getId());
            }
        }

        return rejectedDoc;
    }

    private void checkAndActivateTransporter(Long transporterId) {
        long approvedCount = documentRepository.countByTransporterIdAndVerificationStatus(transporterId, VerificationStatus.APPROVED);

        if (approvedCount == 5) {
            User transporter = userRepository.findById(transporterId)
                    .orElseThrow(() -> new ComplianceException("Transporter not found"));

            if (transporter.getStatus() == UserStatus.PENDING) {
                transporter.setStatus(UserStatus.APPROVED);
                userRepository.save(transporter);
                
                // Audit Log Hook: Transporter Approved & Onboarded
                log.info("AUDIT_LOG | TRANSPORTER_APPROVED | TransporterId: {} | Status: APPROVED", transporterId);
            }
        }
    }
}
