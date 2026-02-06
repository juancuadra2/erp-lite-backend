package com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.adapter;

import com.jcuadrado.erplitebackend.application.port.out.DocumentTypePort;
import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.entity.DocumentTypeEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.mapper.DocumentTypeEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.repository.DocumentTypeJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.specification.DocumentTypeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing DocumentTypePort using JPA repository.
 */
@Component
@RequiredArgsConstructor
public class DocumentTypeRepositoryAdapter implements DocumentTypePort {
    
    private final DocumentTypeJpaRepository repository;
    private final DocumentTypeEntityMapper mapper;
    
    @Override
    public DocumentType save(DocumentType documentType) {
        DocumentTypeEntity entity = mapper.toEntity(documentType);
        DocumentTypeEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<DocumentType> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<DocumentType> findByCode(String code) {
        return repository.findByCode(code)
            .map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }
    
    @Override
    public boolean existsByCodeExcludingUuid(String code, UUID uuid) {
        return repository.existsByCodeAndUuidNot(code, uuid);
    }
    
    @Override
    public Page<DocumentType> findAll(Pageable pageable) {
        return repository.findAll(pageable)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<DocumentType> findByActiveTrue() {
        return repository.findByActiveTrue().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<DocumentType> findByActiveTrue(Pageable pageable) {
        return repository.findByActiveTrue(pageable)
            .map(mapper::toDomain);
    }
    
    @Override
    public Page<DocumentType> findWithFilters(Boolean enabled, String search, Map<String, Object> filters, Pageable pageable) {
        Specification<DocumentTypeEntity> spec = DocumentTypeSpecification.withFilters(enabled, search, filters);
        Page<DocumentType> res = repository.findAll(spec, pageable)
            .map(mapper::toDomain);
        System.out.println(res);
        return res;
    }
    
    @Override
    public void delete(DocumentType documentType) {
        repository.findByUuid(documentType.getUuid())
            .ifPresent(entity -> {
                entity.setDeletedAt(LocalDateTime.now());
                repository.save(entity);
            });
    }
}
