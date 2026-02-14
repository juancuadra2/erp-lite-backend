package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.CreateTaxTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.TaxTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.UpdateTaxTypeRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * TaxTypeDtoMapper - MapStruct mapper for DTOs <-> Domain
 * 
 * Handles conversion between presentation layer DTOs and domain models.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaxTypeDtoMapper {

    /**
     * Maps CreateRequest to domain model
     * Ignores all fields that should be set by the system
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    TaxType toDomain(CreateTaxTypeRequestDto dto);

    /**
     * Maps UpdateRequest to domain model
     * Ignores system fields (will be preserved from existing entity)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    TaxType toDomain(UpdateTaxTypeRequestDto dto);

    /**
     * Maps domain model to response DTO
     */
    TaxTypeResponseDto toResponseDto(TaxType taxType);
}
