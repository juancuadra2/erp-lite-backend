package com.jcuadrado.erplitebackend.domain.model.documenttypes;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DocumentType domain model
 */
class DocumentTypeTest {

    @Test
    void shouldCreateDocumentTypeWithBuilder() {
        // Given
        UUID uuid = UUID.randomUUID();
        String code = "NIT";
        String name = "Número de Identificación Tributaria";
        String description = "Documento tributario para empresas";

        // When
        DocumentType documentType = DocumentType.builder()
                .uuid(uuid)
                .code(code)
                .name(name)
                .description(description)
                .active(true)
                .build();

        // Then
        assertThat(documentType).isNotNull();
        assertThat(documentType.getUuid()).isEqualTo(uuid);
        assertThat(documentType.getCode()).isEqualTo(code);
        assertThat(documentType.getName()).isEqualTo(name);
        assertThat(documentType.getDescription()).isEqualTo(description);
        assertThat(documentType.getActive()).isTrue();
    }

    @Test
    void shouldActivateDocumentType() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("CC")
                .name("Cédula de Ciudadanía")
                .active(false)
                .build();

        // When
        documentType.activate();

        // Then
        assertThat(documentType.getActive()).isTrue();
        assertThat(documentType.isActive()).isTrue();
    }

    @Test
    void shouldDeactivateDocumentType() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("CC")
                .name("Cédula de Ciudadanía")
                .active(true)
                .build();

        // When
        documentType.deactivate();

        // Then
        assertThat(documentType.getActive()).isFalse();
        assertThat(documentType.isActive()).isFalse();
    }

    @Test
    void isActive_shouldReturnTrueWhenActiveIsTrue() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .active(true)
                .build();

        // When & Then
        assertThat(documentType.isActive()).isTrue();
    }

    @Test
    void isActive_shouldReturnFalseWhenActiveIsFalse() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .active(false)
                .build();

        // When & Then
        assertThat(documentType.isActive()).isFalse();
    }

    @Test
    void isActive_shouldReturnFalseWhenActiveIsNull() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .active(null)
                .build();

        // When & Then
        assertThat(documentType.isActive()).isFalse();
    }

    @Test
    void isDeleted_shouldReturnFalseWhenDeletedAtIsNull() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .build();

        // When & Then
        assertThat(documentType.isDeleted()).isFalse();
    }

    @Test
    void isDeleted_shouldReturnTrueWhenDeletedAtIsNotNull() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .deletedAt(LocalDateTime.now())
                .build();

        // When & Then
        assertThat(documentType.isDeleted()).isTrue();
    }

    @Test
    void markAsDeleted_shouldSetDeletedFieldsAndDeactivate() {
        // Given
        Long userId = 100L;
        DocumentType documentType = DocumentType.builder()
                .code("CC")
                .name("Cédula de Ciudadanía")
                .active(true)
                .build();

        LocalDateTime beforeDeletion = LocalDateTime.now();

        // When
        documentType.markAsDeleted(userId);

        // Then
        assertThat(documentType.getDeletedAt()).isNotNull();
        assertThat(documentType.getDeletedAt()).isAfterOrEqualTo(beforeDeletion);
        assertThat(documentType.getDeletedBy()).isEqualTo(userId);
        assertThat(documentType.getActive()).isFalse();
    }

    @Test
    void normalizeCode_shouldConvertToUppercaseAndTrim() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("  nit  ")
                .build();

        // When
        documentType.normalizeCode();

        // Then
        assertThat(documentType.getCode()).isEqualTo("NIT");
    }

    @Test
    void normalizeCode_shouldHandleNullCode() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code(null)
                .build();

        // When
        documentType.normalizeCode();

        // Then
        assertThat(documentType.getCode()).isNull();
    }

    @Test
    void normalizeCode_shouldHandleAlreadyUppercaseCode() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("NIT")
                .build();

        // When
        documentType.normalizeCode();

        // Then
        assertThat(documentType.getCode()).isEqualTo("NIT");
    }

    @Test
    void shouldSetAndGetAuditFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Long userId = 1L;

        DocumentType documentType = DocumentType.builder()
                .createdBy(userId)
                .createdAt(now)
                .updatedBy(userId)
                .updatedAt(now)
                .build();

        // Then
        assertThat(documentType.getCreatedBy()).isEqualTo(userId);
        assertThat(documentType.getCreatedAt()).isEqualTo(now);
        assertThat(documentType.getUpdatedBy()).isEqualTo(userId);
        assertThat(documentType.getUpdatedAt()).isEqualTo(now);
    }
}
