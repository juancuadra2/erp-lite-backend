package com.jcuadrado.erplitebackend.application.usecase.documenttypes;

import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import com.jcuadrado.erplitebackend.domain.service.documenttypes.DocumentTypeDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ManageDocumentTypeUseCaseImpl
 */
@ExtendWith(MockitoExtension.class)
class ManageDocumentTypeUseCaseImplTest {

    @Mock
    private DocumentTypeRepository repository;

    @Mock
    private DocumentTypeDomainService domainService;

    @InjectMocks
    private ManageDocumentTypeUseCaseImpl useCase;

    @Captor
    private ArgumentCaptor<DocumentType> documentTypeCaptor;

    private UUID sampleUuid;

    @BeforeEach
    void setUp() {
        sampleUuid = UUID.randomUUID();
    }

    // ==================== create Tests ====================

    @Test
    void create_shouldPrepareValidateAndSaveDocumentType() {
        // Given
        DocumentType newDocumentType = DocumentType.builder()
                .code("CC")
                .name("Cédula de Ciudadanía")
                .description("Documento de identidad")
                .build();

        DocumentType savedDocumentType = DocumentType.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .code("CC")
                .name("Cédula de Ciudadanía")
                .description("Documento de identidad")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        doNothing().when(domainService).prepareForCreation(any(DocumentType.class));
        when(repository.save(any(DocumentType.class))).thenReturn(savedDocumentType);

        // When
        DocumentType result = useCase.create(newDocumentType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("CC");

        verify(domainService).prepareForCreation(newDocumentType);
        verify(repository).save(documentTypeCaptor.capture());

        DocumentType captured = documentTypeCaptor.getValue();
        assertThat(captured.getCreatedAt()).isNotNull();
    }

    @Test
    void create_shouldSetCreatedAt() {
        // Given
        DocumentType newDocumentType = DocumentType.builder()
                .code("CC")
                .name("Cédula de Ciudadanía")
                .build();

        LocalDateTime beforeCreation = LocalDateTime.now();

        doNothing().when(domainService).prepareForCreation(any(DocumentType.class));
        when(repository.save(any(DocumentType.class))).thenReturn(newDocumentType);

        // When
        useCase.create(newDocumentType);

        // Then
        verify(repository).save(documentTypeCaptor.capture());
        DocumentType captured = documentTypeCaptor.getValue();

        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
    }

    // ==================== update Tests ====================

    @Test
    void update_shouldFindExistingDocumentTypeAndUpdate() {
        // Given
        DocumentType existingDocumentType = DocumentType.builder()
                .id(1L)
                .uuid(sampleUuid)
                .code("NIT")
                .name("Old Name")
                .description("Old Description")
                .active(true)
                .build();

        DocumentType updateData = DocumentType.builder()
                .code("NIT")
                .name("New Name")
                .description("New Description")
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingDocumentType));
        doNothing().when(domainService).prepareForUpdate(any(DocumentType.class));
        when(repository.save(any(DocumentType.class))).thenReturn(existingDocumentType);

        // When
        DocumentType result = useCase.update(sampleUuid, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByUuid(sampleUuid);
        verify(domainService).prepareForUpdate(existingDocumentType);
        verify(repository).save(documentTypeCaptor.capture());

        DocumentType captured = documentTypeCaptor.getValue();
        assertThat(captured.getCode()).isEqualTo("NIT");
        assertThat(captured.getName()).isEqualTo("New Name");
        assertThat(captured.getDescription()).isEqualTo("New Description");
        assertThat(captured.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_shouldThrowExceptionWhenDocumentTypeNotFound() {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();
        DocumentType updateData = DocumentType.builder()
                .code("NIT")
                .name("New Name")
                .build();

        when(repository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.update(nonExistentUuid, updateData))
                .isInstanceOf(DocumentTypeNotFoundException.class);

        verify(repository).findByUuid(nonExistentUuid);
        verify(domainService, never()).prepareForUpdate(any());
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldSetUpdatedAt() {
        // Given
        DocumentType existingDocumentType = DocumentType.builder()
                .uuid(sampleUuid)
                .code("NIT")
                .name("Old Name")
                .build();

        DocumentType updateData = DocumentType.builder()
                .code("NIT")
                .name("New Name")
                .build();

        LocalDateTime beforeUpdate = LocalDateTime.now();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingDocumentType));
        doNothing().when(domainService).prepareForUpdate(any(DocumentType.class));
        when(repository.save(any(DocumentType.class))).thenReturn(existingDocumentType);

        // When
        useCase.update(sampleUuid, updateData);

        // Then
        verify(repository).save(documentTypeCaptor.capture());
        DocumentType captured = documentTypeCaptor.getValue();

        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    // ==================== delete Tests ====================

    @Test
    void delete_shouldMarkDocumentTypeAsDeleted() {
        // Given
        DocumentType existingDocumentType = DocumentType.builder()
                .id(1L)
                .uuid(sampleUuid)
                .code("NIT")
                .name("NIT")
                .active(true)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingDocumentType));
        when(domainService.canDelete(existingDocumentType)).thenReturn(true);
        when(repository.save(any(DocumentType.class))).thenReturn(existingDocumentType);

        // When
        useCase.delete(sampleUuid);

        // Then
        verify(repository).findByUuid(sampleUuid);
        verify(domainService).canDelete(existingDocumentType);
        verify(repository).save(documentTypeCaptor.capture());

        DocumentType captured = documentTypeCaptor.getValue();
        assertThat(captured.getDeletedAt()).isNotNull();
        assertThat(captured.getActive()).isFalse();
    }

    @Test
    void delete_shouldThrowExceptionWhenDocumentTypeNotFound() {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();
        when(repository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.delete(nonExistentUuid))
                .isInstanceOf(DocumentTypeNotFoundException.class);

        verify(repository).findByUuid(nonExistentUuid);
        verify(domainService, never()).canDelete(any());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldThrowExceptionWhenCannotDelete() {
        // Given
        DocumentType existingDocumentType = DocumentType.builder()
                .uuid(sampleUuid)
                .code("NIT")
                .name("NIT")
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingDocumentType));
        when(domainService.canDelete(existingDocumentType)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> useCase.delete(sampleUuid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete document type");

        verify(repository).findByUuid(sampleUuid);
        verify(domainService).canDelete(existingDocumentType);
        verify(repository, never()).save(any());
    }

    // ==================== activate Tests ====================

    @Test
    void activate_shouldActivateDocumentType() {
        // Given
        DocumentType existingDocumentType = DocumentType.builder()
                .id(1L)
                .uuid(sampleUuid)
                .code("NIT")
                .name("NIT")
                .active(false)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingDocumentType));
        when(repository.save(any(DocumentType.class))).thenReturn(existingDocumentType);

        // When
        useCase.activate(sampleUuid);

        // Then
        verify(repository).findByUuid(sampleUuid);
        verify(repository).save(documentTypeCaptor.capture());

        DocumentType captured = documentTypeCaptor.getValue();
        assertThat(captured.getActive()).isTrue();
        assertThat(captured.getUpdatedAt()).isNotNull();
    }

    @Test
    void activate_shouldThrowExceptionWhenDocumentTypeNotFound() {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();
        when(repository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.activate(nonExistentUuid))
                .isInstanceOf(DocumentTypeNotFoundException.class);

        verify(repository).findByUuid(nonExistentUuid);
        verify(repository, never()).save(any());
    }

    // ==================== deactivate Tests ====================

    @Test
    void deactivate_shouldDeactivateDocumentType() {
        // Given
        DocumentType existingDocumentType = DocumentType.builder()
                .id(1L)
                .uuid(sampleUuid)
                .code("NIT")
                .name("NIT")
                .active(true)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingDocumentType));
        when(domainService.canDeactivate(existingDocumentType)).thenReturn(true);
        when(repository.save(any(DocumentType.class))).thenReturn(existingDocumentType);

        // When
        useCase.deactivate(sampleUuid);

        // Then
        verify(repository).findByUuid(sampleUuid);
        verify(domainService).canDeactivate(existingDocumentType);
        verify(repository).save(documentTypeCaptor.capture());

        DocumentType captured = documentTypeCaptor.getValue();
        assertThat(captured.getActive()).isFalse();
        assertThat(captured.getUpdatedAt()).isNotNull();
    }

    @Test
    void deactivate_shouldThrowExceptionWhenDocumentTypeNotFound() {
        // Given
        UUID nonExistentUuid = UUID.randomUUID();
        when(repository.findByUuid(nonExistentUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.deactivate(nonExistentUuid))
                .isInstanceOf(DocumentTypeNotFoundException.class);

        verify(repository).findByUuid(nonExistentUuid);
        verify(domainService, never()).canDeactivate(any());
        verify(repository, never()).save(any());
    }

    @Test
    void deactivate_shouldThrowExceptionWhenCannotDeactivate() {
        // Given
        DocumentType existingDocumentType = DocumentType.builder()
                .uuid(sampleUuid)
                .code("NIT")
                .name("NIT")
                .active(true)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingDocumentType));
        when(domainService.canDeactivate(existingDocumentType)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> useCase.deactivate(sampleUuid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot deactivate document type");

        verify(repository).findByUuid(sampleUuid);
        verify(domainService).canDeactivate(existingDocumentType);
        verify(repository, never()).save(any());
    }
}
