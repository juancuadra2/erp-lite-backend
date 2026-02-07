package com.jcuadrado.erplitebackend.domain.exception.documenttypes;

/**
 * Exception thrown when document type data is invalid
 */
public class InvalidDocumentTypeException extends DocumentTypeDomainException {

    public InvalidDocumentTypeException(String message) {
        super(message);
    }

    public InvalidDocumentTypeException(String field, String reason) {
        super(String.format("Invalid %s: %s", field, reason));
    }
}

