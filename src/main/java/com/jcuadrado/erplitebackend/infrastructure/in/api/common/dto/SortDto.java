package com.jcuadrado.erplitebackend.infrastructure.in.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sorting parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortDto {
    
    /**
     * Field to sort by (e.g., "code", "name", "createdAt").
     */
    private String field;
    
    /**
     * Sort order: ASC or DESC.
     */
    private String order;
}
