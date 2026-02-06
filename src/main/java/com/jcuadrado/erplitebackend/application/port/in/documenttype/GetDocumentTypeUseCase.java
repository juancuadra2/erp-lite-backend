package com.jcuadrado.erplitebackend.application.port.in.documenttype;

import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;

import java.util.UUID;

/**
 * Use case for retrieving document type information.
 */
public interface GetDocumentTypeUseCase {
    
    /**
     * Gets a document type by its UUID.
     * 
     * @param uuid the UUID of the document type
     * @return the document type
     * @throws com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeNotFoundException if not found
     */
    DocumentType getByUuid(UUID uuid);
    
    /**
     * Gets a document type by its code.
     * 
     * @param code the code of the document type
     * @return the document type
     * @throws com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeNotFoundException if not found
     */
    DocumentType getByCode(String code);
}
