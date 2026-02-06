package com.jcuadrado.erplitebackend.application.service.documenttype;

import com.jcuadrado.erplitebackend.application.port.in.documenttype.UpdateDocumentTypeUseCase;
import com.jcuadrado.erplitebackend.application.port.out.DocumentTypePort;
import com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import com.jcuadrado.erplitebackend.domain.documenttype.service.DocumentTypeDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementing update document type use case.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateDocumentTypeService implements UpdateDocumentTypeUseCase {
    
    private final DocumentTypePort documentTypePort;
    private final DocumentTypeDomainService domainService;
    
    @Override
    public DocumentType update(UUID uuid, DocumentType updatedData) {
        // Find existing document type
        DocumentType existing = documentTypePort.findByUuid(uuid)
            .orElseThrow(() -> new DocumentTypeNotFoundException(uuid));
        
        // Validate business rules
        domainService.validateName(updatedData.getName());
        
        // Update fields (code cannot be updated per spec)
        existing.setName(updatedData.getName());
        existing.setDescription(updatedData.getDescription());
        existing.setUpdatedBy(updatedData.getUpdatedBy());
        
        // Save and return
        return documentTypePort.save(existing);
    }
}
