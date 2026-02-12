package com.jcuadrado.erplitebackend.domain.exception.documenttypes;

/**
 * Exception thrown when attempting to create a document type with a duplicate code
 */
public class DuplicateCodeException extends DocumentTypeDomainException {

    public DuplicateCodeException(String code) {
        super(String.format("Document type with code '%s' already exists", code));
    }
}

