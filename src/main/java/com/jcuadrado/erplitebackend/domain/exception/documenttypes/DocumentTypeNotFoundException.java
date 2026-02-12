package com.jcuadrado.erplitebackend.domain.exception.documenttypes;

import java.util.UUID;

/**
 * Exception thrown when a document type is not found
 */
public class DocumentTypeNotFoundException extends DocumentTypeDomainException {

    public DocumentTypeNotFoundException(UUID uuid) {
        super(String.format("Document type not found with UUID: %s", uuid));
    }

    public DocumentTypeNotFoundException(String code) {
        super(String.format("Document type not found with code: %s", code));
    }

    public DocumentTypeNotFoundException(Long id) {
        super(String.format("Document type not found with ID: %d", id));
    }
}

