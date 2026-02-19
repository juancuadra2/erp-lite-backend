package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security;

import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    @Mapping(target = "active", source = "active")
    User toDomain(UserEntity entity);

    @Mapping(target = "roles", ignore = true)
    UserEntity toEntity(User domain);
}
