package com.jcuadrado.erplitebackend.application.service.documenttype;

import com.jcuadrado.erplitebackend.application.port.out.DocumentTypePort;
import com.jcuadrado.erplitebackend.domain.documenttype.exception.DuplicateCodeException;
import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import com.jcuadrado.erplitebackend.domain.documenttype.service.DocumentTypeDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateDocumentTypeService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateDocumentTypeService Tests")
class CreateDocumentTypeServiceTest {

    @Mock
    private DocumentTypePort documentTypePort;

    @Mock
    private DocumentTypeDomainService domainService;

    @InjectMocks
    private CreateDocumentTypeService createService;

    private DocumentType validDocumentType;

    @BeforeEach
    void setUp() {
        validDocumentType = DocumentType.builder()
                .code("nit")
                .name("Número de Identificación Tributaria")
                .description("Documento para empresas")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should create document type successfully")
    void shouldCreateDocumentTypeSuccessfully() {
        // Given
        when(domainService.normalizeCode("nit")).thenReturn("NIT");
        when(documentTypePort.existsByCode("NIT")).thenReturn(false);

        DocumentType savedDocumentType = DocumentType.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .description("Documento para empresas")
                .active(true)
                .build();

        when(documentTypePort.save(any(DocumentType.class))).thenReturn(savedDocumentType);

        // When
        DocumentType result = createService.create(validDocumentType);

        // Then
        assertNotNull(result);
        assertEquals("NIT", result.getCode());
        assertEquals("Número de Identificación Tributaria", result.getName());
        assertNotNull(result.getId());
        assertNotNull(result.getUuid());

        verify(domainService).normalizeCode("nit");
        verify(domainService).validateCode("NIT");
        verify(documentTypePort).existsByCode("NIT");
        verify(documentTypePort).save(any(DocumentType.class));
    }

    @Test
    @DisplayName("Should throw exception when code already exists")
    void shouldThrowExceptionWhenCodeAlreadyExists() {
        // Given
        when(domainService.normalizeCode("nit")).thenReturn("NIT");
        when(documentTypePort.existsByCode("NIT")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateCodeException.class, () -> createService.create(validDocumentType));

        verify(domainService).normalizeCode("nit");
        verify(documentTypePort).existsByCode("NIT");
        verify(documentTypePort, never()).save(any(DocumentType.class));
    }

    @Test
    @DisplayName("Should normalize code before saving")
    void shouldNormalizeCodeBeforeSaving() {
        // Given
        DocumentType documentTypeWithLowercaseCode = DocumentType.builder()
                .code("cc")
                .name("Cédula de Ciudadanía")
                .build();

        when(domainService.normalizeCode("cc")).thenReturn("CC");
        when(documentTypePort.existsByCode("CC")).thenReturn(false);

        DocumentType savedDocumentType = DocumentType.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .code("CC")
                .name("Cédula de Ciudadanía")
                .active(true)
                .build();

        when(documentTypePort.save(any(DocumentType.class))).thenReturn(savedDocumentType);

        // When
        DocumentType result = createService.create(documentTypeWithLowercaseCode);

        // Then
        assertEquals("CC", result.getCode());
        verify(domainService).normalizeCode("cc");
    }

    @Test
    @DisplayName("Should validate code before creating")
    void shouldValidateCodeBeforeCreating() {
        // Given
        when(domainService.normalizeCode("nit")).thenReturn("NIT");
        doNothing().when(domainService).validateCode("NIT");
        when(documentTypePort.existsByCode("NIT")).thenReturn(false);
        when(documentTypePort.save(any(DocumentType.class))).thenReturn(validDocumentType);

        // When
        createService.create(validDocumentType);

        // Then
        verify(domainService).validateCode("NIT");
    }
}

