package com.jcuadrado.erplitebackend.domain.service.security;

import com.jcuadrado.erplitebackend.domain.exception.security.InvalidPasswordException;
import com.jcuadrado.erplitebackend.domain.exception.security.SecurityDomainException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDomainService {

    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 50;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new SecurityDomainException("Username no puede estar vacío");
        }
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            throw new SecurityDomainException(
                    "Username debe tener entre " + USERNAME_MIN_LENGTH + " y " + USERNAME_MAX_LENGTH + " caracteres");
        }
        if (!username.matches(USERNAME_PATTERN)) {
            throw new SecurityDomainException("Username solo puede contener letras, números y guión bajo");
        }
    }

    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new SecurityDomainException("Email no puede estar vacío");
        }
        if (!email.matches(EMAIL_PATTERN)) {
            throw new SecurityDomainException("Email no tiene formato válido");
        }
    }

    public void validatePassword(String password) {
        List<String> violations = new ArrayList<>();

        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            violations.add("Debe tener mínimo " + PASSWORD_MIN_LENGTH + " caracteres");
        }
        if (password != null && !password.matches(".*[A-Z].*")) {
            violations.add("Debe contener al menos 1 letra mayúscula");
        }
        if (password != null && !password.matches(".*[a-z].*")) {
            violations.add("Debe contener al menos 1 letra minúscula");
        }
        if (password != null && !password.matches(".*[0-9].*")) {
            violations.add("Debe contener al menos 1 número");
        }
        if (password != null && !password.matches(".*[!@#$%^&*].*")) {
            violations.add("Debe contener al menos 1 carácter especial (!@#$%^&*)");
        }

        if (!violations.isEmpty()) {
            throw new InvalidPasswordException(String.join("; ", violations));
        }
    }

    public void ensureNotProtectedUser(UUID targetUserId, UUID protectedAdminId) {
        if (protectedAdminId != null && protectedAdminId.equals(targetUserId)) {
            throw new SecurityDomainException("No se puede eliminar o modificar el usuario administrador raíz");
        }
    }
}
