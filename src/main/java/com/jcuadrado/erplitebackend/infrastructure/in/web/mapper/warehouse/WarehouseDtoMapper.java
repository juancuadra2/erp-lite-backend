package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.warehouse;

import com.jcuadrado.erplitebackend.application.command.warehouse.CreateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.command.warehouse.UpdateWarehouseCommand;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.CreateWarehouseRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.UpdateWarehouseRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.WarehouseResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WarehouseDtoMapper {

    CreateWarehouseCommand toCreateCommand(CreateWarehouseRequestDto dto);

    UpdateWarehouseCommand toUpdateCommand(UpdateWarehouseRequestDto dto);

    WarehouseResponseDto toResponseDto(Warehouse domain);
}
