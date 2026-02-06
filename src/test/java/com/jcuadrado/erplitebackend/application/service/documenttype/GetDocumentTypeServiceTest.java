package com.jcuadrado.erplitebackend.application.service.documenttype;

import com.jcuadrado.erplitebackend.application.port.out.DocumentTypePort;
import com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetDocumentTypeService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetDocumentTypeService Tests")
class GetDocumentTypeServiceTest {

    @Mock
    private DocumentTypePort documentTypePort;

    @InjectMocks
    private GetDocumentTypeService getService;

    private UUID testUuid;
    private DocumentType testDocumentType;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        testDocumentType = DocumentType.builder()
                .id(1L)
                .uuid(testUuid)
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .description("Documento para empresas")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should get document type by UUID successfully")
    void shouldGetDocumentTypeByUuidSuccessfully() {
        // Given
        when(documentTypePort.findByUuid(testUuid)).thenReturn(Optional.of(testDocumentType));

        // When
        DocumentType result = getService.getByUuid(testUuid);

        // Then
        assertNotNull(result);
        assertEquals(testUuid, result.getUuid());
        assertEquals("NIT", result.getCode());
        assertEquals("Número de Identificación Tributaria", result.getName());

        verify(documentTypePort).findByUuid(testUuid);
    }

    @Test
    @DisplayName("Should throw exception when document type not found")
    void shouldThrowExceptionWhenDocumentTypeNotFound() {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();
        when(documentTypePort.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DocumentTypeNotFoundException.class,
                () -> getService.getByUuid(nonExistentUuid));

        verify(documentTypePort).findByUuid(nonExistentUuid);
    }

    @Test
    @DisplayName("Should get document type by code successfully")
    void shouldGetDocumentTypeByCodeSuccessfully() {
        // Given
        String code = "NIT";
        when(documentTypePort.findByCode(code)).thenReturn(Optional.of(testDocumentType));

        // When
        DocumentType result = getService.getByCode(code);

        // Then
        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals("Número de Identificación Tributaria", result.getName());

        verify(documentTypePort).findByCode(code);
    }

    @Test
    @DisplayName("Should throw exception when document type by code not found")
    void shouldThrowExceptionWhenDocumentTypeByCodeNotFound() {
        // Given
        String nonExistentCode = "INVALID";
        when(documentTypePort.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DocumentTypeNotFoundException.class,
                () -> getService.getByCode(nonExistentCode));

        verify(documentTypePort).findByCode(nonExistentCode);
    }
}

