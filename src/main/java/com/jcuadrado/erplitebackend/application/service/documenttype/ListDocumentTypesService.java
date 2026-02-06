package com.jcuadrado.erplitebackend.application.service.documenttype;

import com.jcuadrado.erplitebackend.application.port.in.documenttype.ListDocumentTypesUseCase;
import com.jcuadrado.erplitebackend.application.port.out.DocumentTypePort;
import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service implementing list document types use case.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListDocumentTypesService implements ListDocumentTypesUseCase {
    
    private final DocumentTypePort documentTypePort;
    
    @Override
    public Page<DocumentType> listAll(Pageable pageable) {
        return documentTypePort.findAll(pageable);
    }
    
    @Override
    public List<DocumentType> listActive() {
        return documentTypePort.findByActiveTrue();
    }
    
    @Override
    public Page<DocumentType> listWithFilters(Boolean enabled, String search, Map<String, Object> filters, Pageable pageable) {
        return documentTypePort.findWithFilters(enabled, search, filters, pageable);
    }
}
