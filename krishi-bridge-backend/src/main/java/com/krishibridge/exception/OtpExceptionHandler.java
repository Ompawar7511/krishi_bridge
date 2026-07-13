package com.krishibridge.exception;

import com.krishibridge.dto.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OtpExceptionHandler {

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<StandardResponse<Void>> handleOtpExpired(OtpExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(StandardResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(OtpMaxAttemptsReachedException.class)
    public ResponseEntity<StandardResponse<Void>> handleOtpMaxAttempts(OtpMaxAttemptsReachedException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(StandardResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<StandardResponse<Void>> handleOtpInvalid(OtpInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(StandardResponse.failure(ex.getMessage()));
    }
}
