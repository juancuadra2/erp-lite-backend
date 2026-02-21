package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record RoleResponseDto(
        UUID id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
