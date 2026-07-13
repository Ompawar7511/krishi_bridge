package com.krishibridge.exception;

import com.krishibridge.dto.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class VehicleExceptionHandler {

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<StandardResponse<Void>> handleVehicleNotFound(VehicleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(StandardResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(ScheduleOverlapException.class)
    public ResponseEntity<StandardResponse<Void>> handleScheduleOverlap(ScheduleOverlapException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(StandardResponse.failure(ex.getMessage()));
    }
}
