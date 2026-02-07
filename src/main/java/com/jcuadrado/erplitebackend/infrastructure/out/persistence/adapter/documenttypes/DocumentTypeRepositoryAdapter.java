package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.documenttypes;

import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.DocumentTypeJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.documenttypes.DocumentTypeEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.documenttypes.DocumentTypeEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.documenttypes.DocumentTypeSpecificationUtil;
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
 * Adapter implementing DocumentTypeRepository using JPA
 */
@Component
@RequiredArgsConstructor
public class DocumentTypeRepositoryAdapter implements DocumentTypeRepository {

    private final DocumentTypeJpaRepository jpaRepository;
    private final DocumentTypeEntityMapper mapper;

    @Override
    public DocumentType save(DocumentType documentType) {
        DocumentTypeEntity entity = mapper.toEntity(documentType);
        DocumentTypeEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DocumentType> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<DocumentType> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<DocumentType> findByCode(String code) {
        return jpaRepository.findByCode(code)
            .map(mapper::toDomain);
    }

    @Override
    public Page<DocumentType> findAll(Map<String, Object> filters, Pageable pageable) {
        Specification<DocumentTypeEntity> spec = DocumentTypeSpecificationUtil.buildSpecification(filters);
        return jpaRepository.findAll(spec, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public List<DocumentType> findAllActive() {
        return jpaRepository.findByActiveTrue().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeExcludingUuid(String code, UUID excludeUuid) {
        return jpaRepository.existsByCodeAndUuidNot(code, excludeUuid);
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        jpaRepository.findByUuid(uuid)
            .ifPresent(jpaRepository::delete);
    }
}

