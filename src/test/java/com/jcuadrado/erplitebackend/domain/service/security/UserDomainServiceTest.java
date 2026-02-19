package com.jcuadrado.erplitebackend.domain.service.security;

import com.jcuadrado.erplitebackend.domain.exception.security.InvalidPasswordException;
import com.jcuadrado.erplitebackend.domain.exception.security.SecurityDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDomainServiceTest {

    private UserDomainService service;

    @BeforeEach
    void setUp() {
        service = new UserDomainService();
    }

    // ==================== validateUsername ====================

    @Test
    @DisplayName("validateUsername should not throw for a valid alphanumeric username")
    void validateUsername_shouldNotThrowForValidUsername() {
        assertThatCode(() -> service.validateUsername("john_doe123"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUsername should not throw for a username at minimum length (3 chars)")
    void validateUsername_shouldNotThrowForMinLengthUsername() {
        assertThatCode(() -> service.validateUsername("abc"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUsername should not throw for a username at maximum length (50 chars)")
    void validateUsername_shouldNotThrowForMaxLengthUsername() {
        String fiftyChars = "a".repeat(50);
        assertThatCode(() -> service.validateUsername(fiftyChars))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateUsername should throw SecurityDomainException when username is too short (less than 3 chars)")
    void validateUsername_shouldThrowWhenTooShort() {
        assertThatThrownBy(() -> service.validateUsername("ab"))
                .isInstanceOf(SecurityDomainException.class);
    }

    @Test
    @DisplayName("validateUsername should throw SecurityDomainException when username is too long (more than 50 chars)")
    void validateUsername_shouldThrowWhenTooLong() {
        String fiftyOneChars = "a".repeat(51);
        assertThatThrownBy(() -> service.validateUsername(fiftyOneChars))
                .isInstanceOf(SecurityDomainException.class);
    }

    @Test
    @DisplayName("validateUsername should throw SecurityDomainException when username contains invalid characters")
    void validateUsername_shouldThrowWhenContainsInvalidCharacters() {
        assertThatThrownBy(() -> service.validateUsername("john doe"))
                .isInstanceOf(SecurityDomainException.class);
    }

    @Test
    @DisplayName("validateUsername should throw SecurityDomainException when username contains a hyphen")
    void validateUsername_shouldThrowWhenContainsHyphen() {
        assertThatThrownBy(() -> service.validateUsername("john-doe"))
                .isInstanceOf(SecurityDomainException.class);
    }

    @Test
    @DisplayName("validateUsername should throw SecurityDomainException when username is null")
    void validateUsername_shouldThrowWhenNull() {
        assertThatThrownBy(() -> service.validateUsername(null))
                .isInstanceOf(SecurityDomainException.class);
    }

    @Test
    @DisplayName("validateUsername should throw SecurityDomainException when username is blank")
    void validateUsername_shouldThrowWhenBlank() {
        assertThatThrownBy(() -> service.validateUsername("   "))
                .isInstanceOf(SecurityDomainException.class);
    }

    // ==================== validateEmail ====================

    @Test
    @DisplayName("validateEmail should not throw for a valid email address")
    void validateEmail_shouldNotThrowForValidEmail() {
        assertThatCode(() -> service.validateEmail("user@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmail should not throw for a valid email with subdomain")
    void validateEmail_shouldNotThrowForEmailWithSubdomain() {
        assertThatCode(() -> service.validateEmail("user@mail.example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateEmail should throw SecurityDomainException for an email without @ symbol")
    void validateEmail_shouldThrowForMissingAtSymbol() {
        assertThatThrownBy(() -> service.validateEmail("userexample.com"))
                .isInstanceOf(SecurityDomainException.class);
    }

    @Test
    @DisplayName("validateEmail should throw SecurityDomainException for an email without domain")
    void validateEmail_shouldThrowForMissingDomain() {
        assertThatThrownBy(() -> service.validateEmail("user@"))
                .isInstanceOf(SecurityDomainException.class);
    }

    @Test
    @DisplayName("validateEmail should throw SecurityDomainException when email is null")
    void validateEmail_shouldThrowWhenNull() {
        assertThatThrownBy(() -> service.validateEmail(null))
                .isInstanceOf(SecurityDomainException.class);
    }

    @Test
    @DisplayName("validateEmail should throw SecurityDomainException when email is blank")
    void validateEmail_shouldThrowWhenBlank() {
        assertThatThrownBy(() -> service.validateEmail("   "))
                .isInstanceOf(SecurityDomainException.class);
    }

    // ==================== validatePassword ====================

    @Test
    @DisplayName("validatePassword should not throw for a password meeting all requirements")
    void validatePassword_shouldNotThrowForValidPassword() {
        assertThatCode(() -> service.validatePassword("Secure@1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validatePassword should throw InvalidPasswordException when password is too short (less than 8 chars)")
    void validatePassword_shouldThrowWhenTooShort() {
        assertThatThrownBy(() -> service.validatePassword("Sh@1"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("validatePassword should throw InvalidPasswordException when password has no uppercase letter")
    void validatePassword_shouldThrowWhenNoUppercase() {
        assertThatThrownBy(() -> service.validatePassword("secure@1"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("validatePassword should throw InvalidPasswordException when password has no lowercase letter")
    void validatePassword_shouldThrowWhenNoLowercase() {
        assertThatThrownBy(() -> service.validatePassword("SECURE@1"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("validatePassword should throw InvalidPasswordException when password has no digit")
    void validatePassword_shouldThrowWhenNoDigit() {
        assertThatThrownBy(() -> service.validatePassword("Secure@X"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("validatePassword should throw InvalidPasswordException when password has no special character")
    void validatePassword_shouldThrowWhenNoSpecialCharacter() {
        assertThatThrownBy(() -> service.validatePassword("Secure123"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("validatePassword should throw InvalidPasswordException when password is null")
    void validatePassword_shouldThrowWhenNull() {
        assertThatThrownBy(() -> service.validatePassword(null))
                .isInstanceOf(InvalidPasswordException.class);
    }

    // ==================== ensureNotProtectedUser ====================

    @Test
    @DisplayName("ensureNotProtectedUser should throw SecurityDomainException when targetId equals protectedId")
    void ensureNotProtectedUser_shouldThrowWhenSameId() {
        UUID sharedId = UUID.randomUUID();

        assertThatThrownBy(() -> service.ensureNotProtectedUser(sharedId, sharedId))
                .isInstanceOf(SecurityDomainException.class);
    }

    @Test
    @DisplayName("ensureNotProtectedUser should not throw when targetId is different from protectedId")
    void ensureNotProtectedUser_shouldNotThrowWhenDifferentIds() {
        UUID targetId = UUID.randomUUID();
        UUID protectedId = UUID.randomUUID();

        assertThatCode(() -> service.ensureNotProtectedUser(targetId, protectedId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ensureNotProtectedUser should not throw when protectedId is null")
    void ensureNotProtectedUser_shouldNotThrowWhenProtectedIdIsNull() {
        UUID targetId = UUID.randomUUID();

        assertThatCode(() -> service.ensureNotProtectedUser(targetId, null))
                .doesNotThrowAnyException();
    }
}
