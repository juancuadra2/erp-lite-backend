package com.jcuadrado.erplitebackend.application.usecase.taxtype;

import com.jcuadrado.erplitebackend.domain.exception.taxtype.DuplicateTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxPercentageException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeDataException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeConstraintException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeDomainService;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ManageTaxTypeUseCaseImpl - Command Side (CQRS)
 */
@DisplayName("ManageTaxTypeUseCase - Command Tests")
@ExtendWith(MockitoExtension.class)
class ManageTaxTypeUseCaseImplTest {

    @Mock
    private TaxTypeRepository repository;

    @Mock
    private TaxTypeDomainService domainService;

    @Mock
    private TaxTypeValidationService validationService;

    @InjectMocks
    private ManageTaxTypeUseCaseImpl useCase;

    @Captor
    private ArgumentCaptor<TaxType> taxTypeCaptor;

    private UUID sampleUuid;
    private TaxType sampleTaxType;

    @BeforeEach
    void setUp() {
        sampleUuid = UUID.randomUUID();
        sampleTaxType = TaxType.builder()
                .uuid(sampleUuid)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("create should validate, generate UUID, set defaults and save")
    void create_shouldValidateGenerateUuidSetDefaultsAndSave() {
        // Given
        TaxType newTaxType = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .build();

        TaxType savedTaxType = TaxType.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        doNothing().when(domainService).validateCode(anyString());
        doNothing().when(domainService).validateName(anyString());
        doNothing().when(domainService).validatePercentage(any(BigDecimal.class));
        doNothing().when(validationService).ensureCodeIsUnique(anyString(), any());
        when(repository.save(any(TaxType.class))).thenReturn(savedTaxType);

        LocalDateTime beforeCreate = LocalDateTime.now();

        // When
        TaxType result = useCase.create(newTaxType);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("IVA19");

        verify(domainService).validateCode("IVA19");
        verify(domainService).validateName("IVA 19%");
        verify(domainService).validatePercentage(new BigDecimal("19.0000"));
        verify(validationService).ensureCodeIsUnique("IVA19", null);
        verify(repository).save(taxTypeCaptor.capture());

        TaxType captured = taxTypeCaptor.getValue();
        assertThat(captured.getUuid()).isNotNull();
        assertThat(captured.getEnabled()).isTrue();
        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getCreatedAt()).isAfterOrEqualTo(beforeCreate);
    }

    @Test
    @DisplayName("create should keep existing UUID if provided")
    void create_shouldKeepExistingUuidIfProvided() {
        // Given
        UUID providedUuid = UUID.randomUUID();
        TaxType newTaxType = TaxType.builder()
                .uuid(providedUuid)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .applicationType(TaxApplicationType.BOTH)
                .build();

        doNothing().when(domainService).validateCode(anyString());
        doNothing().when(domainService).validateName(anyString());
        doNothing().when(domainService).validatePercentage(any(BigDecimal.class));
        doNothing().when(validationService).ensureCodeIsUnique(anyString(), any());
        when(repository.save(any(TaxType.class))).thenReturn(newTaxType);

        // When
        useCase.create(newTaxType);

        // Then
        verify(repository).save(taxTypeCaptor.capture());
        TaxType captured = taxTypeCaptor.getValue();
        assertThat(captured.getUuid()).isEqualTo(providedUuid);
    }

    @Test
    @DisplayName("create should set enabled to true by default when null")
    void create_shouldSetEnabledToTrueByDefaultWhenNull() {
        // Given
        TaxType newTaxType = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .enabled(null)
                .build();

        doNothing().when(domainService).validateCode(anyString());
        doNothing().when(domainService).validateName(anyString());
        doNothing().when(domainService).validatePercentage(any(BigDecimal.class));
        doNothing().when(validationService).ensureCodeIsUnique(anyString(), any());
        when(repository.save(any(TaxType.class))).thenReturn(newTaxType);

        // When
        useCase.create(newTaxType);

        // Then
        verify(repository).save(taxTypeCaptor.capture());
        TaxType captured = taxTypeCaptor.getValue();
        assertThat(captured.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("create should not change enabled when already set")
    void create_shouldNotChangeEnabledWhenAlreadySet() {
        // Given
        TaxType newTaxType = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .enabled(false)
                .build();

        doNothing().when(domainService).validateCode(anyString());
        doNothing().when(domainService).validateName(anyString());
        doNothing().when(domainService).validatePercentage(any(BigDecimal.class));
        doNothing().when(validationService).ensureCodeIsUnique(anyString(), any());
        when(repository.save(any(TaxType.class))).thenReturn(newTaxType);

        // When
        useCase.create(newTaxType);

        // Then
        verify(repository).save(taxTypeCaptor.capture());
        TaxType captured = taxTypeCaptor.getValue();
        assertThat(captured.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("create should throw exception when code is invalid")
    void create_shouldThrowExceptionWhenCodeIsInvalid() {
        // Given
        TaxType newTaxType = TaxType.builder()
                .code("invalid code")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .build();

        doThrow(new InvalidTaxTypeCodeException("Invalid code"))
                .when(domainService).validateCode(anyString());

        // When & Then
        assertThatThrownBy(() -> useCase.create(newTaxType))
                .isInstanceOf(InvalidTaxTypeCodeException.class);

        verify(domainService).validateCode("invalid code");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create should throw exception when name is invalid")
    void create_shouldThrowExceptionWhenNameIsInvalid() {
        // Given
        TaxType newTaxType = TaxType.builder()
                .code("IVA19")
                .name("")
                .percentage(new BigDecimal("19.0000"))
                .build();

        doNothing().when(domainService).validateCode(anyString());
        doThrow(new InvalidTaxTypeDataException("Invalid name"))
                .when(domainService).validateName(anyString());

        // When & Then
        assertThatThrownBy(() -> useCase.create(newTaxType))
                .isInstanceOf(InvalidTaxTypeDataException.class);

        verify(domainService).validateName("");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create should throw exception when percentage is invalid")
    void create_shouldThrowExceptionWhenPercentageIsInvalid() {
        // Given
        TaxType newTaxType = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("150.0000"))
                .build();

        doNothing().when(domainService).validateCode(anyString());
        doNothing().when(domainService).validateName(anyString());
        doThrow(new InvalidTaxPercentageException("Invalid percentage"))
                .when(domainService).validatePercentage(any(BigDecimal.class));

        // When & Then
        assertThatThrownBy(() -> useCase.create(newTaxType))
                .isInstanceOf(InvalidTaxPercentageException.class);

        verify(domainService).validatePercentage(new BigDecimal("150.0000"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create should throw exception when code already exists")
    void create_shouldThrowExceptionWhenCodeAlreadyExists() {
        // Given
        TaxType newTaxType = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .build();

        doNothing().when(domainService).validateCode(anyString());
        doNothing().when(domainService).validateName(anyString());
        doNothing().when(domainService).validatePercentage(any(BigDecimal.class));
        doThrow(new DuplicateTaxTypeCodeException("Code already exists"))
                .when(validationService).ensureCodeIsUnique(anyString(), any());

        // When & Then
        assertThatThrownBy(() -> useCase.create(newTaxType))
                .isInstanceOf(DuplicateTaxTypeCodeException.class);

        verify(validationService).ensureCodeIsUnique("IVA19", null);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update should find existing and update all fields")
    void update_shouldFindExistingAndUpdateAllFields() {
        // Given
        TaxType existing = sampleTaxType;
        TaxType updates = TaxType.builder()
                .code("IVA5")
                .name("IVA 5%")
                .percentage(new BigDecimal("5.0000"))
                .isIncluded(true)
                .applicationType(TaxApplicationType.SALE)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        doNothing().when(domainService).validateCode(anyString());
        doNothing().when(domainService).validateName(anyString());
        doNothing().when(domainService).validatePercentage(any(BigDecimal.class));
        doNothing().when(validationService).ensureCodeIsUnique(anyString(), eq(sampleUuid));
        when(repository.save(any(TaxType.class))).thenReturn(existing);

        LocalDateTime beforeUpdate = LocalDateTime.now();

        // When
        TaxType result = useCase.update(sampleUuid, updates);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByUuid(sampleUuid);
        verify(domainService).validateCode("IVA5");
        verify(domainService).validateName("IVA 5%");
        verify(domainService).validatePercentage(new BigDecimal("5.0000"));
        verify(validationService).ensureCodeIsUnique("IVA5", sampleUuid);
        verify(repository).save(taxTypeCaptor.capture());

        TaxType captured = taxTypeCaptor.getValue();
        assertThat(captured.getCode()).isEqualTo("IVA5");
        assertThat(captured.getName()).isEqualTo("IVA 5%");
        assertThat(captured.getPercentage()).isEqualByComparingTo(new BigDecimal("5.0000"));
        assertThat(captured.getIsIncluded()).isTrue();
        assertThat(captured.getApplicationType()).isEqualTo(TaxApplicationType.SALE);
        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("update should not validate code uniqueness when code unchanged")
    void update_shouldNotValidateCodeUniquenessWhenCodeUnchanged() {
        // Given
        TaxType existing = sampleTaxType;
        TaxType updates = TaxType.builder()
                .code("IVA19") // Same code
                .name("Updated Name")
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        doNothing().when(domainService).validateCode(anyString());
        doNothing().when(domainService).validateName(anyString());
        when(repository.save(any(TaxType.class))).thenReturn(existing);

        // When
        useCase.update(sampleUuid, updates);

        // Then
        verify(domainService).validateCode("IVA19");
        verify(validationService, never()).ensureCodeIsUnique(anyString(), any());
        verify(repository).save(any());
    }

    @Test
    @DisplayName("update should only update provided fields")
    void update_shouldOnlyUpdateProvidedFields() {
        // Given
        TaxType existing = sampleTaxType;
        TaxType updates = TaxType.builder()
                .name("Updated Name Only")
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        doNothing().when(domainService).validateName(anyString());
        when(repository.save(any(TaxType.class))).thenReturn(existing);

        // When
        useCase.update(sampleUuid, updates);

        // Then
        verify(repository).save(taxTypeCaptor.capture());
        TaxType captured = taxTypeCaptor.getValue();
        assertThat(captured.getName()).isEqualTo("Updated Name Only");
        assertThat(captured.getCode()).isEqualTo("IVA19"); // Unchanged
        assertThat(captured.getPercentage()).isEqualByComparingTo(new BigDecimal("19.0000")); // Unchanged
    }

    @Test
    @DisplayName("update should not update name when name is null in updates")
    void update_shouldNotUpdateNameWhenNameIsNullInUpdates() {
        // Given
        TaxType existing = sampleTaxType;
        TaxType updates = TaxType.builder()
                .name(null) // Explicitly null
                .code("IVA5") // Other field to update
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        doNothing().when(domainService).validateCode(anyString());
        doNothing().when(validationService).ensureCodeIsUnique(anyString(), eq(sampleUuid));
        when(repository.save(any(TaxType.class))).thenReturn(existing);

        // When
        useCase.update(sampleUuid, updates);

        // Then
        verify(repository).save(taxTypeCaptor.capture());
        TaxType captured = taxTypeCaptor.getValue();
        assertThat(captured.getName()).isEqualTo("IVA 19%"); // Unchanged
        assertThat(captured.getCode()).isEqualTo("IVA5"); // Updated
    }

    @Test
    @DisplayName("update should throw exception when tax type not found")
    void update_shouldThrowExceptionWhenTaxTypeNotFound() {
        // Given
        TaxType updates = TaxType.builder()
                .name("Updated Name")
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.update(sampleUuid, updates))
                .isInstanceOf(TaxTypeNotFoundException.class);

        verify(repository).findByUuid(sampleUuid);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw exception when updated code is invalid")
    void update_shouldThrowExceptionWhenUpdatedCodeIsInvalid() {
        // Given
        TaxType existing = sampleTaxType;
        TaxType updates = TaxType.builder()
                .code("invalid code")
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        doThrow(new InvalidTaxTypeCodeException("Invalid code"))
                .when(domainService).validateCode(anyString());

        // When & Then
        assertThatThrownBy(() -> useCase.update(sampleUuid, updates))
                .isInstanceOf(InvalidTaxTypeCodeException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("activate should find, activate and update tax type")
    void activate_shouldFindActivateAndUpdateTaxType() {
        // Given
        TaxType existing = TaxType.builder()
                .uuid(sampleUuid)
                .code("IVA19")
                .name("IVA 19%")
                .enabled(false)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        when(repository.save(any(TaxType.class))).thenReturn(existing);

        LocalDateTime beforeActivate = LocalDateTime.now();

        // When
        useCase.activate(sampleUuid);

        // Then
        verify(repository).findByUuid(sampleUuid);
        verify(repository).save(taxTypeCaptor.capture());

        TaxType captured = taxTypeCaptor.getValue();
        assertThat(captured.getEnabled()).isTrue();
        assertThat(captured.getDeletedBy()).isNull();
        assertThat(captured.getDeletedAt()).isNull();
        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getUpdatedAt()).isAfterOrEqualTo(beforeActivate);
    }

    @Test
    @DisplayName("activate should throw exception when tax type not found")
    void activate_shouldThrowExceptionWhenTaxTypeNotFound() {
        // Given
        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.activate(sampleUuid))
                .isInstanceOf(TaxTypeNotFoundException.class);

        verify(repository).findByUuid(sampleUuid);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deactivate should find, deactivate and update tax type")
    void deactivate_shouldFindDeactivateAndUpdateTaxType() {
        // Given
        TaxType existing = TaxType.builder()
                .uuid(sampleUuid)
                .code("IVA19")
                .name("IVA 19%")
                .enabled(true)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        when(repository.save(any(TaxType.class))).thenReturn(existing);

        LocalDateTime beforeDeactivate = LocalDateTime.now();

        // When
        useCase.deactivate(sampleUuid);

        // Then
        verify(repository).findByUuid(sampleUuid);
        verify(repository).save(taxTypeCaptor.capture());

        TaxType captured = taxTypeCaptor.getValue();
        assertThat(captured.getEnabled()).isFalse();
        assertThat(captured.getDeletedBy()).isEqualTo(1L); // Temporal value
        assertThat(captured.getDeletedAt()).isNotNull();
        assertThat(captured.getDeletedAt()).isAfterOrEqualTo(beforeDeactivate);
        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getUpdatedAt()).isAfterOrEqualTo(beforeDeactivate);
    }

    @Test
    @DisplayName("deactivate should throw exception when tax type not found")
    void deactivate_shouldThrowExceptionWhenTaxTypeNotFound() {
        // Given
        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.deactivate(sampleUuid))
                .isInstanceOf(TaxTypeNotFoundException.class);

        verify(repository).findByUuid(sampleUuid);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete should remove tax type when no associations")
    void delete_shouldRemoveTaxTypeWhenNoAssociations() {
        // Given
        TaxType existing = sampleTaxType;

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        when(repository.countProductsWithTaxType(sampleUuid)).thenReturn(0L);
        when(repository.countTransactionsWithTaxType(sampleUuid)).thenReturn(0L);
        when(domainService.canBeDeleted(existing, 0L, 0L)).thenReturn(true);
        doNothing().when(repository).delete(any(TaxType.class));

        // When
        useCase.delete(sampleUuid);

        // Then
        verify(repository).findByUuid(sampleUuid);
        verify(repository).countProductsWithTaxType(sampleUuid);
        verify(repository).countTransactionsWithTaxType(sampleUuid);
        verify(domainService).canBeDeleted(existing, 0L, 0L);
        verify(repository).delete(existing);
    }

    @Test
    @DisplayName("delete should throw exception when has associated products")
    void delete_shouldThrowExceptionWhenHasAssociatedProducts() {
        // Given
        TaxType existing = sampleTaxType;

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        when(repository.countProductsWithTaxType(sampleUuid)).thenReturn(10L);
        when(repository.countTransactionsWithTaxType(sampleUuid)).thenReturn(0L);
        when(domainService.canBeDeleted(existing, 10L, 0L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> useCase.delete(sampleUuid))
                .isInstanceOf(TaxTypeConstraintException.class)
                .hasMessageContaining("associated products or transactions");

        verify(repository).findByUuid(sampleUuid);
        verify(repository).countProductsWithTaxType(sampleUuid);
        verify(repository).countTransactionsWithTaxType(sampleUuid);
        verify(domainService).canBeDeleted(existing, 10L, 0L);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete should throw exception when has associated transactions")
    void delete_shouldThrowExceptionWhenHasAssociatedTransactions() {
        // Given
        TaxType existing = sampleTaxType;

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existing));
        when(repository.countProductsWithTaxType(sampleUuid)).thenReturn(0L);
        when(repository.countTransactionsWithTaxType(sampleUuid)).thenReturn(5L);
        when(domainService.canBeDeleted(existing, 0L, 5L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> useCase.delete(sampleUuid))
                .isInstanceOf(TaxTypeConstraintException.class)
                .hasMessageContaining("associated products or transactions");

        verify(repository).findByUuid(sampleUuid);
        verify(repository).countProductsWithTaxType(sampleUuid);
        verify(repository).countTransactionsWithTaxType(sampleUuid);
        verify(domainService).canBeDeleted(existing, 0L, 5L);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete should throw exception when tax type not found")
    void delete_shouldThrowExceptionWhenTaxTypeNotFound() {
        // Given
        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.delete(sampleUuid))
                .isInstanceOf(TaxTypeNotFoundException.class);

        verify(repository).findByUuid(sampleUuid);
        verify(repository, never()).countProductsWithTaxType(any());
        verify(repository, never()).countTransactionsWithTaxType(any());
        verify(repository, never()).delete(any());
    }
}
