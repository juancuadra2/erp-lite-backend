package com.jcuadrado.erplitebackend.domain.exception.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityExceptionsTest {

    // ==================== SecurityDomainException ====================

    @Test
    @DisplayName("SecurityDomainException should propagate the provided message")
    void securityDomainException_shouldPropagateMessage() {
        SecurityDomainException ex = new SecurityDomainException("domain error");

        assertThat(ex.getMessage()).isEqualTo("domain error");
    }

    @Test
    @DisplayName("SecurityDomainException should propagate message and cause")
    void securityDomainException_shouldPropagateMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        SecurityDomainException ex = new SecurityDomainException("domain error", cause);

        assertThat(ex.getMessage()).isEqualTo("domain error");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    // ==================== UserNotFoundException ====================

    @Test
    @DisplayName("UserNotFoundException should propagate the provided message and extend SecurityDomainException")
    void userNotFoundException_shouldPropagateMessage() {
        UserNotFoundException ex = new UserNotFoundException("User not found: 123");

        assertThat(ex.getMessage()).isEqualTo("User not found: 123");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }

    // ==================== InvalidCredentialsException ====================

    @Test
    @DisplayName("InvalidCredentialsException should propagate the provided message and extend SecurityDomainException")
    void invalidCredentialsException_shouldPropagateMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Credenciales inválidas");

        assertThat(ex.getMessage()).isEqualTo("Credenciales inválidas");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }

    // ==================== AccountLockedException ====================

    @Test
    @DisplayName("AccountLockedException should propagate the provided message and extend SecurityDomainException")
    void accountLockedException_shouldPropagateMessage() {
        AccountLockedException ex = new AccountLockedException("Cuenta bloqueada");

        assertThat(ex.getMessage()).isEqualTo("Cuenta bloqueada");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }

    // ==================== DuplicateUsernameException ====================

    @Test
    @DisplayName("DuplicateUsernameException should propagate the provided message and extend SecurityDomainException")
    void duplicateUsernameException_shouldPropagateMessage() {
        DuplicateUsernameException ex = new DuplicateUsernameException("Username ya existe: admin");

        assertThat(ex.getMessage()).isEqualTo("Username ya existe: admin");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }

    // ==================== DuplicateEmailException ====================

    @Test
    @DisplayName("DuplicateEmailException should propagate the provided message and extend SecurityDomainException")
    void duplicateEmailException_shouldPropagateMessage() {
        DuplicateEmailException ex = new DuplicateEmailException("Email ya existe: user@example.com");

        assertThat(ex.getMessage()).isEqualTo("Email ya existe: user@example.com");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }

    // ==================== InvalidPasswordException ====================

    @Test
    @DisplayName("InvalidPasswordException should propagate the provided message and extend SecurityDomainException")
    void invalidPasswordException_shouldPropagateMessage() {
        InvalidPasswordException ex = new InvalidPasswordException("Password must have 8 characters");

        assertThat(ex.getMessage()).isEqualTo("Password must have 8 characters");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }

    // ==================== InvalidRefreshTokenException ====================

    @Test
    @DisplayName("InvalidRefreshTokenException should propagate the provided message and extend SecurityDomainException")
    void invalidRefreshTokenException_shouldPropagateMessage() {
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException("Refresh token inválido o revocado");

        assertThat(ex.getMessage()).isEqualTo("Refresh token inválido o revocado");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }

    // ==================== RoleNotFoundException ====================

    @Test
    @DisplayName("RoleNotFoundException should propagate the provided message and extend SecurityDomainException")
    void roleNotFoundException_shouldPropagateMessage() {
        RoleNotFoundException ex = new RoleNotFoundException("Role not found");

        assertThat(ex.getMessage()).isEqualTo("Role not found");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }

    // ==================== PermissionDeniedException ====================

    @Test
    @DisplayName("PermissionDeniedException should propagate the provided message and extend SecurityDomainException")
    void permissionDeniedException_shouldPropagateMessage() {
        PermissionDeniedException ex = new PermissionDeniedException("Access denied");

        assertThat(ex.getMessage()).isEqualTo("Access denied");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }

    // ==================== RoleInUseException ====================

    @Test
    @DisplayName("RoleInUseException should propagate the provided message and extend SecurityDomainException")
    void roleInUseException_shouldPropagateMessage() {
        RoleInUseException ex = new RoleInUseException("Role is in use and cannot be deleted");

        assertThat(ex.getMessage()).isEqualTo("Role is in use and cannot be deleted");
        assertThat(ex).isInstanceOf(SecurityDomainException.class);
    }
}
