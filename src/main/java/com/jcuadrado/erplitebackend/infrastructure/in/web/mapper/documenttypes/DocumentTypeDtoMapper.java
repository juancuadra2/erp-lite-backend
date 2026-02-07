package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.documenttypes;

import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.CreateDocumentTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.DocumentTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.UpdateDocumentTypeRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between DTOs and DocumentType domain model
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentTypeDtoMapper {

    /**
     * Convert CreateDocumentTypeRequestDto to domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    DocumentType toDomain(CreateDocumentTypeRequestDto dto);

    /**
     * Convert UpdateDocumentTypeRequestDto to domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    DocumentType toDomain(UpdateDocumentTypeRequestDto dto);

    /**
     * Convert domain model to DocumentTypeResponseDto
     */
    DocumentTypeResponseDto toResponseDto(DocumentType documentType);
}
