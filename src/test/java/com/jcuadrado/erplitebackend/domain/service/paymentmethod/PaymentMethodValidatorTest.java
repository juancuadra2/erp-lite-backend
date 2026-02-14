package com.jcuadrado.erplitebackend.domain.service.paymentmethod;

import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.InvalidPaymentMethodCodeException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.InvalidPaymentMethodDataException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for PaymentMethodValidator
 */
class PaymentMethodValidatorTest {

    private final PaymentMethodValidator validator = new PaymentMethodValidator();

    // ========== Code Validation Tests ==========

    @Test
    void validateCode_shouldPassForValidCode() {
        // Given
        String code = "CASH";

        // When & Then
        assertDoesNotThrow(() -> validator.validateCode(code));
    }

    @Test
    void validateCode_shouldPassForCodeWithUnderscores() {
        // Given
        String code = "CREDIT_CARD";

        // When & Then
        assertDoesNotThrow(() -> validator.validateCode(code));
    }

    @Test
    void validateCode_shouldPassForCodeWithNumbers() {
        // Given
        String code = "PAYMENT123";

        // When & Then
        assertDoesNotThrow(() -> validator.validateCode(code));
    }

    @Test
    void validateCode_shouldThrowExceptionWhenCodeIsNull() {
        // Given
        String code = null;

        // When & Then
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidPaymentMethodCodeException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void validateCode_shouldThrowExceptionWhenCodeIsEmpty() {
        // Given
        String code = "";

        // When & Then
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidPaymentMethodCodeException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void validateCode_shouldThrowExceptionWhenCodeIsBlank() {
        // Given
        String code = "   ";

        // When & Then
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidPaymentMethodCodeException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void validateCode_shouldThrowExceptionWhenCodeExceedsMaxLength() {
        // Given
        String code = "A".repeat(31); // 31 characters

        // When & Then
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidPaymentMethodCodeException.class)
                .hasMessageContaining("cannot exceed 30 characters");
    }

    @Test
    void validateCode_shouldThrowExceptionWhenCodeContainsLowercase() {
        // Given
        String code = "Cash";

        // When & Then
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidPaymentMethodCodeException.class)
                .hasMessageContaining("must contain only uppercase letters");
    }

    @Test
    void validateCode_shouldThrowExceptionWhenCodeContainsSpaces() {
        // Given
        String code = "CREDIT CARD";

        // When & Then
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidPaymentMethodCodeException.class)
                .hasMessageContaining("must contain only uppercase letters");
    }

    @Test
    void validateCode_shouldThrowExceptionWhenCodeContainsSpecialCharacters() {
        // Given
        String code = "CASH@123";

        // When & Then
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidPaymentMethodCodeException.class)
                .hasMessageContaining("must contain only uppercase letters");
    }

    @Test
    void validateCode_shouldPassForMaxLengthCode() {
        // Given
        String code = "A".repeat(30); // 30 characters (max)

        // When & Then
        assertDoesNotThrow(() -> validator.validateCode(code));
    }

    // ========== Name Validation Tests ==========

    @Test
    void validateName_shouldPassForValidName() {
        // Given
        String name = "Efectivo";

        // When & Then
        assertDoesNotThrow(() -> validator.validateName(name));
    }

    @Test
    void validateName_shouldThrowExceptionWhenNameIsNull() {
        // Given
        String name = null;

        // When & Then
        assertThatThrownBy(() -> validator.validateName(name))
                .isInstanceOf(InvalidPaymentMethodDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void validateName_shouldThrowExceptionWhenNameIsEmpty() {
        // Given
        String name = "";

        // When & Then
        assertThatThrownBy(() -> validator.validateName(name))
                .isInstanceOf(InvalidPaymentMethodDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void validateName_shouldThrowExceptionWhenNameIsBlank() {
        // Given
        String name = "   ";

        // When & Then
        assertThatThrownBy(() -> validator.validateName(name))
                .isInstanceOf(InvalidPaymentMethodDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void validateName_shouldThrowExceptionWhenNameIsNonWhitespaceButTrimsToEmpty() {
        // Given
        String name = "\u0000";

        // When & Then
        assertThatThrownBy(() -> validator.validateName(name))
                .isInstanceOf(InvalidPaymentMethodDataException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void validateName_shouldThrowExceptionWhenNameExceedsMaxLength() {
        // Given
        String name = "A".repeat(101); // 101 characters

        // When & Then
        assertThatThrownBy(() -> validator.validateName(name))
                .isInstanceOf(InvalidPaymentMethodDataException.class)
                .hasMessageContaining("cannot exceed 100 characters");
    }

    @Test
    void validateName_shouldPassForMaxLengthName() {
        // Given
        String name = "A".repeat(100); // 100 characters (max)

        // When & Then
        assertDoesNotThrow(() -> validator.validateName(name));
    }

    @Test
    void validateName_shouldPassForMinLengthName() {
        // Given
        String name = "A"; // 1 character (min)

        // When & Then
        assertDoesNotThrow(() -> validator.validateName(name));
    }

    // ========== Validate All Tests ==========

    @Test
    void validateAll_shouldPassForValidData() {
        // Given
        String code = "CASH";
        String name = "Efectivo";

        // When & Then
        assertDoesNotThrow(() -> validator.validateAll(code, name));
    }

    @Test
    void validateAll_shouldThrowExceptionWhenCodeIsInvalid() {
        // Given
        String code = "cash"; // lowercase
        String name = "Efectivo";

        // When & Then
        assertThatThrownBy(() -> validator.validateAll(code, name))
                .isInstanceOf(InvalidPaymentMethodCodeException.class);
    }

    @Test
    void validateAll_shouldThrowExceptionWhenNameIsInvalid() {
        // Given
        String code = "CASH";
        String name = ""; // empty

        // When & Then
        assertThatThrownBy(() -> validator.validateAll(code, name))
                .isInstanceOf(InvalidPaymentMethodDataException.class);
    }
}
