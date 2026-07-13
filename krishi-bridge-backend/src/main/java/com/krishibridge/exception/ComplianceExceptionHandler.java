package com.krishibridge.exception;

import com.krishibridge.dto.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ComplianceExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleDocumentNotFound(DocumentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(ComplianceException.class)
    public ResponseEntity<StandardResponse<Void>> handleComplianceException(ComplianceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.failure(ex.getMessage()));
    }
}
