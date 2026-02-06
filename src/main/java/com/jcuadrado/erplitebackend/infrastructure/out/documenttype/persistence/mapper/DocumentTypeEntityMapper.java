package com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.mapper;

import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.out.documenttype.persistence.entity.DocumentTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between DocumentType domain model and DocumentTypeEntity.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentTypeEntityMapper {
    
    /**
     * Convert domain model to entity.
     */
    DocumentTypeEntity toEntity(DocumentType documentType);
    
    /**
     * Convert entity to domain model.
     */
    DocumentType toDomain(DocumentTypeEntity entity);
}
