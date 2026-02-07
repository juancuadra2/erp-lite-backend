package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.MunicipalityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {DepartmentEntityMapper.class})
public interface MunicipalityEntityMapper {

    @Mapping(target = "department.municipalities", ignore = true)
    MunicipalityEntity toEntity(Municipality municipality);

    @Mapping(target = "department.municipalities", ignore = true)
    Municipality toDomain(MunicipalityEntity entity);
}
