package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PermissionResponseDto(
        UUID id,
        String entity,
        String action,
        String condition,
        String description
) {
}
