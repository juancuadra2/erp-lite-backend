package com.jcuadrado.erplitebackend.domain.service.geography;

import com.jcuadrado.erplitebackend.domain.exception.geography.InvalidGeographyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class GeographyValidatorTest {

    private GeographyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GeographyValidator();
    }

    // ==================== Department Code ====================

    @Test
    void validateDepartmentCode_shouldPassWithValidCode() {
        assertDoesNotThrow(() -> validator.validateDepartmentCode("05"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "11", "99", "42"})
    void validateDepartmentCode_shouldPassWithValidTwoDigitCodes(String code) {
        assertDoesNotThrow(() -> validator.validateDepartmentCode(code));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validateDepartmentCode_shouldThrowWhenCodeIsNullOrBlank(String code) {
        assertThatThrownBy(() -> validator.validateDepartmentCode(code))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Department code is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"5", "123", "AB", "0A", "1", "1234", "A5"})
    void validateDepartmentCode_shouldThrowWhenCodeIsNotExactlyTwoDigits(String code) {
        assertThatThrownBy(() -> validator.validateDepartmentCode(code))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Department code must be exactly 2 digits");
    }

    // ==================== Municipality Code ====================

    @Test
    void validateMunicipalityCode_shouldPassWithValidCode() {
        assertDoesNotThrow(() -> validator.validateMunicipalityCode("05001"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000", "12345", "99999", "05001"})
    void validateMunicipalityCode_shouldPassWithValidFiveDigitCodes(String code) {
        assertDoesNotThrow(() -> validator.validateMunicipalityCode(code));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validateMunicipalityCode_shouldThrowWhenCodeIsNullOrBlank(String code) {
        assertThatThrownBy(() -> validator.validateMunicipalityCode(code))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Municipality code is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0500", "050011", "ABCDE", "0500A", "1", "12", "123", "1234"})
    void validateMunicipalityCode_shouldThrowWhenCodeIsNotExactlyFiveDigits(String code) {
        assertThatThrownBy(() -> validator.validateMunicipalityCode(code))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Municipality code must be exactly 5 digits");
    }

    // ==================== Name ====================

    @Test
    void validateName_shouldPassWithValidName() {
        assertDoesNotThrow(() -> validator.validateName("Antioquia"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validateName_shouldThrowWhenNameIsNullOrBlank(String name) {
        assertThatThrownBy(() -> validator.validateName(name))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Name is required");
    }

    @Test
    void validateName_shouldThrowWhenNameIsTooLong() {
        assertThatThrownBy(() -> validator.validateName("A".repeat(101)))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Name must not exceed 100 characters");
    }

    @Test
    void validateName_shouldPassWithMaxLengthName() {
        assertDoesNotThrow(() -> validator.validateName("A".repeat(100)));
    }

    @Test
    void validateName_shouldPassWithSingleCharacterName() {
        assertDoesNotThrow(() -> validator.validateName("A"));
    }

    // ==================== Composite Validations ====================

    @Test
    void validateDepartment_shouldPassWithValidCodeAndName() {
        assertDoesNotThrow(() -> validator.validateDepartment("05", "Antioquia"));
    }

    @Test
    void validateDepartment_shouldThrowWhenCodeIsInvalid() {
        assertThatThrownBy(() -> validator.validateDepartment("ABC", "Antioquia"))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Department code must be exactly 2 digits");
    }

    @Test
    void validateDepartment_shouldThrowWhenNameIsInvalid() {
        assertThatThrownBy(() -> validator.validateDepartment("05", ""))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Name is required");
    }

    @Test
    void validateMunicipality_shouldPassWithValidCodeAndName() {
        assertDoesNotThrow(() -> validator.validateMunicipality("05001", "Medellín"));
    }

    @Test
    void validateMunicipality_shouldThrowWhenCodeIsInvalid() {
        assertThatThrownBy(() -> validator.validateMunicipality("050", "Medellín"))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Municipality code must be exactly 5 digits");
    }

    @Test
    void validateMunicipality_shouldThrowWhenNameIsInvalid() {
        assertThatThrownBy(() -> validator.validateMunicipality("05001", ""))
                .isInstanceOf(InvalidGeographyException.class)
                .hasMessageContaining("Name is required");
    }
}
