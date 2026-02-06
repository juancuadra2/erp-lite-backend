package com.jcuadrado.erplitebackend.application.service.documenttype;

import com.jcuadrado.erplitebackend.application.port.out.DocumentTypePort;
import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ListDocumentTypesService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListDocumentTypesService Tests")
class ListDocumentTypesServiceTest {

    @Mock
    private DocumentTypePort documentTypePort;

    @InjectMocks
    private ListDocumentTypesService listService;

    private List<DocumentType> testDocumentTypes;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testDocumentTypes = Arrays.asList(
                DocumentType.builder()
                        .id(1L)
                        .uuid(UUID.randomUUID())
                        .code("NIT")
                        .name("Número de Identificación Tributaria")
                        .active(true)
                        .build(),
                DocumentType.builder()
                        .id(2L)
                        .uuid(UUID.randomUUID())
                        .code("CC")
                        .name("Cédula de Ciudadanía")
                        .active(true)
                        .build(),
                DocumentType.builder()
                        .id(3L)
                        .uuid(UUID.randomUUID())
                        .code("CE")
                        .name("Cédula de Extranjería")
                        .active(false)
                        .build()
        );

        pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    @DisplayName("Should list all document types with pagination")
    void shouldListAllDocumentTypesWithPagination() {
        // Given
        Page<DocumentType> page = new PageImpl<>(testDocumentTypes, pageable, testDocumentTypes.size());
        when(documentTypePort.findAll(pageable)).thenReturn(page);

        // When
        Page<DocumentType> result = listService.listAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(testDocumentTypes.size(), result.getContent().size());

        verify(documentTypePort).findAll(pageable);
    }

    @Test
    @DisplayName("Should list only active document types")
    void shouldListOnlyActiveDocumentTypes() {
        // Given
        List<DocumentType> activeTypes = testDocumentTypes.stream()
                .filter(DocumentType::isActive)
                .toList();
        when(documentTypePort.findByActiveTrue()).thenReturn(activeTypes);

        // When
        List<DocumentType> result = listService.listActive();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(DocumentType::isActive));

        verify(documentTypePort).findByActiveTrue();
    }

    @Test
    @DisplayName("Should list document types with enabled filter")
    void shouldListDocumentTypesWithEnabledFilter() {
        // Given
        List<DocumentType> activeTypes = testDocumentTypes.stream()
                .filter(DocumentType::isActive)
                .toList();
        Page<DocumentType> page = new PageImpl<>(activeTypes, pageable, activeTypes.size());

        when(documentTypePort.findWithFilters(eq(true), eq(null), eq(Collections.emptyMap()), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<DocumentType> result = listService.listWithFilters(true, null, Collections.emptyMap(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(DocumentType::isActive));

        verify(documentTypePort).findWithFilters(eq(true), eq(null), eq(Collections.emptyMap()), any(Pageable.class));
    }

    @Test
    @DisplayName("Should list document types with search filter")
    void shouldListDocumentTypesWithSearchFilter() {
        // Given
        String search = "Cedula";
        List<DocumentType> searchResults = testDocumentTypes.stream()
                .filter(dt -> dt.getName().toLowerCase().contains(search.toLowerCase()))
                .toList();
        Page<DocumentType> page = new PageImpl<>(searchResults, pageable, searchResults.size());

        when(documentTypePort.findWithFilters(eq(null), eq(search), eq(Collections.emptyMap()), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<DocumentType> result = listService.listWithFilters(null, search, Collections.emptyMap(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(searchResults.size(), result.getTotalElements());

        verify(documentTypePort).findWithFilters(eq(null), eq(search), eq(Collections.emptyMap()), any(Pageable.class));
    }

    @Test
    @DisplayName("Should list document types with multiple filters")
    void shouldListDocumentTypesWithMultipleFilters() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("country", "CO");

        List<DocumentType> filteredResults = testDocumentTypes.stream()
                .filter(DocumentType::isActive)
                .toList();
        Page<DocumentType> page = new PageImpl<>(filteredResults, pageable, filteredResults.size());

        when(documentTypePort.findWithFilters(eq(true), eq("cedula"), eq(filters), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<DocumentType> result = listService.listWithFilters(true, "cedula", filters, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getTotalElements() > 0);

        verify(documentTypePort).findWithFilters(eq(true), eq("cedula"), eq(filters), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle empty results")
    void shouldHandleEmptyResults() {
        // Given
        Page<DocumentType> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(documentTypePort.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<DocumentType> result = listService.listAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(documentTypePort).findAll(pageable);
    }
}

