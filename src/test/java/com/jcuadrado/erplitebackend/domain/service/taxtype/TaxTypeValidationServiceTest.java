package com.jcuadrado.erplitebackend.domain.service.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.DuplicateTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaxTypeValidationService
 */
@DisplayName("TaxTypeValidationService - Unit Tests")
@ExtendWith(MockitoExtension.class)
class TaxTypeValidationServiceTest {

    @Mock
    private TaxTypeRepository repository;

    private TaxTypeValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TaxTypeValidationService(repository);
    }

    @Test
    @DisplayName("ensureCodeIsUnique should not throw exception when code is unique for create")
    void ensureCodeIsUnique_shouldNotThrowExceptionWhenCodeIsUniqueForCreate() {
        // Given
        String code = "IVA19";
        when(repository.existsByCode(code)).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> validationService.ensureCodeIsUnique(code, null));
        verify(repository).existsByCode(code);
        verify(repository, never()).existsByCodeAndUuidNot(anyString(), any());
    }

    @Test
    @DisplayName("ensureCodeIsUnique should throw exception when code exists for create")
    void ensureCodeIsUnique_shouldThrowExceptionWhenCodeExistsForCreate() {
        // Given
        String code = "IVA19";
        when(repository.existsByCode(code)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> validationService.ensureCodeIsUnique(code, null))
                .isInstanceOf(DuplicateTaxTypeCodeException.class)
                .hasMessageContaining("already exists");

        verify(repository).existsByCode(code);
        verify(repository, never()).existsByCodeAndUuidNot(anyString(), any());
    }

    @Test
    @DisplayName("ensureCodeIsUnique should not throw exception when code is unique for update")
    void ensureCodeIsUnique_shouldNotThrowExceptionWhenCodeIsUniqueForUpdate() {
        // Given
        String code = "IVA19";
        UUID excludeUuid = UUID.randomUUID();
        when(repository.existsByCodeAndUuidNot(code, excludeUuid)).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> validationService.ensureCodeIsUnique(code, excludeUuid));
        verify(repository).existsByCodeAndUuidNot(code, excludeUuid);
        verify(repository, never()).existsByCode(anyString());
    }

    @Test
    @DisplayName("ensureCodeIsUnique should throw exception when code exists for another entity")
    void ensureCodeIsUnique_shouldThrowExceptionWhenCodeExistsForAnotherEntity() {
        // Given
        String code = "IVA19";
        UUID excludeUuid = UUID.randomUUID();
        when(repository.existsByCodeAndUuidNot(code, excludeUuid)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> validationService.ensureCodeIsUnique(code, excludeUuid))
                .isInstanceOf(DuplicateTaxTypeCodeException.class)
                .hasMessageContaining("already exists");

        verify(repository).existsByCodeAndUuidNot(code, excludeUuid);
        verify(repository, never()).existsByCode(anyString());
    }

    @Test
    @DisplayName("ensureCodeIsUnique should not throw exception when code belongs to same entity")
    void ensureCodeIsUnique_shouldNotThrowExceptionWhenCodeBelongsToSameEntity() {
        // Given - El mismo código pero es del mismo UUID que estamos actualizando
        String code = "IVA19";
        UUID excludeUuid = UUID.randomUUID();
        when(repository.existsByCodeAndUuidNot(code, excludeUuid)).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> validationService.ensureCodeIsUnique(code, excludeUuid));
        verify(repository).existsByCodeAndUuidNot(code, excludeUuid);
    }
}
