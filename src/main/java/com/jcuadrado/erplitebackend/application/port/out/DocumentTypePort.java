package com.jcuadrado.erplitebackend.application.port.out;

import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for document type persistence operations.
 * Defines the contract that persistence adapters must implement.
 */
public interface DocumentTypePort {
    
    /**
     * Save a document type.
     */
    DocumentType save(DocumentType documentType);
    
    /**
     * Find document type by UUID.
     */
    Optional<DocumentType> findByUuid(UUID uuid);
    
    /**
     * Find document type by code.
     */
    Optional<DocumentType> findByCode(String code);
    
    /**
     * Check if a code exists.
     */
    boolean existsByCode(String code);
    
    /**
     * Check if a code exists excluding a specific UUID.
     */
    boolean existsByCodeExcludingUuid(String code, UUID uuid);
    
    /**
     * Find all document types with pagination.
     */
    Page<DocumentType> findAll(Pageable pageable);
    
    /**
     * Find all active document types.
     */
    List<DocumentType> findByActiveTrue();
    
    /**
     * Find active document types with pagination.
     */
    Page<DocumentType> findByActiveTrue(Pageable pageable);
    
    /**
     * Find document types with advanced filters.
     * 
     * @param enabled filter by active status
     * @param search global search in code, name, description
     * @param filters additional dynamic filters
     * @param pageable pagination and sorting
     * @return page of document types
     */
    Page<DocumentType> findWithFilters(Boolean enabled, String search, Map<String, Object> filters, Pageable pageable);
    
    /**
     * Delete a document type (soft delete).
     */
    void delete(DocumentType documentType);
}
