package com.jcuadrado.erplitebackend.application.port.in.documenttype;

import java.util.UUID;

/**
 * Use case for deactivating a document type.
 */
public interface DeactivateDocumentTypeUseCase {
    
    /**
     * Deactivates a document type.
     * 
     * @param uuid the UUID of the document type to deactivate
     */
    void deactivate(UUID uuid);
    
    /**
     * Activates a document type.
     * 
     * @param uuid the UUID of the document type to activate
     */
    void activate(UUID uuid);
}
