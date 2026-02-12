package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.DepartmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartmentEntityMapper {

    @Mapping(target = "municipalities", ignore = true)
    DepartmentEntity toEntity(Department department);

    @Mapping(target = "municipalities", ignore = true)
    Department toDomain(DepartmentEntity entity);
}
