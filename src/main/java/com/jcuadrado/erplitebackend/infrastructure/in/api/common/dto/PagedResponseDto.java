package com.jcuadrado.erplitebackend.infrastructure.in.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paged response DTO with metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDto<T> {
    
    /**
     * List of items in the current page.
     */
    private List<T> content;
    
    /**
     * Total number of elements across all pages.
     */
    private Long totalElements;
    
    /**
     * Total number of pages.
     */
    private Integer totalPages;
    
    /**
     * Current page number (0-based).
     */
    private Integer currentPage;
    
    /**
     * Number of items per page.
     */
    private Integer pageSize;
}
