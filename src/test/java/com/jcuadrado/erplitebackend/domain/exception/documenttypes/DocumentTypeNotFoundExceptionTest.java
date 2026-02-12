package com.jcuadrado.erplitebackend.domain.exception.documenttypes;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTypeNotFoundExceptionTest {

    @Test
    void constructor_withUUID_shouldCreateExceptionWithFormattedMessage() {
        // Given
        UUID uuid = UUID.randomUUID();

        // When
        DocumentTypeNotFoundException exception = new DocumentTypeNotFoundException(uuid);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(String.format("Document type not found with UUID: %s", uuid));
        assertThat(exception).isInstanceOf(DocumentTypeDomainException.class);
    }

    @Test
    void constructor_withCode_shouldCreateExceptionWithFormattedMessage() {
        // Given
        String code = "NIT";

        // When
        DocumentTypeNotFoundException exception = new DocumentTypeNotFoundException(code);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Document type not found with code: NIT");
        assertThat(exception).isInstanceOf(DocumentTypeDomainException.class);
    }

    @Test
    void constructor_withId_shouldCreateExceptionWithFormattedMessage() {
        // Given
        Long id = 123L;

        // When
        DocumentTypeNotFoundException exception = new DocumentTypeNotFoundException(id);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo("Document type not found with ID: 123");
        assertThat(exception).isInstanceOf(DocumentTypeDomainException.class);
    }
}
