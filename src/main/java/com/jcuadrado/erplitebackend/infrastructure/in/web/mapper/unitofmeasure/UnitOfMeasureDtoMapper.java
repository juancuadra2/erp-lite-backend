package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.CreateUnitOfMeasureRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.UnitOfMeasureResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.UpdateUnitOfMeasureRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UnitOfMeasureDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    UnitOfMeasure toDomain(CreateUnitOfMeasureRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    UnitOfMeasure toDomain(UpdateUnitOfMeasureRequestDto dto);

    UnitOfMeasureResponseDto toResponseDto(UnitOfMeasure unitOfMeasure);
}
