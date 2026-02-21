package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserResponseDto(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        UUID documentTypeId,
        String documentNumber,
        boolean active,
        LocalDateTime lastLogin,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
