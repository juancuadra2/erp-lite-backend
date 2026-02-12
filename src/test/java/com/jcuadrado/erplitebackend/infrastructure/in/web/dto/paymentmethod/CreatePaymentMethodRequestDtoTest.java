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
 * Unit tests for CreatePaymentMethodRequestDto
 */
@DisplayName("CreatePaymentMethodRequestDto Tests")
class CreatePaymentMethodRequestDtoTest {

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
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getCode()).isEqualTo("CASH");
        assertThat(dto.getName()).isEqualTo("Efectivo");
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void shouldCreateDtoWithNoArgsConstructor() {
        // When
        CreatePaymentMethodRequestDto dto = new CreatePaymentMethodRequestDto();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getCode()).isNull();
        assertThat(dto.getName()).isNull();
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void shouldCreateDtoWithAllArgsConstructor() {
        // When
        CreatePaymentMethodRequestDto dto = new CreatePaymentMethodRequestDto("CARD", "Tarjeta");

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getCode()).isEqualTo("CARD");
        assertThat(dto.getName()).isEqualTo("Tarjeta");
    }

    @Test
    @DisplayName("Should set and get all fields")
    void shouldSetAndGetAllFields() {
        // Given
        CreatePaymentMethodRequestDto dto = new CreatePaymentMethodRequestDto();

        // When
        dto.setCode("TRANSFER");
        dto.setName("Transferencia");

        // Then
        assertThat(dto.getCode()).isEqualTo("TRANSFER");
        assertThat(dto.getName()).isEqualTo("Transferencia");
    }

    @Test
    @DisplayName("Should validate successfully with valid data")
    void shouldValidateSuccessfullyWithValidData() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        // When
        Set<ConstraintViolation<CreatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when code is blank")
    void shouldFailValidationWhenCodeIsBlank() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("")
                .name("Efectivo")
                .build();

        // When
        Set<ConstraintViolation<CreatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Code is required"));
    }

    @Test
    @DisplayName("Should fail validation when code is null")
    void shouldFailValidationWhenCodeIsNull() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code(null)
                .name("Efectivo")
                .build();

        // When
        Set<ConstraintViolation<CreatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Code is required"));
    }

    @Test
    @DisplayName("Should fail validation when code exceeds max length")
    void shouldFailValidationWhenCodeExceedsMaxLength() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("A".repeat(31))
                .name("Efectivo")
                .build();

        // When
        Set<ConstraintViolation<CreatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must not exceed 30 characters"));
    }

    @Test
    @DisplayName("Should fail validation when code has invalid format")
    void shouldFailValidationWhenCodeHasInvalidFormat() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("cash-payment")
                .name("Efectivo")
                .build();

        // When
        Set<ConstraintViolation<CreatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must contain only uppercase letters"));
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("")
                .build();

        // When
        Set<ConstraintViolation<CreatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Name is required") || 
                                              v.getMessage().contains("must be between 1 and 100 characters"));
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void shouldFailValidationWhenNameIsNull() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name(null)
                .build();

        // When
        Set<ConstraintViolation<CreatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Name is required"));
    }

    @Test
    @DisplayName("Should fail validation when name exceeds max length")
    void shouldFailValidationWhenNameExceedsMaxLength() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("A".repeat(101))
                .build();

        // When
        Set<ConstraintViolation<CreatePaymentMethodRequestDto>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("must be between 1 and 100 characters"));
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        CreatePaymentMethodRequestDto dto1 = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        CreatePaymentMethodRequestDto dto2 = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        CreatePaymentMethodRequestDto dto3 = CreatePaymentMethodRequestDto.builder()
                .code("CARD")
                .name("Tarjeta")
                .build();

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("CASH");
        assertThat(toString).contains("Efectivo");
    }
}
