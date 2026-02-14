package com.jcuadrado.erplitebackend.domain.service.documenttypes;

import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DuplicateCodeException;
import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Domain service with business rules for Document Type
 */
@RequiredArgsConstructor
public class DocumentTypeDomainService {

    private final DocumentTypeRepository repository;
    private final DocumentTypeValidator validator;

    /**
     * Normalize code to uppercase and trim
     */
    public String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.toUpperCase().trim();
    }

    /**
     * Validate that code is unique in the system
     * @throws DuplicateCodeException if code already exists
     */
    public void validateUniqueCode(String code) {
        String normalizedCode = normalizeCode(code);
        if (repository.existsByCode(normalizedCode)) {
            throw new DuplicateCodeException(normalizedCode);
        }
    }

    /**
     * Validate that code is unique excluding a specific UUID (for updates)
     * @throws DuplicateCodeException if code already exists for different document type
     */
    public void validateUniqueCodeExcluding(String code, UUID excludeUuid) {
        String normalizedCode = normalizeCode(code);
        if (repository.existsByCodeExcludingUuid(normalizedCode, excludeUuid)) {
            throw new DuplicateCodeException(normalizedCode);
        }
    }

    /**
     * Check if document type can be deactivated
     * Business rule: Can always deactivate (no dependencies yet)
     */
    public boolean canDeactivate(DocumentType documentType) {
        // In the future, check if there are references in other modules
        return true;
    }

    /**
     * Check if document type can be deleted
     * Business rule: Can delete if not referenced by other entities
     */
    public boolean canDelete(DocumentType documentType) {
        // In the future, check if there are references in other modules
        // For now, we always allow soft delete
        return true;
    }

    /**
     * Validate and prepare document type for creation
     */
    public void prepareForCreation(DocumentType documentType) {
        validator.validateAll(
            documentType.getCode(),
            documentType.getName(),
            documentType.getDescription()
        );

        documentType.setCode(normalizeCode(documentType.getCode()));
        validateUniqueCode(documentType.getCode());

        if (documentType.getUuid() == null) {
            documentType.setUuid(UUID.randomUUID());
        }
        if (documentType.getActive() == null) {
            documentType.setActive(true);
        }
    }

    /**
     * Validate and prepare document type for update
     */
    public void prepareForUpdate(DocumentType documentType) {
        validator.validateAll(
            documentType.getCode(),
            documentType.getName(),
            documentType.getDescription()
        );

        documentType.setCode(normalizeCode(documentType.getCode()));
        validateUniqueCodeExcluding(documentType.getCode(), documentType.getUuid());
    }
}

