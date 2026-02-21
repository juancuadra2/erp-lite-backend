package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequestDto(
        @NotBlank(message = "Entity is required")
        @Size(max = 50, message = "Entity must not exceed 50 characters")
        String entity,

        @NotNull(message = "Action is required")
        String action,

        @Size(max = 255, message = "Condition must not exceed 255 characters")
        String condition,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {
}
