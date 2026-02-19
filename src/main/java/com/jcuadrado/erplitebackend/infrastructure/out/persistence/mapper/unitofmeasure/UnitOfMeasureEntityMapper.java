package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.unitofmeasure.UnitOfMeasureEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UnitOfMeasureEntityMapper {

    UnitOfMeasureEntity toEntity(UnitOfMeasure unitOfMeasure);

    UnitOfMeasure toDomain(UnitOfMeasureEntity entity);
}
