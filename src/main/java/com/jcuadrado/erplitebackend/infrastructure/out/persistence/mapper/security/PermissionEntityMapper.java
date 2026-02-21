package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security;

import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.PermissionAction;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.PermissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PermissionEntityMapper {

    @Mapping(target = "action", expression = "java(mapAction(entity.getAction()))")
    @Mapping(target = "condition", source = "conditionExpr")
    Permission toDomain(PermissionEntity entity);

    @Mapping(target = "action", expression = "java(domain.getAction().name())")
    @Mapping(target = "conditionExpr", source = "condition")
    PermissionEntity toEntity(Permission domain);

    default PermissionAction mapAction(String action) {
        return PermissionAction.valueOf(action);
    }
}
