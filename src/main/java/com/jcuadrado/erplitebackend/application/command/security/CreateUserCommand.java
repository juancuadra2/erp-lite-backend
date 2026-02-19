package com.jcuadrado.erplitebackend.application.command.security;

import java.util.List;
import java.util.UUID;

public record CreateUserCommand(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        UUID documentTypeId,
        String documentNumber,
        List<UUID> roleIds,
        UUID createdBy
) {
}
