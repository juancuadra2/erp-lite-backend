package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateUserRequestDto(
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        UUID documentTypeId,

        @Size(max = 20, message = "Document number must not exceed 20 characters")
        String documentNumber
) {
}
