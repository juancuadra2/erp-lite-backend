package com.jcuadrado.erplitebackend.domain.service.documenttypes;

import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DuplicateCodeException;
import com.jcuadrado.erplitebackend.domain.exception.documenttypes.InvalidDocumentTypeException;
import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentTypeDomainServiceTest {

    @Mock
    private DocumentTypeRepository repository;

    @Mock
    private DocumentTypeValidator validator;

    @InjectMocks
    private DocumentTypeDomainService domainService;

    @Test
    void normalizeCode_shouldConvertToUppercaseAndTrim() {
        // Given
        String code = "  nit  ";

        // When
        String result = domainService.normalizeCode(code);

        // Then
        assertThat(result).isEqualTo("NIT");
    }

    @Test
    void normalizeCode_shouldReturnNullWhenCodeIsNull() {
        // Given
        String code = null;

        // When
        String result = domainService.normalizeCode(code);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void normalizeCode_shouldHandleAlreadyNormalizedCode() {
        // Given
        String code = "NIT";

        // When
        String result = domainService.normalizeCode(code);

        // Then
        assertThat(result).isEqualTo("NIT");
    }

    @Test
    void validateUniqueCode_shouldPassWhenCodeDoesNotExist() {
        // Given
        String code = "NIT";
        when(repository.existsByCode("NIT")).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> domainService.validateUniqueCode(code));
        verify(repository).existsByCode("NIT");
    }

    @Test
    void validateUniqueCode_shouldThrowExceptionWhenCodeExists() {
        // Given
        String code = "NIT";
        when(repository.existsByCode("NIT")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> domainService.validateUniqueCode(code))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("NIT");

        verify(repository).existsByCode("NIT");
    }

    @Test
    void validateUniqueCode_shouldNormalizeCodeBeforeChecking() {
        // Given
        String code = "  nit  ";
        when(repository.existsByCode("NIT")).thenReturn(false);

        // When
        domainService.validateUniqueCode(code);

        // Then
        verify(repository).existsByCode("NIT");
    }

    @Test
    void validateUniqueCodeExcluding_shouldPassWhenCodeDoesNotExistForOtherDocuments() {
        // Given
        String code = "NIT";
        UUID uuid = UUID.randomUUID();
        when(repository.existsByCodeExcludingUuid("NIT", uuid)).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> domainService.validateUniqueCodeExcluding(code, uuid));
        verify(repository).existsByCodeExcludingUuid("NIT", uuid);
    }

    @Test
    void validateUniqueCodeExcluding_shouldThrowExceptionWhenCodeExistsForOtherDocuments() {
        // Given
        String code = "NIT";
        UUID uuid = UUID.randomUUID();
        when(repository.existsByCodeExcludingUuid("NIT", uuid)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> domainService.validateUniqueCodeExcluding(code, uuid))
                .isInstanceOf(DuplicateCodeException.class)
                .hasMessageContaining("NIT");

        verify(repository).existsByCodeExcludingUuid("NIT", uuid);
    }

    @Test
    void canDeactivate_shouldReturnTrue() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("NIT")
                .name("NIT")
                .active(true)
                .build();

        // When
        boolean result = domainService.canDeactivate(documentType);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canDelete_shouldReturnTrue() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("NIT")
                .name("NIT")
                .active(true)
                .build();

        // When
        boolean result = domainService.canDelete(documentType);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void prepareForCreation_shouldValidateAndNormalizeCode() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("  nit  ")
                .name("Número de Identificación Tributaria")
                .description("Documento tributario")
                .build();

        doNothing().when(validator).validateAll(anyString(), anyString(), anyString());
        when(repository.existsByCode("NIT")).thenReturn(false);

        // When
        domainService.prepareForCreation(documentType);

        // Then
        assertThat(documentType.getCode()).isEqualTo("NIT");
        verify(validator).validateAll("  nit  ", "Número de Identificación Tributaria", "Documento tributario");
        verify(repository).existsByCode("NIT");
    }

    @Test
    void prepareForCreation_shouldGenerateUuidIfNotProvided() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .uuid(null)
                .build();

        doNothing().when(validator).validateAll(anyString(), anyString(), any());
        when(repository.existsByCode("NIT")).thenReturn(false);

        // When
        domainService.prepareForCreation(documentType);

        // Then
        assertThat(documentType.getUuid()).isNotNull();
    }

    @Test
    void prepareForCreation_shouldKeepExistingUuid() {
        // Given
        UUID existingUuid = UUID.randomUUID();
        DocumentType documentType = DocumentType.builder()
                .uuid(existingUuid)
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .build();

        doNothing().when(validator).validateAll(anyString(), anyString(), any());
        when(repository.existsByCode("NIT")).thenReturn(false);

        // When
        domainService.prepareForCreation(documentType);

        // Then
        assertThat(documentType.getUuid()).isEqualTo(existingUuid);
    }

    @Test
    void prepareForCreation_shouldSetActiveToTrueIfNull() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .active(null)
                .build();

        doNothing().when(validator).validateAll(anyString(), anyString(), any());
        when(repository.existsByCode("NIT")).thenReturn(false);

        // When
        domainService.prepareForCreation(documentType);

        // Then
        assertThat(documentType.getActive()).isTrue();
    }

    @Test
    void prepareForCreation_shouldKeepExistingActiveValue() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .active(false)
                .build();

        doNothing().when(validator).validateAll(anyString(), anyString(), any());
        when(repository.existsByCode("NIT")).thenReturn(false);

        // When
        domainService.prepareForCreation(documentType);

        // Then
        assertThat(documentType.getActive()).isFalse();
    }

    @Test
    void prepareForCreation_shouldThrowExceptionWhenCodeIsDuplicated() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .build();

        doNothing().when(validator).validateAll(anyString(), anyString(), any());
        when(repository.existsByCode("NIT")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> domainService.prepareForCreation(documentType))
                .isInstanceOf(DuplicateCodeException.class);
    }

    @Test
    void prepareForCreation_shouldThrowExceptionWhenValidationFails() {
        // Given
        DocumentType documentType = DocumentType.builder()
                .code("A")
                .name("Name")
                .build();

        doThrow(new InvalidDocumentTypeException("code", "Code must be at least 2 characters"))
                .when(validator).validateAll(anyString(), anyString(), any());

        // When & Then
        assertThatThrownBy(() -> domainService.prepareForCreation(documentType))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Code must be at least 2 characters");
    }

    // ==================== prepareForUpdate Tests ====================

    @Test
    void prepareForUpdate_shouldValidateAndNormalizeCode() {
        // Given
        UUID uuid = UUID.randomUUID();
        DocumentType documentType = DocumentType.builder()
                .uuid(uuid)
                .code("  cc  ")
                .name("Cédula de Ciudadanía")
                .description("Documento de identidad")
                .build();

        doNothing().when(validator).validateAll(anyString(), anyString(), anyString());
        when(repository.existsByCodeExcludingUuid("CC", uuid)).thenReturn(false);

        // When
        domainService.prepareForUpdate(documentType);

        // Then
        assertThat(documentType.getCode()).isEqualTo("CC");
        verify(validator).validateAll("  cc  ", "Cédula de Ciudadanía", "Documento de identidad");
        verify(repository).existsByCodeExcludingUuid("CC", uuid);
    }

    @Test
    void prepareForUpdate_shouldThrowExceptionWhenCodeIsDuplicatedForOtherDocument() {
        // Given
        UUID uuid = UUID.randomUUID();
        DocumentType documentType = DocumentType.builder()
                .uuid(uuid)
                .code("NIT")
                .name("Número de Identificación Tributaria")
                .build();

        doNothing().when(validator).validateAll(anyString(), anyString(), any());
        when(repository.existsByCodeExcludingUuid("NIT", uuid)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> domainService.prepareForUpdate(documentType))
                .isInstanceOf(DuplicateCodeException.class);
    }

    @Test
    void prepareForUpdate_shouldThrowExceptionWhenValidationFails() {
        // Given
        UUID uuid = UUID.randomUUID();
        DocumentType documentType = DocumentType.builder()
                .uuid(uuid)
                .code("A")
                .name("Name")
                .build();

        doThrow(new InvalidDocumentTypeException("code", "Code must be at least 2 characters"))
                .when(validator).validateAll(anyString(), anyString(), any());

        // When & Then
        assertThatThrownBy(() -> domainService.prepareForUpdate(documentType))
                .isInstanceOf(InvalidDocumentTypeException.class)
                .hasMessageContaining("Code must be at least 2 characters");
    }
}
