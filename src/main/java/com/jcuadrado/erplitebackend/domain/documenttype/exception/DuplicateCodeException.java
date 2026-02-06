package com.jcuadrado.erplitebackend.domain.documenttype.exception;

/**
 * Exception thrown when attempting to create a document type with a code that already exists.
 */
public class DuplicateCodeException extends DocumentTypeDomainException {
    
    public DuplicateCodeException(String code) {
        super("Document type code already exists: " + code);
    }
}
