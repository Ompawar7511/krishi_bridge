package com.krishibridge.exception;

import com.krishibridge.dto.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RatingExceptionHandler {

    @ExceptionHandler(RatingNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleRatingNotFound(RatingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.failure(ex.getMessage()));
    }
}
