package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security;

import com.jcuadrado.erplitebackend.domain.model.security.RefreshToken;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.RefreshTokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefreshTokenEntityMapper {

    RefreshToken toDomain(RefreshTokenEntity entity);

    RefreshTokenEntity toEntity(RefreshToken domain);
}
