package com.jcuadrado.erplitebackend.domain.exception.documenttypes;

/**
 * Base domain exception for Document Type operations
 */
public class DocumentTypeDomainException extends RuntimeException {

    public DocumentTypeDomainException(String message) {
        super(message);
    }

    public DocumentTypeDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

