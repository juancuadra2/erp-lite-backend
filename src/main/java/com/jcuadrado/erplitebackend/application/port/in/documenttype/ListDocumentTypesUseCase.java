package com.jcuadrado.erplitebackend.application.port.in.documenttype;

import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Use case for listing and searching document types.
 */
public interface ListDocumentTypesUseCase {
    
    /**
     * Lists all document types with pagination.
     * 
     * @param pageable pagination parameters
     * @return page of document types
     */
    Page<DocumentType> listAll(Pageable pageable);
    
    /**
     * Lists all active document types.
     * 
     * @return list of active document types
     */
    List<DocumentType> listActive();
    
    /**
     * Lists document types with advanced filters.
     * 
     * @param enabled filter by active status (optional)
     * @param search global search in code, name, description (optional)
     * @param filters additional dynamic filters (optional)
     * @param pageable pagination and sorting
     * @return page of document types
     */
    Page<DocumentType> listWithFilters(Boolean enabled, String search, Map<String, Object> filters, Pageable pageable);
}
