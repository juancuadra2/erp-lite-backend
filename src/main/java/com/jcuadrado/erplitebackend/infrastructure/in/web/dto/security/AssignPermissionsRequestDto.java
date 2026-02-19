package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record AssignPermissionsRequestDto(
        @NotEmpty(message = "At least one permission must be specified") List<UUID> permissionIds
) {
}
