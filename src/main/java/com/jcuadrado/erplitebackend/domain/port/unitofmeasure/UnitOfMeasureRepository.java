package com.jcuadrado.erplitebackend.domain.port.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UnitOfMeasureRepository {

    UnitOfMeasure save(UnitOfMeasure unitOfMeasure);

    Optional<UnitOfMeasure> findByUuid(UUID uuid);

    Page<UnitOfMeasure> findAll(Map<String, Object> filters, Pageable pageable);

    List<UnitOfMeasure> findByEnabled(Boolean enabled);

    List<UnitOfMeasure> findByNameContaining(String name, Boolean enabled);

    List<UnitOfMeasure> findByAbbreviationContaining(String abbreviation, Boolean enabled);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndUuidNot(String name, UUID excludeUuid);

    boolean existsByAbbreviationIgnoreCase(String abbreviation);

    boolean existsByAbbreviationIgnoreCaseAndUuidNot(String abbreviation, UUID excludeUuid);

    long countProductsWithUnitOfMeasure(UUID unitOfMeasureUuid);
}
