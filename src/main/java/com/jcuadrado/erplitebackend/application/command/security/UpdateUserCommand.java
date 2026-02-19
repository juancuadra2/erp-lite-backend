package com.jcuadrado.erplitebackend.application.command.security;

import java.util.UUID;

public record UpdateUserCommand(
        String email,
        String firstName,
        String lastName,
        UUID documentTypeId,
        String documentNumber,
        UUID updatedBy
) {
}
