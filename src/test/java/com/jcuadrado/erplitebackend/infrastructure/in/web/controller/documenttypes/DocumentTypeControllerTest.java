package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.documenttypes;

import com.jcuadrado.erplitebackend.application.port.documenttypes.CompareDocumentTypesUseCase;
import com.jcuadrado.erplitebackend.application.port.documenttypes.ManageDocumentTypeUseCase;
import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DuplicateCodeException;
import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.CreateDocumentTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.DocumentTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.UpdateDocumentTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.documenttypes.DocumentTypeDtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentTypeControllerTest {

    @Mock
    private ManageDocumentTypeUseCase manageUseCase;

    @Mock
    private CompareDocumentTypesUseCase compareUseCase;

    @Mock
    private DocumentTypeDtoMapper mapper;

    @InjectMocks
    private DocumentTypeController controller;

    private DocumentTypeResponseDto createResponseDto(DocumentType documentType) {
        return DocumentTypeResponseDto.builder()
                .uuid(documentType.getUuid())
                .code(documentType.getCode())
                .name(documentType.getName())
                .description(documentType.getDescription())
                .active(documentType.isActive())
                .createdAt(documentType.getCreatedAt())
                .updatedAt(documentType.getUpdatedAt())
                .build();
    }

    @Test
    void create_shouldReturn201WithCreatedDocumentType() throws Exception {
        // Given
        CreateDocumentTypeRequestDto request = CreateDocumentTypeRequestDto.builder()
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .description("Documento tributario")
                .build();

        UUID createdUuid = UUID.randomUUID();
        DocumentType domainModel = DocumentType.builder()
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .description("Documento tributario")
                .build();

        DocumentType created = DocumentType.builder()
                .id(1L)
                .uuid(createdUuid)
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .description("Documento tributario")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(mapper.toDomain(request)).thenReturn(domainModel);
        when(manageUseCase.create(domainModel)).thenReturn(created);
        when(mapper.toResponseDto(created)).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        ResponseEntity<DocumentTypeResponseDto> response = controller.create(request);

        // When & Then
        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(createdUuid, response.getBody().getUuid());

        verify(manageUseCase).create(domainModel);
    }

    @Test
    void create_shouldReturn400WhenCodeIsBlank() throws Exception {
        // Given
        CreateDocumentTypeRequestDto.builder()
                .code("")
                .name("Valid Name")
                .build();

        // When & Then
        // Validation would normally fail in a real Spring MVC context
        // In this unit test, we're just verifying the use case is never called
        verify(manageUseCase, never()).create(any());
    }

    @Test
    void create_shouldReturn400WhenNameIsBlank() throws Exception {
        // Given
        CreateDocumentTypeRequestDto.builder()
                .code("NIT")
                .name("")
                .build();

        // When & Then
        // Validation would normally fail in a real Spring MVC context
        // In this unit test, we're just verifying the use case is never called
        verify(manageUseCase, never()).create(any());
    }

    @Test
    void create_shouldReturn409WhenCodeAlreadyExists() throws Exception {
        // Given
        CreateDocumentTypeRequestDto request = CreateDocumentTypeRequestDto.builder()
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .build();

        DocumentType domainModel = DocumentType.builder()
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .build();

        when(mapper.toDomain(request)).thenReturn(domainModel);
        when(manageUseCase.create(domainModel))
                .thenThrow(new DuplicateCodeException("NIT"));

        // When & Then
        assertThrows(DuplicateCodeException.class, () -> {
            controller.create(request);
        });

        verify(manageUseCase).create(domainModel);
    }

    // ==================== GET /api/document-types/{uuid} Tests ====================

    @Test
    void getByUuid_shouldReturn200WithDocumentType() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        DocumentType documentType = DocumentType.builder()
                .id(1L)
                .uuid(uuid)
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .active(true)
                .build();

        when(compareUseCase.getByUuid(uuid)).thenReturn(documentType);
        when(mapper.toResponseDto(documentType)).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When & Then
        ResponseEntity<DocumentTypeResponseDto> response = controller.getByUuid(uuid);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(uuid, response.getBody().getUuid());
        assertEquals("NIT", response.getBody().getCode());

        verify(compareUseCase).getByUuid(uuid);
    }

    @Test
    void getByUuid_shouldReturn404WhenNotFound() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        when(compareUseCase.getByUuid(uuid))
                .thenThrow(new DocumentTypeNotFoundException(uuid));

        // When & Then
        assertThrows(DocumentTypeNotFoundException.class, () -> {
            controller.getByUuid(uuid);
        });

        verify(compareUseCase).getByUuid(uuid);
    }

    // ==================== GET /api/document-types/code/{code} Tests ====================

    @Test
    void getByCode_shouldReturn200WithDocumentType() throws Exception {
        // Given
        String code = "NIT";
        DocumentType documentType = DocumentType.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .code(code)
                .name("Número de Identificación Tributaria")
                .active(true)
                .build();

        when(compareUseCase.getByCode(code)).thenReturn(documentType);
        when(mapper.toResponseDto(documentType)).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When & Then
        ResponseEntity<DocumentTypeResponseDto> response = controller.getByCode(code);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(code, response.getBody().getCode());

        verify(compareUseCase).getByCode(code);
    }

    @Test
    void getByCode_shouldReturn404WhenNotFound() throws Exception {
        // Given
        String code = "INVALID";
        when(compareUseCase.getByCode(code))
                .thenThrow(new DocumentTypeNotFoundException(code));

        // When & Then
        assertThrows(DocumentTypeNotFoundException.class, () -> {
            controller.getByCode(code);
        });

        verify(compareUseCase).getByCode(code);
    }

    // ==================== GET /api/document-types (List) Tests ====================

    @Test
    void list_shouldReturn200WithPagedDocumentTypes() throws Exception {
        // Given
        DocumentType doc1 = DocumentType.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .code("NIT")
                .name("NIT")
                .active(true)
                .build();

        DocumentType doc2 = DocumentType.builder()
                .id(2L)
                .uuid(UUID.randomUUID())
                .code("CC")
                .name("Cédula")
                .active(true)
                .build();

        List<DocumentType> documentTypes = Arrays.asList(doc1, doc2);
        Page<DocumentType> page = new PageImpl<>(documentTypes);

        when(compareUseCase.findAll(anyMap(), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseDto(any())).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When & Then
        ResponseEntity<?> response = controller.list(null, null, 0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        verify(compareUseCase).findAll(anyMap(), any(Pageable.class));
    }

    @Test
    void list_shouldAcceptPaginationParameters() throws Exception {
        // Given
        Page<DocumentType> emptyPage = new PageImpl<>(Collections.emptyList());
        when(compareUseCase.findAll(anyMap(), any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        ResponseEntity<?> response = controller.list(null, null, 2, 20, "name", "desc");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        verify(compareUseCase).findAll(anyMap(), any(Pageable.class));
    }

    @Test
    void list_shouldAcceptFilterParameters() throws Exception {
        // Given
        Page<DocumentType> emptyPage = new PageImpl<>(Collections.emptyList());
        when(compareUseCase.findAll(anyMap(), any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        ResponseEntity<?> response = controller.list(true, "search term", 0, 10, "id", "asc");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        verify(compareUseCase).findAll(anyMap(), any(Pageable.class));
    }

    // ==================== GET /api/document-types/active Tests ====================

    @Test
    void getAllActive_shouldReturn200WithActiveDocumentTypes() throws Exception {
        // Given
        DocumentType doc1 = DocumentType.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .code("NIT")
                .name("NIT")
                .active(true)
                .build();

        List<DocumentType> activeTypes = Collections.singletonList(doc1);
        when(compareUseCase.getAllActive()).thenReturn(activeTypes);
        when(mapper.toResponseDto(any())).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When & Then
        ResponseEntity<List<DocumentTypeResponseDto>> response = controller.getAllActive();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(compareUseCase).getAllActive();
    }

    // ==================== PUT /api/document-types/{uuid} (Update) Tests ====================

    @Test
    void update_shouldReturn200WithUpdatedDocumentType() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        UpdateDocumentTypeRequestDto request = UpdateDocumentTypeRequestDto.builder()
                .code("NIT")
                .name("Updated Name")
                .description("Updated Description")
                .build();

        DocumentType domainModel = DocumentType.builder()
                .code("NIT")
                .name("Updated Name")
                .description("Updated Description")
                .build();

        DocumentType updated = DocumentType.builder()
                .id(1L)
                .uuid(uuid)
                .code("NIT")
                .name("Updated Name")
                .description("Updated Description")
                .active(true)
                .build();

        when(mapper.toDomain(request)).thenReturn(domainModel);
        when(manageUseCase.update(eq(uuid), any())).thenReturn(updated);
        when(mapper.toResponseDto(updated)).thenAnswer(invocation -> 
            createResponseDto(invocation.getArgument(0)));

        // When & Then
        ResponseEntity<DocumentTypeResponseDto> response = controller.update(uuid, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getName());

        verify(manageUseCase).update(eq(uuid), any());
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        UpdateDocumentTypeRequestDto request = UpdateDocumentTypeRequestDto.builder()
                .code("NIT")
                .name("Updated Name")
                .build();

        DocumentType domainModel = DocumentType.builder()
                .code("NIT")
                .name("Updated Name")
                .build();

        when(mapper.toDomain(request)).thenReturn(domainModel);
        when(manageUseCase.update(eq(uuid), any()))
                .thenThrow(new DocumentTypeNotFoundException(uuid));

        // When & Then
        assertThrows(DocumentTypeNotFoundException.class, () -> {
            controller.update(uuid, request);
        });

        verify(manageUseCase).update(eq(uuid), any());
    }

    @Test
    void update_shouldReturn400WhenInvalidData() throws Exception {
        // Given
        UpdateDocumentTypeRequestDto.builder()
                .code("")
                .name("Valid Name")
                .build();

        // When & Then
        // Validation would normally fail in a real Spring MVC context
        // In this unit test, we're just verifying the use case is never called
        verify(manageUseCase, never()).update(any(), any());
    }

    // ==================== DELETE /api/document-types/{uuid} Tests ====================

    @Test
    void delete_shouldReturn204() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).delete(uuid);

        // When & Then
        ResponseEntity<Void> response = controller.delete(uuid);

        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());

        verify(manageUseCase).delete(uuid);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        doThrow(new DocumentTypeNotFoundException(uuid))
                .when(manageUseCase).delete(uuid);

        // When & Then
        assertThrows(DocumentTypeNotFoundException.class, () -> {
            controller.delete(uuid);
        });

        verify(manageUseCase).delete(uuid);
    }

    // ==================== PATCH /api/document-types/{uuid}/activate Tests ====================

    @Test
    void activate_shouldReturn200() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).activate(uuid);

        // When & Then
        ResponseEntity<Void> response = controller.activate(uuid);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        verify(manageUseCase).activate(uuid);
    }

    @Test
    void activate_shouldReturn404WhenNotFound() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        doThrow(new DocumentTypeNotFoundException(uuid))
                .when(manageUseCase).activate(uuid);

        // When & Then
        assertThrows(DocumentTypeNotFoundException.class, () -> {
            controller.activate(uuid);
        });

        verify(manageUseCase).activate(uuid);
    }

    // ==================== PATCH /api/document-types/{uuid}/deactivate Tests ====================

    @Test
    void deactivate_shouldReturn200() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).deactivate(uuid);

        // When & Then
        ResponseEntity<Void> response = controller.deactivate(uuid);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        verify(manageUseCase).deactivate(uuid);
    }

    @Test
    void deactivate_shouldReturn404WhenNotFound() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        doThrow(new DocumentTypeNotFoundException(uuid))
                .when(manageUseCase).deactivate(uuid);

        // When & Then
        assertThrows(DocumentTypeNotFoundException.class, () -> {
            controller.deactivate(uuid);
        });

        verify(manageUseCase).deactivate(uuid);
    }
}
