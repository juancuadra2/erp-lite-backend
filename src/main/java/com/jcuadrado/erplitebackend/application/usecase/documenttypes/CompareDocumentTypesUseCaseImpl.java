package com.jcuadrado.erplitebackend.application.usecase.documenttypes;

import com.jcuadrado.erplitebackend.application.port.documenttypes.CompareDocumentTypesUseCase;
import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of CompareDocumentTypesUseCase
 * Orchestrates repository for query operations
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompareDocumentTypesUseCaseImpl implements CompareDocumentTypesUseCase {

    private final DocumentTypeRepository repository;

    @Override
    public DocumentType getByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
            .orElseThrow(() -> new DocumentTypeNotFoundException(uuid));
    }

    @Override
    public DocumentType getByCode(String code) {
        return repository.findByCode(code)
            .orElseThrow(() -> new DocumentTypeNotFoundException(code));
    }

    @Override
    public List<DocumentType> getAllActive() {
        return repository.findAllActive();
    }

    @Override
    public Page<DocumentType> findAll(Map<String, Object> filters, Pageable pageable) {
        return repository.findAll(filters, pageable);
    }
}

