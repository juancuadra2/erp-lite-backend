package com.jcuadrado.erplitebackend.domain.service.documenttypes;

import com.jcuadrado.erplitebackend.domain.exception.documenttypes.InvalidDocumentTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DocumentTypeValidatorTest {

    private DocumentTypeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DocumentTypeValidator();
    }

    @Test
    void validateCode_shouldPassWithValidCode() {
        // Given
        String validCode = "NIT";

        // When & Then
        assertDoesNotThrow(() -> validator.validateCode(validCode));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validateCode_shouldThrowExceptionWhenCodeIsNullOrBlank(String code) {
        // When & Then
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Code is required");
    }

    @Test
    void validateCode_shouldThrowExceptionWhenCodeIsTooShort() {
        // Given
        String shortCode = "A";

        // When & Then
        assertThatThrownBy(() -> validator.validateCode(shortCode))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Code must be at least 2 characters");
    }

    @Test
    void validateCode_shouldThrowExceptionWhenCodeIsTooLong() {
        // Given
        String longCode = "12345678901"; // 11 characters

        // When & Then
        assertThatThrownBy(() -> validator.validateCode(longCode))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Code must not exceed 10 characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"NIT-123", "CC@", "AB#CD", "A B", "nit%"})
    void validateCode_shouldThrowExceptionWhenCodeContainsInvalidCharacters(String invalidCode) {
        // When & Then
        String trimmedCode = invalidCode.trim();
        assertThatThrownBy(() -> validator.validateCode(trimmedCode))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Code must contain only alphanumeric characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"NIT", "CC", "CE123", "A1B2C3", "12345"})
    void validateCode_shouldPassWithValidAlphanumericCodes(String validCode) {
        // When & Then
        assertDoesNotThrow(() -> validator.validateCode(validCode));
    }

    @Test
    void validateName_shouldPassWithValidName() {
        // Given
        String validName = "Número de Identificación Tributaria";

        // When & Then
        assertDoesNotThrow(() -> validator.validateName(validName));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validateName_shouldThrowExceptionWhenNameIsNullOrBlank(String name) {
        // When & Then
        assertThatThrownBy(() -> validator.validateName(name))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Name is required");
    }

    @Test
    void validateName_shouldThrowExceptionWhenNameIsTooLong() {
        // Given
        String longName = "A".repeat(201);

        // When & Then
        assertThatThrownBy(() -> validator.validateName(longName))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Name must not exceed 200 characters");
    }

    @Test
    void validateName_shouldPassWithMaxLengthName() {
        // Given
        String maxLengthName = "A".repeat(200);

        // When & Then
        assertDoesNotThrow(() -> validator.validateName(maxLengthName));
    }

    @Test
    void validateDescription_shouldPassWithNullDescription() {
        // Given
        // When & Then
        assertDoesNotThrow(() -> validator.validateDescription(null));
    }

    @Test
    void validateDescription_shouldPassWithValidDescription() {
        // Given
        String validDescription = "Documento de identificación tributaria para empresas en Colombia";

        // When & Then
        assertDoesNotThrow(() -> validator.validateDescription(validDescription));
    }

    @Test
    void validateDescription_shouldThrowExceptionWhenDescriptionIsTooLong() {
        // Given
        String longDescription = "A".repeat(501);

        // When & Then
        assertThatThrownBy(() -> validator.validateDescription(longDescription))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Description must not exceed 500 characters");
    }

    @Test
    void validateDescription_shouldPassWithMaxLengthDescription() {
        // Given
        String maxLengthDescription = "A".repeat(500);

        // When & Then
        assertDoesNotThrow(() -> validator.validateDescription(maxLengthDescription));
    }

    @Test
    void validateAll_shouldPassWithAllValidFields() {
        // Given
        String code = "NIT";
        String name = "Número de Identificación Tributaria";
        String description = "Documento tributario";

        // When & Then
        assertDoesNotThrow(() -> validator.validateAll(code, name, description));
    }

    @Test
    void validateAll_shouldThrowExceptionWhenCodeIsInvalid() {
        // Given
        String invalidCode = "A";
        String name = "Valid Name";
        String description = "Valid Description";

        // When & Then
        assertThatThrownBy(() -> validator.validateAll(invalidCode, name, description))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Code must be at least 2 characters");
    }

    @Test
    void validateAll_shouldThrowExceptionWhenNameIsInvalid() {
        // Given
        String code = "NIT";
        String invalidName = "";
        String description = "Valid Description";

        // When & Then
        assertThatThrownBy(() -> validator.validateAll(code, invalidName, description))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Name is required");
    }

    @Test
    void validateAll_shouldThrowExceptionWhenDescriptionIsInvalid() {
        // Given
        String code = "NIT";
        String name = "Valid Name";
        String invalidDescription = "A".repeat(501);

        // When & Then
        assertThatThrownBy(() -> validator.validateAll(code, name, invalidDescription))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Description must not exceed 500 characters");
    }

    @Test
    void validateAll_shouldPassWithNullDescription() {
        // Given
        String code = "NIT";
        String name = "Valid Name";

        // When & Then
        assertDoesNotThrow(() -> validator.validateAll(code, name, null));
    }
}
