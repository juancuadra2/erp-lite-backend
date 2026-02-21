package com.jcuadrado.erplitebackend.domain.model.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Permission {

    private UUID id;
    private String entity;
    private PermissionAction action;
    private String condition;
    private String description;

    public static Permission create(String entity, PermissionAction action,
                                    String condition, String description) {
        return Permission.builder()
                .id(UUID.randomUUID())
                .entity(entity)
                .action(action)
                .condition(condition)
                .description(description)
                .build();
    }

    public boolean hasCondition() {
        return condition != null && !condition.isBlank();
    }
}
