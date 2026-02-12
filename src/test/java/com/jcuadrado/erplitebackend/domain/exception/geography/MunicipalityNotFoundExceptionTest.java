package com.jcuadrado.erplitebackend.domain.exception.geography;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MunicipalityNotFoundExceptionTest {

    @Test
    void constructor_withUUID_shouldCreateExceptionWithFormattedMessage() {
        // Given
        UUID uuid = UUID.randomUUID();

        // When
        MunicipalityNotFoundException exception = new MunicipalityNotFoundException(uuid);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(String.format("Municipality not found with UUID: %s", uuid));
        assertThat(exception).isInstanceOf(GeographyDomainException.class);
    }

    @Test
    void constructor_withCode_shouldCreateExceptionWithFormattedMessage() {
        // Given
        String code = "05001";

        // When
        MunicipalityNotFoundException exception = new MunicipalityNotFoundException(code);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Municipality not found with code: 05001");
        assertThat(exception).isInstanceOf(GeographyDomainException.class);
    }

    @Test
    void constructor_withId_shouldCreateExceptionWithFormattedMessage() {
        // Given
        Long id = 456L;

        // When
        MunicipalityNotFoundException exception = new MunicipalityNotFoundException(id);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Municipality not found with ID: 456");
        assertThat(exception).isInstanceOf(GeographyDomainException.class);
    }
}
