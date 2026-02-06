package com.jcuadrado.erplitebackend.domain.documenttype.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DocumentType domain model.
 */
@DisplayName("DocumentType Domain Model Tests")
class DocumentTypeTest {

    @Test
    @DisplayName("Should create document type with all fields")
    void shouldCreateDocumentTypeWithAllFields() {
        // Given
        Long id = 1L;
        UUID uuid = UUID.randomUUID();
        String code = "NIT";
        String name = "Número de Identificación Tributaria";
        String description = "Documento de identificación para empresas";
        Boolean active = true;
        LocalDateTime now = LocalDateTime.now();

        // When
        DocumentType documentType = DocumentType.builder()
                .id(id)
                .uuid(uuid)
                .code(code)
                .name(name)
                .description(description)
                .active(active)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertNotNull(documentType);
        assertEquals(id, documentType.getId());
        assertEquals(uuid, documentType.getUuid());
        assertEquals(code, documentType.getCode());
        assertEquals(name, documentType.getName());
        assertEquals(description, documentType.getDescription());
        assertTrue(documentType.getActive());
        assertEquals(now, documentType.getCreatedAt());
        assertEquals(now, documentType.getUpdatedAt());
    }

    @Test
    @DisplayName("Should activate document type")
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
        assertTrue(documentType.getActive());
    }

    @Test
    @DisplayName("Should deactivate document type")
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
        assertFalse(documentType.getActive());
    }

    @Test
    @DisplayName("Should return true when document type is active")
    void shouldReturnTrueWhenDocumentTypeIsActive() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("CC")
                .name("Cédula de Ciudadanía")
                .active(true)
                .build();

        // When
        boolean isActive = documentType.isActive();

        // Then
        assertTrue(isActive);
    }

    @Test
    @DisplayName("Should return false when document type is inactive")
    void shouldReturnFalseWhenDocumentTypeIsInactive() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("CC")
                .name("Cédula de Ciudadanía")
                .active(false)
                .build();

        // When
        boolean isActive = documentType.isActive();

        // Then
        assertFalse(isActive);
    }

    @Test
    @DisplayName("Should return false when active is null")
    void shouldReturnFalseWhenActiveIsNull() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("CC")
                .name("Cédula de Ciudadanía")
                .active(null)
                .build();

        // When
        boolean isActive = documentType.isActive();

        // Then
        assertFalse(isActive);
    }
}

