package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security;

import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserDtoMapper {

    UserResponseDto toResponseDto(User user);
}
