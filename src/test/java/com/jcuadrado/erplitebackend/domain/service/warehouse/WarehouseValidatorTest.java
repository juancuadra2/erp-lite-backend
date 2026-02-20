package com.jcuadrado.erplitebackend.domain.service.warehouse;

import com.jcuadrado.erplitebackend.domain.exception.warehouse.InvalidWarehouseDataException;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarehouseValidatorTest {

    private WarehouseValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WarehouseValidator();
    }

    // ── validateCode ──────────────────────────────────────────────

    @Test
    @DisplayName("validateCode should pass for valid code")
    void validateCode_shouldPass_forValidCode() {
        assertThatNoException().isThrownBy(() -> validator.validateCode("BOD-001"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("validateCode should throw for blank code")
    void validateCode_shouldThrow_forBlankCode(String code) {
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    @Test
    @DisplayName("validateCode should throw for null code")
    void validateCode_shouldThrow_forNullCode() {
        assertThatThrownBy(() -> validator.validateCode(null))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    @Test
    @DisplayName("validateCode should throw for code shorter than 3 chars")
    void validateCode_shouldThrow_forCodeTooShort() {
        assertThatThrownBy(() -> validator.validateCode("AB"))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    @Test
    @DisplayName("validateCode should throw for code longer than 20 chars")
    void validateCode_shouldThrow_forCodeTooLong() {
        assertThatThrownBy(() -> validator.validateCode("A".repeat(21)))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bod-001", "bod 001", "BOD_001", "BOD.001"})
    @DisplayName("validateCode should throw for code with invalid characters")
    void validateCode_shouldThrow_forInvalidPattern(String code) {
        assertThatThrownBy(() -> validator.validateCode(code))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    // ── validateName ─────────────────────────────────────────────

    @Test
    @DisplayName("validateName should pass for valid name")
    void validateName_shouldPass_forValidName() {
        assertThatNoException().isThrownBy(() -> validator.validateName("Bodega Principal"));
    }

    @Test
    @DisplayName("validateName should throw for null name")
    void validateName_shouldThrow_forNullName() {
        assertThatThrownBy(() -> validator.validateName(null))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    @Test
    @DisplayName("validateName should throw for name shorter than 3 chars")
    void validateName_shouldThrow_forNameTooShort() {
        assertThatThrownBy(() -> validator.validateName("AB"))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    @Test
    @DisplayName("validateName should throw for name longer than 100 chars")
    void validateName_shouldThrow_forNameTooLong() {
        assertThatThrownBy(() -> validator.validateName("A".repeat(101)))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    // ── validateType ─────────────────────────────────────────────

    @Test
    @DisplayName("validateType should pass for non-null type")
    void validateType_shouldPass_forNonNullType() {
        assertThatNoException().isThrownBy(() -> validator.validateType(WarehouseType.SUCURSAL));
    }

    @Test
    @DisplayName("validateType should throw for null type")
    void validateType_shouldThrow_forNullType() {
        assertThatThrownBy(() -> validator.validateType(null))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    // ── validateEmail ─────────────────────────────────────────────

    @Test
    @DisplayName("validateEmail should pass for valid email")
    void validateEmail_shouldPass_forValidEmail() {
        assertThatNoException().isThrownBy(() -> validator.validateEmail("bodega@empresa.com"));
    }

    @Test
    @DisplayName("validateEmail should pass when email is null")
    void validateEmail_shouldPass_whenNull() {
        assertThatNoException().isThrownBy(() -> validator.validateEmail(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"notanemail", "missing@", "@nodomain"})
    @DisplayName("validateEmail should throw for invalid email format")
    void validateEmail_shouldThrow_forInvalidFormat(String email) {
        assertThatThrownBy(() -> validator.validateEmail(email))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }

    // ── validatePhone ─────────────────────────────────────────────

    @Test
    @DisplayName("validatePhone should pass for valid phone")
    void validatePhone_shouldPass_forValidPhone() {
        assertThatNoException().isThrownBy(() -> validator.validatePhone("3001234567"));
    }

    @Test
    @DisplayName("validatePhone should pass when phone is null")
    void validatePhone_shouldPass_whenNull() {
        assertThatNoException().isThrownBy(() -> validator.validatePhone(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "abc123456", "!@#$%^&*()"})
    @DisplayName("validatePhone should throw for invalid phone format")
    void validatePhone_shouldThrow_forInvalidFormat(String phone) {
        assertThatThrownBy(() -> validator.validatePhone(phone))
                .isInstanceOf(InvalidWarehouseDataException.class);
    }
}
