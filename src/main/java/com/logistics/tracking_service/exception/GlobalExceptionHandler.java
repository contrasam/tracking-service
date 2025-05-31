package com.logistics.tracking_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @ExceptionHandler(TrackingServiceException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleTrackingServiceException(TrackingServiceException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("errorCode", ex.getErrorCode());
        
        HttpStatus status;
        
        // Determine appropriate HTTP status based on error code
        switch (ex.getErrorCode()) {
            case TrackingServiceException.ErrorCodes.INVALID_SHIPPER_CODE:
            case TrackingServiceException.ErrorCodes.INVALID_VERSION:
                status = HttpStatus.BAD_REQUEST;
                break;
            case TrackingServiceException.ErrorCodes.DATABASE_ERROR:
            case TrackingServiceException.ErrorCodes.SEQUENCE_ERROR:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        return Mono.just(new ResponseEntity<>(errorResponse, status));
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
        errorResponse.put("message", "An unexpected error occurred");
        errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
        errorResponse.put("details", ex.getMessage());
        
        return Mono.just(errorResponse);
    }
}
