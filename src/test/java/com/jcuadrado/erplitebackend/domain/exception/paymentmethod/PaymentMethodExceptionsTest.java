package com.jcuadrado.erplitebackend.domain.exception.paymentmethod;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaymentMethod exceptions
 */
class PaymentMethodExceptionsTest {

    // ==================== PaymentMethodNotFoundException Tests ====================

    @Test
    void paymentMethodNotFoundException_withMessage_shouldCreateException() {
        // When
        PaymentMethodNotFoundException exception = new PaymentMethodNotFoundException("Payment method not found");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Payment method not found");
    }

    @Test
    void paymentMethodNotFoundException_withFieldAndValue_shouldFormatMessage() {
        // When
        PaymentMethodNotFoundException exception = new PaymentMethodNotFoundException("uuid", "123e4567-e89b-12d3-a456-426614174000");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("Payment method not found with uuid: 123e4567-e89b-12d3-a456-426614174000");
    }

    // ==================== DuplicatePaymentMethodCodeException Tests ====================

    @Test
    void duplicatePaymentMethodCodeException_withCode_shouldFormatMessage() {
        // When
        DuplicatePaymentMethodCodeException exception = new DuplicatePaymentMethodCodeException("CASH");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("CASH");
    }

    @Test
    void duplicatePaymentMethodCodeException_withMessageAndCause_shouldCreateException() {
        // Given
        Throwable cause = new RuntimeException("Database error");

        // When
        DuplicatePaymentMethodCodeException exception = new DuplicatePaymentMethodCodeException("Duplicate code", cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Duplicate code");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    // ==================== InvalidPaymentMethodCodeException Tests ====================

    @Test
    void invalidPaymentMethodCodeException_withMessage_shouldCreateException() {
        // When
        InvalidPaymentMethodCodeException exception = new InvalidPaymentMethodCodeException("Code cannot be empty");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Code cannot be empty");
    }

    @Test
    void invalidPaymentMethodCodeException_withMessageAndCause_shouldCreateException() {
        // Given
        Throwable cause = new IllegalArgumentException("Invalid format");

        // When
        InvalidPaymentMethodCodeException exception = new InvalidPaymentMethodCodeException("Invalid code format", cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Invalid code format");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    // ==================== InvalidPaymentMethodDataException Tests ====================

    @Test
    void invalidPaymentMethodDataException_withMessage_shouldCreateException() {
        // When
        InvalidPaymentMethodDataException exception = new InvalidPaymentMethodDataException("Invalid payment method data");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Invalid payment method data");
    }

    @Test
    void invalidPaymentMethodDataException_withFieldAndMessage_shouldFormatMessage() {
        // When
        InvalidPaymentMethodDataException exception = new InvalidPaymentMethodDataException("name", "Name is required");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("name");
        assertThat(exception.getMessage()).contains("Name is required");
    }

    @Test
    void invalidPaymentMethodDataException_withMessageAndCause_shouldCreateException() {
        // Given
        Throwable cause = new NullPointerException("Null value");

        // When
        InvalidPaymentMethodDataException exception = new InvalidPaymentMethodDataException("Invalid data", cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Invalid data");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    // ==================== PaymentMethodConstraintException Tests ====================

    @Test
    void paymentMethodConstraintException_withMessage_shouldCreateException() {
        // When
        PaymentMethodConstraintException exception = new PaymentMethodConstraintException("Constraint violation");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Constraint violation");
    }

    @Test
    void paymentMethodConstraintException_withMessageAndCause_shouldCreateException() {
        // Given
        Throwable cause = new RuntimeException("Database constraint");

        // When
        PaymentMethodConstraintException exception = new PaymentMethodConstraintException("Cannot delete: used in transactions", cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Cannot delete: used in transactions");
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
