package com.jcuadrado.erplitebackend.domain.service.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.InvalidUnitOfMeasureDataException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnitOfMeasureValidatorTest {

    private final UnitOfMeasureValidator validator = new UnitOfMeasureValidator();

    @Test
    void validateAll_shouldPassWithValidData() {
        assertThatCode(() -> validator.validateAll("Kilogramo", "KG")).doesNotThrowAnyException();
    }

    @Test
    void validateName_shouldFailWhenNameIsInvalid() {
        assertThatThrownBy(() -> validator.validateName("1"))
                .isInstanceOf(InvalidUnitOfMeasureDataException.class);
    }

    @Test
    void validateAbbreviation_shouldFailWhenAbbreviationIsInvalid() {
        assertThatThrownBy(() -> validator.validateAbbreviation("KG-01"))
                .isInstanceOf(InvalidUnitOfMeasureDataException.class);
    }

    @Test
    void validateName_shouldFailWhenPatternIsInvalidButLengthIsValid() {
        assertThatThrownBy(() -> validator.validateName("A1"))
                .isInstanceOf(InvalidUnitOfMeasureDataException.class);
    }

        @Test
        void validateName_shouldFailWhenNameIsNullOrBlank() {
        assertThatThrownBy(() -> validator.validateName(null))
            .isInstanceOf(InvalidUnitOfMeasureDataException.class);
        assertThatThrownBy(() -> validator.validateName("   "))
            .isInstanceOf(InvalidUnitOfMeasureDataException.class);
        }

        @Test
        void validateName_shouldFailWhenNameLengthIsOutOfRange() {
        assertThatThrownBy(() -> validator.validateName("A"))
            .isInstanceOf(InvalidUnitOfMeasureDataException.class);
        assertThatThrownBy(() -> validator.validateName("A".repeat(51)))
            .isInstanceOf(InvalidUnitOfMeasureDataException.class);
        }

        @Test
        void validateAbbreviation_shouldFailWhenAbbreviationIsNullBlankOrTooLong() {
        assertThatThrownBy(() -> validator.validateAbbreviation(null))
            .isInstanceOf(InvalidUnitOfMeasureDataException.class);
        assertThatThrownBy(() -> validator.validateAbbreviation("  "))
            .isInstanceOf(InvalidUnitOfMeasureDataException.class);
        assertThatThrownBy(() -> validator.validateAbbreviation("ABCDEFGHIJK"))
            .isInstanceOf(InvalidUnitOfMeasureDataException.class);
        }
}
