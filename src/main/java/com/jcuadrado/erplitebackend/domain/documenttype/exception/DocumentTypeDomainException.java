package com.jcuadrado.erplitebackend.domain.documenttype.exception;

/**
 * Base exception for document type domain errors.
 */
public class DocumentTypeDomainException extends RuntimeException {
    
    public DocumentTypeDomainException(String message) {
        super(message);
    }
    
    public DocumentTypeDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
