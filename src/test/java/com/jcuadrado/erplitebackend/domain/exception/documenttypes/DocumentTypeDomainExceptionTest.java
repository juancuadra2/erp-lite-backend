package com.jcuadrado.erplitebackend.domain.exception.documenttypes;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTypeDomainExceptionTest {

    @Test
    void constructor_withMessage_shouldCreateExceptionWithMessage() {
        // Given
        String message = "Test error message";

        // When
        DocumentTypeDomainException exception = new DocumentTypeDomainException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructor_withMessageAndCause_shouldCreateExceptionWithBoth() {
        // Given
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");

        // When
        DocumentTypeDomainException exception = new DocumentTypeDomainException(message, cause);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Root cause");
    }
}
