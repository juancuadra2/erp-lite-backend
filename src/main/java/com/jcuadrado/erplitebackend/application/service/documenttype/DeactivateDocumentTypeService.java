package com.jcuadrado.erplitebackend.application.service.documenttype;

import com.jcuadrado.erplitebackend.application.port.in.documenttype.DeactivateDocumentTypeUseCase;
import com.jcuadrado.erplitebackend.application.port.out.DocumentTypePort;
import com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementing deactivate/activate document type use case.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DeactivateDocumentTypeService implements DeactivateDocumentTypeUseCase {
    
    private final DocumentTypePort documentTypePort;
    
    @Override
    public void deactivate(UUID uuid) {
        DocumentType documentType = documentTypePort.findByUuid(uuid)
            .orElseThrow(() -> new DocumentTypeNotFoundException(uuid));
        
        documentType.deactivate();
        documentTypePort.save(documentType);
    }
    
    @Override
    public void activate(UUID uuid) {
        DocumentType documentType = documentTypePort.findByUuid(uuid)
            .orElseThrow(() -> new DocumentTypeNotFoundException(uuid));
        
        documentType.activate();
        documentTypePort.save(documentType);
    }
}
