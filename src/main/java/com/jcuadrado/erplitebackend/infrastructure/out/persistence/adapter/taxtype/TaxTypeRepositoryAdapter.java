package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.TaxTypeJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.taxtype.TaxTypeEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.taxtype.TaxTypeSpecificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TaxTypeRepositoryAdapter - Adapter implementing domain repository port
 * 
 * Bridges the gap between domain layer and JPA persistence layer.
 */
@Component
@RequiredArgsConstructor
public class TaxTypeRepositoryAdapter implements TaxTypeRepository {

    private final TaxTypeJpaRepository jpaRepository;
    private final TaxTypeEntityMapper mapper;

    @Override
    public TaxType save(TaxType taxType) {
        TaxTypeEntity entity = mapper.toEntity(taxType);
        TaxTypeEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TaxType> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<TaxType> findByCode(String code) {
        return jpaRepository.findByCode(code)
            .map(mapper::toDomain);
    }

    @Override
    public void delete(TaxType taxType) {
        jpaRepository.findByUuid(taxType.getUuid())
            .ifPresent(jpaRepository::delete);
    }

    @Override
    public Page<TaxType> findAll(Map<String, Object> filters, Pageable pageable) {
        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        return jpaRepository.findAll(spec, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public List<TaxType> findByEnabled(Boolean enabled) {
        if (enabled == null) {
            return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        }
        
        return jpaRepository.findByEnabled(enabled).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaxType> findByNameContaining(String searchTerm, Boolean enabled) {
        List<TaxTypeEntity> entities = jpaRepository.findByNameContainingIgnoreCase(searchTerm);
        
        if (enabled != null) {
            entities = entities.stream()
                .filter(entity -> entity.getEnabled().equals(enabled))
                .collect(Collectors.toList());
        }
        
        return entities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaxType> findByApplicationType(TaxApplicationType applicationType, Boolean enabled) {
        Map<String, Object> filters = Map.of(
            "applicationType", applicationType
        );
        
        if (enabled != null) {
            filters = Map.of(
                "applicationType", applicationType,
                "enabled", enabled
            );
        }
        
        Specification<TaxTypeEntity> spec = TaxTypeSpecificationUtil.buildSpecification(filters);
        return jpaRepository.findAll(spec).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndUuidNot(String code, UUID uuid) {
        return jpaRepository.existsByCodeAndUuidNot(code, uuid);
    }

    @Override
    public long countProductsWithTaxType(UUID taxTypeUuid) {
        // TODO [ISSUE-123]: Implementar consulta cuando exista el módulo de productos.
        return 0L;
    }

    @Override
    public long countTransactionsWithTaxType(UUID taxTypeUuid) {
        // TODO [ISSUE-124]: Implementar consulta cuando exista el módulo de transacciones.
        return 0L;
    }
}
