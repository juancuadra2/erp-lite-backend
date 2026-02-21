package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security;

import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.AuditLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogEntityMapper {

    @Mapping(target = "action", expression = "java(mapAction(entity.getAction()))")
    AuditLog toDomain(AuditLogEntity entity);

    @Mapping(target = "action", expression = "java(domain.getAction().name())")
    AuditLogEntity toEntity(AuditLog domain);

    default AuditAction mapAction(String action) {
        return AuditAction.valueOf(action);
    }
}
