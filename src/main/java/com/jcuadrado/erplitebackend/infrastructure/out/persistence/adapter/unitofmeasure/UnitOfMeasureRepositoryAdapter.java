package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.domain.port.unitofmeasure.UnitOfMeasureRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.UnitOfMeasureJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.unitofmeasure.UnitOfMeasureEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.unitofmeasure.UnitOfMeasureEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.unitofmeasure.UnitOfMeasureSpecificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnitOfMeasureRepositoryAdapter implements UnitOfMeasureRepository {

    private final UnitOfMeasureJpaRepository jpaRepository;
    private final UnitOfMeasureEntityMapper mapper;

    @Override
    public UnitOfMeasure save(UnitOfMeasure unitOfMeasure) {
        UnitOfMeasureEntity entity = mapper.toEntity(unitOfMeasure);
        UnitOfMeasureEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UnitOfMeasure> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(mapper::toDomain);
    }

    @Override
    public Page<UnitOfMeasure> findAll(Map<String, Object> filters, Pageable pageable) {
        Specification<UnitOfMeasureEntity> specification = UnitOfMeasureSpecificationUtil.buildSpecification(filters);
        return jpaRepository.findAll(specification, pageable).map(mapper::toDomain);
    }

    @Override
    public List<UnitOfMeasure> findByEnabled(Boolean enabled) {
        if (Boolean.TRUE.equals(enabled)) {
            return jpaRepository.findByEnabledTrue().stream().map(mapper::toDomain).toList();
        }

        return jpaRepository.findAll().stream()
                .filter(entity -> enabled == null || entity.getEnabled().equals(enabled))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<UnitOfMeasure> findByNameContaining(String name, Boolean enabled) {
        return jpaRepository.findByNameContainingIgnoreCase(name).stream()
                .filter(entity -> enabled == null || entity.getEnabled().equals(enabled))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<UnitOfMeasure> findByAbbreviationContaining(String abbreviation, Boolean enabled) {
        return jpaRepository.findByAbbreviationContainingIgnoreCase(abbreviation).stream()
                .filter(entity -> enabled == null || entity.getEnabled().equals(enabled))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsByNameIgnoreCaseAndUuidNot(String name, UUID excludeUuid) {
        return jpaRepository.existsByNameIgnoreCaseAndUuidNot(name, excludeUuid);
    }

    @Override
    public boolean existsByAbbreviationIgnoreCase(String abbreviation) {
        return jpaRepository.existsByAbbreviationIgnoreCase(abbreviation);
    }

    @Override
    public boolean existsByAbbreviationIgnoreCaseAndUuidNot(String abbreviation, UUID excludeUuid) {
        return jpaRepository.existsByAbbreviationIgnoreCaseAndUuidNot(abbreviation, excludeUuid);
    }

    @Override
    public long countProductsWithUnitOfMeasure(UUID unitOfMeasureUuid) {
        return jpaRepository.countProductsWithUnitOfMeasure(unitOfMeasureUuid);
    }
}
