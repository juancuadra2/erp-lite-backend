package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security;

import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleEntityMapper {

    @Mapping(target = "active", source = "active")
    Role toDomain(RoleEntity entity);

    @Mapping(target = "permissions", ignore = true)
    RoleEntity toEntity(Role domain);
}
