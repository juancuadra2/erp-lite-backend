package com.jcuadrado.erplitebackend.application.command.security;

import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogFilter(
        UUID userId,
        String entity,
        AuditAction action,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
