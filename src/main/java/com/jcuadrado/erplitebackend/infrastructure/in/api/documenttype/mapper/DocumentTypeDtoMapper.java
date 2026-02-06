package com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.mapper;

import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.in.api.common.dto.UserSummaryDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.dto.CreateDocumentTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.dto.DocumentTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.dto.UpdateDocumentTypeRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

/**
 * MapStruct mapper for converting between DTOs and domain models.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentTypeDtoMapper {
    
    /**
     * Convert create request DTO to domain model.
     */
    DocumentType toDomain(CreateDocumentTypeRequestDto dto);
    
    /**
     * Convert update request DTO to domain model.
     */
    DocumentType toDomain(UpdateDocumentTypeRequestDto dto);
    
    /**
     * Convert domain model to response DTO.
     * Note: User data (createdBy/updatedBy/deletedBy) will need to be populated separately
     * by the service layer when user management is implemented.
     */
    @Mapping(target = "createdBy", expression = "java(createUserSummary(documentType.getCreatedBy()))")
    @Mapping(target = "updatedBy", expression = "java(createUserSummary(documentType.getUpdatedBy()))")
    DocumentTypeResponseDto toResponseDto(DocumentType documentType);
    
    /**
     * Creates a mock user summary. This should be replaced with actual user fetching
     * when the user management module is implemented.
     */
    default UserSummaryDto createUserSummary(Long userId) {
        if (userId == null) {
            return null;
        }
        return UserSummaryDto.builder()
            .id(userId)
            .uuid(UUID.randomUUID()) // TODO: Fetch actual user UUID
            .username("admin") // TODO: Fetch actual username
            .email("admin@example.com") // TODO: Fetch actual email
            .build();
    }
}
