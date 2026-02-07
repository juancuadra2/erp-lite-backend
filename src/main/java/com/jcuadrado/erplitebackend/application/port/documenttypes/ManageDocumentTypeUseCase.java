package com.jcuadrado.erplitebackend.application.port.documenttypes;

import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;

/**
 * Use case for managing document types (Commands: create, update, delete, activate/deactivate)
 */
public interface ManageDocumentTypeUseCase {

    /**
     * Create a new document type
     */
    DocumentType create(DocumentType documentType);

    /**
     * Update an existing document type
     */
    DocumentType update(java.util.UUID uuid, DocumentType documentType);

    /**
     * Delete a document type (soft delete)
     */
    void delete(java.util.UUID uuid);

    /**
     * Activate a document type
     */
    void activate(java.util.UUID uuid);

    /**
     * Deactivate a document type
     */
    void deactivate(java.util.UUID uuid);
}

