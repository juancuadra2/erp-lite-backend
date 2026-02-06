package com.jcuadrado.erplitebackend.domain.documenttype.exception;

import java.util.UUID;

/**
 * Exception thrown when a document type is not found.
 */
public class DocumentTypeNotFoundException extends DocumentTypeDomainException {
    
    public DocumentTypeNotFoundException(UUID uuid) {
        super("Document type not found with UUID: " + uuid);
    }
    
    public DocumentTypeNotFoundException(String code) {
        super("Document type not found with code: " + code);
    }
    
    public DocumentTypeNotFoundException(Long id) {
        super("Document type not found with ID: " + id);
    }
}
