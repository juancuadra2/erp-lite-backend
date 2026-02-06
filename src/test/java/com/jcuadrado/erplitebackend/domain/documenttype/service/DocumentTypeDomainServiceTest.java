package com.jcuadrado.erplitebackend.domain.documenttype.service;

import com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeConstraintException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DocumentTypeDomainService.
 */
@DisplayName("DocumentTypeDomainService Tests")
class DocumentTypeDomainServiceTest {

    private DocumentTypeDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new DocumentTypeDomainService();
    }

    @Test
    @DisplayName("Should normalize code to uppercase")
    void shouldNormalizeCodeToUppercase() {
        // Given
        String code = "nit";

        // When
        String normalized = domainService.normalizeCode(code);

        // Then
        assertEquals("NIT", normalized);
    }

    @Test
    @DisplayName("Should trim and normalize code")
    void shouldTrimAndNormalizeCode() {
        // Given
        String code = "  cc  ";

        // When
        String normalized = domainService.normalizeCode(code);

        // Then
        assertEquals("CC", normalized);
    }

    @Test
    @DisplayName("Should validate correct code")
    void shouldValidateCorrectCode() {
        // Given
        String code = "NIT";

        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should throw exception when code is null")
    void shouldThrowExceptionWhenCodeIsNull() {
        // Given
        String code = null;

        // When & Then
        assertThrows(DocumentTypeConstraintException.class,
                () -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should throw exception when code is empty")
    void shouldThrowExceptionWhenCodeIsEmpty() {
        // Given
        String code = "";

        // When & Then
        assertThrows(DocumentTypeConstraintException.class,
                () -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should throw exception when code is blank")
    void shouldThrowExceptionWhenCodeIsBlank() {
        // Given
        String code = "   ";

        // When & Then
        assertThrows(DocumentTypeConstraintException.class,
                () -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should throw exception when code is too short")
    void shouldThrowExceptionWhenCodeIsTooShort() {
        // Given
        String code = "X";

        // When & Then
        assertThrows(DocumentTypeConstraintException.class,
                () -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should throw exception when code is too long")
    void shouldThrowExceptionWhenCodeIsTooLong() {
        // Given
        String code = "ABCDEFGHIJK"; // 11 characters

        // When & Then
        assertThrows(DocumentTypeConstraintException.class,
                () -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should validate code with minimum length")
    void shouldValidateCodeWithMinimumLength() {
        // Given
        String code = "CC"; // 2 characters

        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should validate code with maximum length")
    void shouldValidateCodeWithMaximumLength() {
        // Given
        String code = "ABCDEFGHIJ"; // 10 characters

        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should validate alphanumeric code")
    void shouldValidateAlphanumericCode() {
        // Given
        String code = "ABC123";

        // When & Then
        assertDoesNotThrow(() -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should throw exception when code contains special characters")
    void shouldThrowExceptionWhenCodeContainsSpecialCharacters() {
        // Given
        String code = "NIT-01";

        // When & Then
        assertThrows(DocumentTypeConstraintException.class,
                () -> domainService.validateCode(code));
    }

    @Test
    @DisplayName("Should throw exception when code contains spaces")
    void shouldThrowExceptionWhenCodeContainsSpaces() {
        // Given
        String code = "NI T";

        // When & Then
        assertThrows(DocumentTypeConstraintException.class,
                () -> domainService.validateCode(code));
    }
}

