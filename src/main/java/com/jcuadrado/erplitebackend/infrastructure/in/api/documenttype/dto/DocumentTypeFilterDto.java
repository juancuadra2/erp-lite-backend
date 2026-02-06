package com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.dto;

import com.jcuadrado.erplitebackend.infrastructure.in.api.common.dto.SortDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for document type filter parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeFilterDto {
    
    /**
     * Filter by active status.
     */
    private Boolean enabled;
    
    /**
     * Global search in code, name, and description.
     */
    private String search;
    
    /**
     * Page number (0-based).
     */
    @Min(0)
    @Builder.Default
    private Integer page = 0;
    
    /**
     * Number of items per page.
     */
    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer limit = 10;
    
    /**
     * Fields to include in response (e.g., ["id", "uuid", "code", "name"]).
     */
    private List<String> fields;
    
    /**
     * Sorting parameters.
     */
    private SortDto sort;
    
    /**
     * Relations to populate (e.g., ["createdBy", "updatedBy"]).
     */
    private List<String> populate;
    
    /**
     * Additional dynamic filters (e.g., {"country": "CO", "type": "admin"}).
     */
    private Map<String, Object> filters;
}
