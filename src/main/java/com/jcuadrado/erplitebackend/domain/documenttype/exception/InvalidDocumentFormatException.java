package com.jcuadrado.erplitebackend.domain.documenttype.exception;

/**
 * Exception thrown when a document number doesn't match the expected format.
 */
public class InvalidDocumentFormatException extends DocumentTypeDomainException {
    
    public InvalidDocumentFormatException(String documentNumber, String pattern) {
        super("Document number '" + documentNumber + "' does not match pattern: " + pattern);
    }
    
    public InvalidDocumentFormatException(String message) {
        super(message);
    }
}
