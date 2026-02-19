package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AuditLogResponseDto(
        UUID id,
        UUID userId,
        String username,
        String entity,
        UUID entityId,
        String action,
        String oldValue,
        String newValue,
        String ipAddress,
        String userAgent,
        LocalDateTime timestamp
) {
}
