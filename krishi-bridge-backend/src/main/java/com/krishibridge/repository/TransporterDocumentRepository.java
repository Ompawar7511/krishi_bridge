package com.krishibridge.repository;

import com.krishibridge.entity.TransporterDocument;
import com.krishibridge.enums.DocumentType;
import com.krishibridge.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransporterDocumentRepository extends JpaRepository<TransporterDocument, Long> {
    List<TransporterDocument> findByTransporterId(Long transporterId);
    List<TransporterDocument> findByVerificationStatus(VerificationStatus status);
    Optional<TransporterDocument> findByTransporterIdAndDocumentType(Long transporterId, DocumentType documentType);
    long countByTransporterIdAndVerificationStatus(Long transporterId, VerificationStatus status);
}
