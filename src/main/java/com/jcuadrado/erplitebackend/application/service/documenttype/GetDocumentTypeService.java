package com.jcuadrado.erplitebackend.application.service.documenttype;

import com.jcuadrado.erplitebackend.application.port.in.documenttype.GetDocumentTypeUseCase;
import com.jcuadrado.erplitebackend.application.port.out.DocumentTypePort;
import com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementing get document type use case.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetDocumentTypeService implements GetDocumentTypeUseCase {
    
    private final DocumentTypePort documentTypePort;
    
    @Override
    public DocumentType getByUuid(UUID uuid) {
        return documentTypePort.findByUuid(uuid)
            .orElseThrow(() -> new DocumentTypeNotFoundException(uuid));
    }
    
    @Override
    public DocumentType getByCode(String code) {
        return documentTypePort.findByCode(code)
            .orElseThrow(() -> new DocumentTypeNotFoundException(code));
    }
}
