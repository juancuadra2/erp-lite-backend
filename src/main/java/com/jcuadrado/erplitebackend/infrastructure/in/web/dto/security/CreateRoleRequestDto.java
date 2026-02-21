package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateRoleRequestDto(
        @NotBlank(message = "Role name is required")
        @Size(min = 1, max = 50, message = "Role name must be between 1 and 50 characters")
        String name,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        List<UUID> permissionIds
) {
}
