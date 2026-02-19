package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UpdatePaymentMethodRequestDto
 */
@DisplayName("UpdatePaymentMethodRequestDto Tests")
class UpdatePaymentMethodRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create DTO with builder")
    void shouldCreateDtoWithBuilder() {
        // When
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getCode()).isEqualTo("CASH");
        assertThat(dto.getName()).isEqualTo("Efectivo");
        assertThat(dto.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void shouldCreateDtoWithNoArgsConstructor() {
        // When
        UpdatePaymentMethodRequestDto dto = new UpdatePaymentMethodRequestDto();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getCode()).isNull();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEnabled()).isNull();
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void shouldCreateDtoWithAllArgsConstructor() {
        // When
        UpdatePaymentMethodRequestDto dto = new UpdatePaymentMethodRequestDto("CARD", "Tarjeta", false);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getCode()).isEqualTo("CARD");
        assertThat(dto.getName()).isEqualTo("Tarjeta");
        assertThat(dto.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should set and get all fields")
    void shouldSetAndGetAllFields() {
        // Given
        UpdatePaymentMethodRequestDto dto = new UpdatePaymentMethodRequestDto();

        // When
        dto.setCode("TRANSFER");
        dto.setName("Transferencia");
        dto.setEnabled(true);

        // Then
        assertThat(dto.getCode()).isEqualTo("TRANSFER");
        assertThat(dto.getName()).isEqualTo("Transferencia");
        assertThat(dto.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should validate successfully with valid data")
    void shouldValidateSuccessfullyWithValidData() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        // When
        Set<ConstraintViolation<UpdatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate successfully when enabled is null")
    void shouldValidateSuccessfullyWhenEnabledIsNull() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(null)
                .build();

        // When
        Set<ConstraintViolation<UpdatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when code is blank")
    void shouldFailValidationWhenCodeIsBlank() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("")
                .name("Efectivo")
                .enabled(true)
                .build();

        // When
        Set<ConstraintViolation<UpdatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Code is required"));
    }

    @Test
    @DisplayName("Should fail validation when code is null")
    void shouldFailValidationWhenCodeIsNull() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code(null)
                .name("Efectivo")
                .enabled(true)
                .build();

        // When
        Set<ConstraintViolation<UpdatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Code is required"));
    }

    @Test
    @DisplayName("Should fail validation when code exceeds max length")
    void shouldFailValidationWhenCodeExceedsMaxLength() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("A".repeat(31))
                .name("Efectivo")
                .enabled(true)
                .build();

        // When
        Set<ConstraintViolation<UpdatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must not exceed 30 characters"));
    }

    @Test
    @DisplayName("Should fail validation when code has invalid format")
    void shouldFailValidationWhenCodeHasInvalidFormat() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("cash-payment")
                .name("Efectivo")
                .enabled(true)
                .build();

        // When
        Set<ConstraintViolation<UpdatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must contain only uppercase letters"));
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("")
                .enabled(true)
                .build();

        // When
        Set<ConstraintViolation<UpdatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Name is required") || 
                                              v.getMessage().contains("must be between 1 and 100 characters"));
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void shouldFailValidationWhenNameIsNull() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name(null)
                .enabled(true)
                .build();

        // When
        Set<ConstraintViolation<UpdatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Name is required"));
    }

    @Test
    @DisplayName("Should fail validation when name exceeds max length")
    void shouldFailValidationWhenNameExceedsMaxLength() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("A".repeat(101))
                .enabled(true)
                .build();

        // When
        Set<ConstraintViolation<UpdatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must be between 1 and 100 characters"));
    }
}
