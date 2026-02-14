package com.jcuadrado.erplitebackend.infrastructure.out.persistence;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for TaxType persistence operations
 * Note: This is NOT in a feature folder as it's a JPA interface
 */
@Repository
public interface TaxTypeJpaRepository extends
    JpaRepository<TaxTypeEntity, Long>,
    JpaSpecificationExecutor<TaxTypeEntity> {

    /**
     * Find tax type by UUID
     */
    Optional<TaxTypeEntity> findByUuid(UUID uuid);

    /**
     * Find tax type by code
     */
    Optional<TaxTypeEntity> findByCode(String code);

    /**
     * Check if a code exists
     */
    boolean existsByCode(String code);

    /**
     * Check if a code exists excluding a specific UUID
     */
    boolean existsByCodeAndUuidNot(String code, UUID uuid);

    /**
     * Find all enabled tax types
     */
    List<TaxTypeEntity> findByEnabledTrue();

    /**
     * Find tax types by enabled status
     */
    List<TaxTypeEntity> findByEnabled(Boolean enabled);

    /**
     * Find tax types by name containing (case-insensitive)
     */
    List<TaxTypeEntity> findByNameContainingIgnoreCase(String name);

    /**
     * Find tax types by application type
     * Note: Use Specifications for complex queries including BOTH logic
     */
    List<TaxTypeEntity> findByApplicationType(TaxApplicationType applicationType);

    /**
     * Find tax types by application type and enabled status
     */
    List<TaxTypeEntity> findByApplicationTypeAndEnabled(TaxApplicationType applicationType, Boolean enabled);
}
