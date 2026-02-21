package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record AssignRolesRequestDto(
        @NotEmpty(message = "At least one role must be specified") List<UUID> roleIds
) {
}
