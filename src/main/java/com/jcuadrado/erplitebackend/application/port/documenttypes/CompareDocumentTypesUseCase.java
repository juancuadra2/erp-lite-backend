package com.jcuadrado.erplitebackend.application.port.documenttypes;

import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Use case for querying document types (Queries: get, list, search, compare)
 */
public interface CompareDocumentTypesUseCase {

    /**
     * Get document type by UUID
     */
    DocumentType getByUuid(UUID uuid);

    /**
     * Get document type by code
     */
    DocumentType getByCode(String code);

    /**
     * Get all active document types
     */
    List<DocumentType> getAllActive();

    /**
     * Find all document types with filters and pagination
     * @param filters Map with filter criteria (enabled, search, etc.)
     * @param pageable Pagination and sorting information
     */
    Page<DocumentType> findAll(Map<String, Object> filters, Pageable pageable);
}

