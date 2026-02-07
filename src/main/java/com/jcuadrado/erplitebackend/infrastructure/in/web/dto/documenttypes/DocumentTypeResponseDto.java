package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for document type response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeResponseDto {

    private Long id;
    private UUID uuid;
    private String code;
    private String name;
    private String description;
    private Boolean active;

    // Audit fields
    private Long createdBy;
    private Long updatedBy;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}

