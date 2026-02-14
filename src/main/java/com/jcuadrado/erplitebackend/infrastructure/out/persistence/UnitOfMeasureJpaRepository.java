package com.jcuadrado.erplitebackend.infrastructure.out.persistence;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.unitofmeasure.UnitOfMeasureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitOfMeasureJpaRepository extends
        JpaRepository<UnitOfMeasureEntity, Long>,
        JpaSpecificationExecutor<UnitOfMeasureEntity> {

    Optional<UnitOfMeasureEntity> findByUuid(UUID uuid);

    List<UnitOfMeasureEntity> findByEnabledTrue();

    List<UnitOfMeasureEntity> findByNameContainingIgnoreCase(String name);

    List<UnitOfMeasureEntity> findByAbbreviationContainingIgnoreCase(String abbreviation);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndUuidNot(String name, UUID uuid);

    boolean existsByAbbreviationIgnoreCase(String abbreviation);

    boolean existsByAbbreviationIgnoreCaseAndUuidNot(String abbreviation, UUID uuid);

    @Query("""
        SELECT 0
        FROM UnitOfMeasureEntity uom
        WHERE uom.uuid = :unitOfMeasureUuid
    """)
    long countProductsWithUnitOfMeasure(@Param("unitOfMeasureUuid") UUID unitOfMeasureUuid);
}
