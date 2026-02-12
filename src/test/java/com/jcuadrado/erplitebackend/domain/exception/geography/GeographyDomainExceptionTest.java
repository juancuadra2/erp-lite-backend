package com.jcuadrado.erplitebackend.domain.exception.geography;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeographyDomainExceptionTest {

    @Test
    void constructor_withMessage_shouldCreateExceptionWithMessage() {
        // Given
        String message = "Test geography error message";

        // When
        GeographyDomainException exception = new GeographyDomainException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructor_withMessageAndCause_shouldCreateExceptionWithBoth() {
        // Given
        String message = "Test geography error message";
        Throwable cause = new RuntimeException("Root cause");

        // When
        GeographyDomainException exception = new GeographyDomainException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Root cause");
    }
}
