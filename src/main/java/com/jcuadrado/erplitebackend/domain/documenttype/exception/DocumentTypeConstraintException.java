package com.jcuadrado.erplitebackend.domain.documenttype.exception;

/**
 * Exception thrown when a business rule constraint is violated.
 */
public class DocumentTypeConstraintException extends DocumentTypeDomainException {
    
    public DocumentTypeConstraintException(String message) {
        super(message);
    }
    
    public DocumentTypeConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}
