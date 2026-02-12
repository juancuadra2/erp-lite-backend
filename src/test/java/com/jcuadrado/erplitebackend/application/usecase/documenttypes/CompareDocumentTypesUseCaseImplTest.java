package com.jcuadrado.erplitebackend.application.usecase.documenttypes;

import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompareDocumentTypesUseCaseImpl
 */
@ExtendWith(MockitoExtension.class)
class CompareDocumentTypesUseCaseImplTest {

    @Mock
    private DocumentTypeRepository repository;

    @InjectMocks
    private CompareDocumentTypesUseCaseImpl useCase;

    private DocumentType sampleDocumentType1;
    private DocumentType sampleDocumentType2;
    private UUID sampleUuid1;
    private UUID sampleUuid2;

    @BeforeEach
    void setUp() {
        sampleUuid1 = UUID.randomUUID();
        sampleUuid2 = UUID.randomUUID();

        sampleDocumentType1 = DocumentType.builder()
                .id(1L)
                .uuid(sampleUuid1)
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .description("Documento tributario")
                .active(true)
                .build();

        sampleDocumentType2 = DocumentType.builder()
                .id(2L)
                .uuid(sampleUuid2)
                .code("CC")
                .name("Cédula de Ciudadanía")
                .description("Documento de identidad")
                .active(true)
                .build();
    }

    // ==================== getByUuid Tests ====================

    @Test
    void getByUuid_shouldReturnDocumentTypeWhenFound() {
        // Given
        when(repository.findByUuid(sampleUuid1)).thenReturn(Optional.of(sampleDocumentType1));

        // When
        DocumentType result = useCase.getByUuid(sampleUuid1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(sampleUuid1);
        assertThat(result.getCode()).isEqualTo("NIT");
        verify(repository).findByUuid(sampleUuid1);
    }

    @Test
    void getByUuid_shouldThrowExceptionWhenNotFound() {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();
        when(repository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.getByUuid(nonExistentUuid))
                .isInstanceOf(DocumentTypeNotFoundException.class);

        verify(repository).findByUuid(nonExistentUuid);
    }

    // ==================== getByCode Tests ====================

    @Test
    void getByCode_shouldReturnDocumentTypeWhenFound() {
        // Given
        String code = "NIT";
        when(repository.findByCode(code)).thenReturn(Optional.of(sampleDocumentType1));

        // When
        DocumentType result = useCase.getByCode(code);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("NIT");
        assertThat(result.getName()).isEqualTo("Número de Identificación Tributaria");
        verify(repository).findByCode(code);
    }

    @Test
    void getByCode_shouldThrowExceptionWhenNotFound() {
        // Given
        String nonExistentCode = "INVALID";
        when(repository.findByCode(nonExistentCode)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.getByCode(nonExistentCode))
                .isInstanceOf(DocumentTypeNotFoundException.class);

        verify(repository).findByCode(nonExistentCode);
    }

    // ==================== getAllActive Tests ====================

    @Test
    void getAllActive_shouldReturnAllActiveDocumentTypes() {
        // Given
        List<DocumentType> activeDocumentTypes = Arrays.asList(
                sampleDocumentType1,
                sampleDocumentType2
        );
        when(repository.findAllActive()).thenReturn(activeDocumentTypes);

        // When
        List<DocumentType> result = useCase.getAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(sampleDocumentType1, sampleDocumentType2);
        verify(repository).findAllActive();
    }

    @Test
    void getAllActive_shouldReturnEmptyListWhenNoActiveDocumentTypes() {
        // Given
        when(repository.findAllActive()).thenReturn(Collections.emptyList());

        // When
        List<DocumentType> result = useCase.getAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(repository).findAllActive();
    }

    // ==================== findAll Tests ====================

    @Test
    void findAll_shouldReturnPagedDocumentTypes() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = new HashMap<>();
        filters.put("active", true);

        List<DocumentType> documentTypes = Arrays.asList(sampleDocumentType1, sampleDocumentType2);
        Page<DocumentType> page = new PageImpl<>(documentTypes, pageable, 2);

        when(repository.findAll(filters, pageable)).thenReturn(page);

        // When
        Page<DocumentType> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(sampleDocumentType1, sampleDocumentType2);
        verify(repository).findAll(filters, pageable);
    }

    @Test
    void findAll_shouldReturnEmptyPageWhenNoResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = new HashMap<>();
        Page<DocumentType> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(repository.findAll(filters, pageable)).thenReturn(emptyPage);

        // When
        Page<DocumentType> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(repository).findAll(filters, pageable);
    }

    @Test
    void findAll_shouldHandleEmptyFilters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> emptyFilters = new HashMap<>();

        List<DocumentType> documentTypes = Arrays.asList(sampleDocumentType1, sampleDocumentType2);
        Page<DocumentType> page = new PageImpl<>(documentTypes, pageable, 2);

        when(repository.findAll(emptyFilters, pageable)).thenReturn(page);

        // When
        Page<DocumentType> result = useCase.findAll(emptyFilters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(repository).findAll(emptyFilters, pageable);
    }

    @Test
    void findAll_shouldHandleDifferentPageSizes() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        Map<String, Object> filters = new HashMap<>();

        List<DocumentType> documentTypes = Collections.singletonList(sampleDocumentType1);
        Page<DocumentType> page = new PageImpl<>(documentTypes, pageable, 10);

        when(repository.findAll(filters, pageable)).thenReturn(page);

        // When
        Page<DocumentType> result = useCase.findAll(filters, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.hasNext()).isTrue();
        verify(repository).findAll(filters, pageable);
    }
}
