package com.jcuadrado.erplitebackend.infrastructure.out.persistence;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.documenttypes.DocumentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for DocumentType persistence operations
 * Note: This is NOT in a feature folder as it's a JPA interface
 */
@Repository
public interface DocumentTypeJpaRepository extends
    JpaRepository<DocumentTypeEntity, Long>,
    JpaSpecificationExecutor<DocumentTypeEntity> {

    /**
     * Find document type by UUID
     */
    Optional<DocumentTypeEntity> findByUuid(UUID uuid);

    /**
     * Find document type by code
     */
    Optional<DocumentTypeEntity> findByCode(String code);

    /**
     * Check if a code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if a code exists excluding a specific UUID
     */
    boolean existsByCodeAndUuidNot(String code, UUID uuid);

    /**
     * Find all active document types
     */
    List<DocumentTypeEntity> findByActiveTrue();
}

