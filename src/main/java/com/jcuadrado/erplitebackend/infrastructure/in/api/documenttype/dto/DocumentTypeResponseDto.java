package com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.dto;

import com.jcuadrado.erplitebackend.infrastructure.in.api.common.dto.UserSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for document type responses.
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryDto createdBy;
    private UserSummaryDto updatedBy;
}
