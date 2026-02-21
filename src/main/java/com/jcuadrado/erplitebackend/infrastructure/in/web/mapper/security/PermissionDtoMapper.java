package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security;

import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.PermissionResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermissionDtoMapper {

    @Mapping(target = "action", expression = "java(permission.getAction().name())")
    PermissionResponseDto toResponseDto(Permission permission);
}
