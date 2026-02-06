package com.jcuadrado.erplitebackend.infrastructure.in.api.common;

import com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeConstraintException;
import com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.documenttype.exception.DuplicateCodeException;
import com.jcuadrado.erplitebackend.domain.documenttype.exception.InvalidDocumentFormatException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle document type not found exceptions.
     */
    @ExceptionHandler(DocumentTypeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentTypeNotFoundException(
            DocumentTypeNotFoundException ex) {
        log.warn("Document type not found: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .error("RESOURCE_NOT_FOUND")
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handle duplicate code exceptions.
     */
    @ExceptionHandler(DuplicateCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCodeException(
            DuplicateCodeException ex) {
        log.warn("Duplicate code detected: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .error("DUPLICATE_CODE")
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handle invalid document format exceptions.
     */
    @ExceptionHandler(InvalidDocumentFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDocumentFormatException(
            InvalidDocumentFormatException ex) {
        log.warn("Invalid document format: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .error("VALIDATION_ERROR")
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle constraint exceptions (business rule violations).
     */
    @ExceptionHandler(DocumentTypeConstraintException.class)
    public ResponseEntity<ErrorResponse> handleDocumentTypeConstraintException(
            DocumentTypeConstraintException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .error("BUSINESS_RULE_VIOLATION")
            .build();
        return ResponseEntity.status(422).body(error);
    }
    
    /**
     * Handle validation exceptions (Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        // Concatenate all validation error messages with semicolon
        String message = ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                return fieldName + ": " + errorMessage;
            })
            .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", message);

        ErrorResponse error = ErrorResponse.builder()
            .message(message)
            .error("VALIDATION_ERROR")
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle illegal argument exceptions (parameter validation).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .error("VALIDATION_ERROR")
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log complete stack trace for debugging
        log.error("Unexpected error occurred", ex);

        // Don't expose technical details to client
        ErrorResponse error = ErrorResponse.builder()
            .message("An unexpected error occurred. Please try again later or contact support")
            .error("INTERNAL_SERVER_ERROR")
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Standard error response format.
     * Follows the format specified in erp-lite-general-spec.md
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private String error;
    }
}
