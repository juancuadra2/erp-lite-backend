package com.jcuadrado.erplitebackend.domain.port.documenttypes;

import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for Document Type persistence operations.
 * This interface is part of the domain layer and defines the contract
 * that persistence adapters must implement.
 */
public interface DocumentTypeRepository {

    /**
     * Save a document type (create or update)
     */
    DocumentType save(DocumentType documentType);

    /**
     * Find document type by ID
     */
    Optional<DocumentType> findById(Long id);

    /**
     * Find document type by UUID
     */
    Optional<DocumentType> findByUuid(UUID uuid);

    /**
     * Find document type by code
     */
    Optional<DocumentType> findByCode(String code);

    /**
     * Find all document types with pagination and filters
     * @param filters Map with filter criteria (enabled, search, etc.)
     * @param pageable Pagination and sorting information
     */
    Page<DocumentType> findAll(Map<String, Object> filters, Pageable pageable);

    /**
     * Find all active document types
     */
    List<DocumentType> findAllActive();

    /**
     * Check if a code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if a code exists excluding a specific UUID (for updates)
     */
    boolean existsByCodeExcludingUuid(String code, UUID excludeUuid);

    /**
     * Delete document type by UUID (soft delete)
     */
    void deleteByUuid(UUID uuid);
}

