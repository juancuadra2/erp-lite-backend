package com.jcuadrado.erplitebackend.application.usecase.documenttypes;

import com.jcuadrado.erplitebackend.application.port.documenttypes.ManageDocumentTypeUseCase;
import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import com.jcuadrado.erplitebackend.domain.service.documenttypes.DocumentTypeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of ManageDocumentTypeUseCase
 * Orchestrates domain services and repository for command operations
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ManageDocumentTypeUseCaseImpl implements ManageDocumentTypeUseCase {

    private final DocumentTypeRepository repository;
    private final DocumentTypeDomainService domainService;

    @Override
    public DocumentType create(DocumentType documentType) {
        log.debug("Creating document type with code: {}", documentType.getCode());

        // Prepare and validate for creation
        domainService.prepareForCreation(documentType);

        // Set audit fields
        documentType.setCreatedAt(LocalDateTime.now());
        // TODO: Set createdBy from security context when auth is implemented

        // Save and return
        DocumentType saved = repository.save(documentType);

        log.info("Document type created successfully with ID: {} and UUID: {}", saved.getId(), saved.getUuid());
        return saved;
    }

    @Override
    public DocumentType update(UUID uuid, DocumentType documentType) {
        // Find existing document type
        DocumentType existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new DocumentTypeNotFoundException(uuid));

        // Update fields
        existing.setCode(documentType.getCode());
        existing.setName(documentType.getName());
        existing.setDescription(documentType.getDescription());

        // Prepare and validate for update
        domainService.prepareForUpdate(existing);

        // Set audit fields
        existing.setUpdatedAt(LocalDateTime.now());
        // TODO: Set updatedBy from security context when auth is implemented

        // Save and return
        return repository.save(existing);
    }

    @Override
    public void delete(UUID uuid) {
        // Find existing document type
        DocumentType existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new DocumentTypeNotFoundException(uuid));

        // Check if can be deleted
        if (!domainService.canDelete(existing)) {
            throw new IllegalStateException("Cannot delete document type: it is referenced by other entities");
        }

        // Soft delete
        existing.markAsDeleted(null); // TODO: Get userId from security context
        repository.save(existing);
    }

    @Override
    public void activate(UUID uuid) {
        // Find existing document type
        DocumentType existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new DocumentTypeNotFoundException(uuid));

        // Activate
        existing.activate();

        // Set audit fields
        existing.setUpdatedAt(LocalDateTime.now());
        // TODO: Set updatedBy from security context when auth is implemented

        // Save
        repository.save(existing);
    }

    @Override
    public void deactivate(UUID uuid) {
        // Find existing document type
        DocumentType existing = repository.findByUuid(uuid)
            .orElseThrow(() -> new DocumentTypeNotFoundException(uuid));

        // Check if can be deactivated
        if (!domainService.canDeactivate(existing)) {
            throw new IllegalStateException("Cannot deactivate document type: it is in use");
        }

        // Deactivate
        existing.deactivate();

        // Set audit fields
        existing.setUpdatedAt(LocalDateTime.now());
        // TODO: Set updatedBy from security context when auth is implemented

        // Save
        repository.save(existing);
    }
}

