package com.jcuadrado.erplitebackend.domain.exception.taxtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TaxType exceptions
 */
@DisplayName("TaxType Exceptions - Unit Tests")
class TaxTypeExceptionsTest {

    @Test
    @DisplayName("TaxTypeNotFoundException with message should create exception")
    void taxTypeNotFoundException_withMessage_shouldCreateException() {
        // When
        TaxTypeNotFoundException exception = new TaxTypeNotFoundException("Tax type not found");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Tax type not found");
    }

    @Test
    @DisplayName("TaxTypeNotFoundException with UUID should format message")
    void taxTypeNotFoundException_withUuid_shouldFormatMessage() {
        // Given
        UUID uuid = UUID.randomUUID();

        // When
        TaxTypeNotFoundException exception = new TaxTypeNotFoundException(uuid);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("Tax type not found with UUID");
        assertThat(exception.getMessage()).contains(uuid.toString());
    }

    @Test
    @DisplayName("DuplicateTaxTypeCodeException with message should create exception")
    void duplicateTaxTypeCodeException_withMessage_shouldCreateException() {
        // When
        DuplicateTaxTypeCodeException exception = new DuplicateTaxTypeCodeException("Duplicate code IVA19");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Duplicate code IVA19");
    }

    @Test
    @DisplayName("DuplicateTaxTypeCodeException should inherit from RuntimeException")
    void duplicateTaxTypeCodeException_shouldInheritFromRuntimeException() {
        // When
        DuplicateTaxTypeCodeException exception = new DuplicateTaxTypeCodeException("Duplicate");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InvalidTaxTypeCodeException with message should create exception")
    void invalidTaxTypeCodeException_withMessage_shouldCreateException() {
        // When
        InvalidTaxTypeCodeException exception = new InvalidTaxTypeCodeException("Code cannot be empty");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Code cannot be empty");
    }

    @Test
    @DisplayName("InvalidTaxTypeCodeException should inherit from RuntimeException")
    void invalidTaxTypeCodeException_shouldInheritFromRuntimeException() {
        // When
        InvalidTaxTypeCodeException exception = new InvalidTaxTypeCodeException("Invalid code");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InvalidTaxPercentageException with message should create exception")
    void invalidTaxPercentageException_withMessage_shouldCreateException() {
        // When
        InvalidTaxPercentageException exception = new InvalidTaxPercentageException("Percentage cannot be null");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Percentage cannot be null");
    }

    @Test
    @DisplayName("InvalidTaxPercentageException should inherit from RuntimeException")
    void invalidTaxPercentageException_shouldInheritFromRuntimeException() {
        // When
        InvalidTaxPercentageException exception = new InvalidTaxPercentageException("Invalid percentage");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InvalidTaxTypeDataException with message should create exception")
    void invalidTaxTypeDataException_withMessage_shouldCreateException() {
        // When
        InvalidTaxTypeDataException exception = new InvalidTaxTypeDataException("Invalid tax type data");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Invalid tax type data");
    }

    @Test
    @DisplayName("InvalidTaxTypeDataException should inherit from RuntimeException")
    void invalidTaxTypeDataException_shouldInheritFromRuntimeException() {
        // When
        InvalidTaxTypeDataException exception = new InvalidTaxTypeDataException("Invalid data");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("TaxTypeConstraintException with message should create exception")
    void taxTypeConstraintException_withMessage_shouldCreateException() {
        // When
        TaxTypeConstraintException exception = new TaxTypeConstraintException("Constraint violation");

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Constraint violation");
    }

    @Test
    @DisplayName("TaxTypeConstraintException should inherit from RuntimeException")
    void taxTypeConstraintException_shouldInheritFromRuntimeException() {
        // When
        TaxTypeConstraintException exception = new TaxTypeConstraintException("Cannot delete");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
