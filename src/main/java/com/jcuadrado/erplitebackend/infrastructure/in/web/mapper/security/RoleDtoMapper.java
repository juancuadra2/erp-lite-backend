package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security;

import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.RoleResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleDtoMapper {

    RoleResponseDto toResponseDto(Role role);
}
