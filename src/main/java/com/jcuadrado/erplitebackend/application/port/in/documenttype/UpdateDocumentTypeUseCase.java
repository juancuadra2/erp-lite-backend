package com.jcuadrado.erplitebackend.application.port.in.documenttype;

import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;

import java.util.UUID;

/**
 * Use case for updating an existing document type.
 */
public interface UpdateDocumentTypeUseCase {
    
    /**
     * Updates an existing document type.
     * 
     * @param uuid the UUID of the document type to update
     * @param documentType the updated document type data
     * @return the updated document type
     */
    DocumentType update(UUID uuid, DocumentType documentType);
}
