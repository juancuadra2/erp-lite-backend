package com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.repository;

import com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.entity.DocumentTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for DocumentType persistence operations.
 */
@Repository
public interface DocumentTypeJpaRepository extends JpaRepository<DocumentTypeEntity, Long>, 
        JpaSpecificationExecutor<DocumentTypeEntity> {
    
    /**
     * Find document type by UUID.
     */
    Optional<DocumentTypeEntity> findByUuid(UUID uuid);
    
    /**
     * Find document type by code.
     */
    Optional<DocumentTypeEntity> findByCode(String code);
    
    /**
     * Check if a code exists (for uniqueness validation).
     */
    boolean existsByCode(String code);
    
    /**
     * Check if a code exists excluding a specific UUID (for updates).
     */
    boolean existsByCodeAndUuidNot(String code, UUID uuid);
    
    /**
     * Find all active document types with pagination.
     */
    Page<DocumentTypeEntity> findByActiveTrue(Pageable pageable);
    
    /**
     * Find all active document types.
     */
    List<DocumentTypeEntity> findByActiveTrue();
    
    /**
     * Find all document types with pagination.
     */
    Page<DocumentTypeEntity> findAll(Pageable pageable);
}
