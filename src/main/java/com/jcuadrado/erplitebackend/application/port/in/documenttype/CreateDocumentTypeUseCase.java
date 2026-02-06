package com.jcuadrado.erplitebackend.application.port.in.documenttype;

import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;

/**
 * Use case for creating a new document type.
 */
public interface CreateDocumentTypeUseCase {
    
    /**
     * Creates a new document type.
     * 
     * @param documentType the document type to create
     * @return the created document type with generated ID and UUID
     */
    DocumentType create(DocumentType documentType);
}
