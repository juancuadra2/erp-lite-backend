package com.jcuadrado.erplitebackend.domain.exception.geography;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateMunicipalityCodeExceptionTest {

    @Test
    void constructor_withCodeAndDepartmentName_shouldCreateExceptionWithFormattedMessage() {
        // Given
        String code = "05001";
        String departmentName = "Antioquia";

        // When
        DuplicateMunicipalityCodeException exception = new DuplicateMunicipalityCodeException(code, departmentName);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Municipality with code '05001' already exists in department 'Antioquia'");
        assertThat(exception).isInstanceOf(GeographyDomainException.class);
    }

    @Test
    void constructor_withCodeOnly_shouldCreateExceptionWithFormattedMessage() {
        // Given
        String code = "05001";

        // When
        DuplicateMunicipalityCodeException exception = new DuplicateMunicipalityCodeException(code);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Municipality with code '05001' already exists in this department");
        assertThat(exception).isInstanceOf(GeographyDomainException.class);
    }
}
