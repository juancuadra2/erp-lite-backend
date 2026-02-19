package com.jcuadrado.erplitebackend.domain.model.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AuditLog {

    private UUID id;
    private UUID userId;
    private String username;
    private String entity;
    private UUID entityId;
    private AuditAction action;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;

    public static AuditLog create(UUID userId, String username, String entity,
                                  UUID entityId, AuditAction action,
                                  String ipAddress, String userAgent) {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username(username)
                .entity(entity)
                .entityId(entityId)
                .action(action)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
