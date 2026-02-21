package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security;

import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.AuditLogResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuditLogDtoMapper {

    @Mapping(target = "action", expression = "java(auditLog.getAction().name())")
    AuditLogResponseDto toResponseDto(AuditLog auditLog);
}
