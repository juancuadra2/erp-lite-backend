package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.warehouse.WarehouseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WarehouseEntityMapper {

    @Mapping(source = "uuid", target = "uuid")
    @Mapping(source = "municipalityUuid", target = "municipalityId")
    Warehouse toDomain(WarehouseEntity entity);

    @Mapping(source = "uuid", target = "uuid")
    @Mapping(source = "municipalityId", target = "municipalityUuid")
    WarehouseEntity toEntity(Warehouse domain);
}
