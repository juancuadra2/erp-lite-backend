package com.jcuadrado.erplitebackend.domain.service.paymentmethod;

import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.DuplicatePaymentMethodCodeException;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodDomainServiceTest {

    @Mock
    private PaymentMethodRepository repository;

    @Mock
    private PaymentMethodValidator validator;

    @InjectMocks
    private PaymentMethodDomainService domainService;

    // ========== Normalize Code Tests ==========

    @Test
    void normalizeCode_shouldConvertToUppercaseAndTrim() {
        // Given
        String code = "  cash  ";

        // When
        String result = domainService.normalizeCode(code);

        // Then
        assertThat(result).isEqualTo("CASH");
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
        String code = "CASH";

        // When
        String result = domainService.normalizeCode(code);

        // Then
        assertThat(result).isEqualTo("CASH");
    }

    // ========== Validate Unique Code Tests ==========

    @Test
    void validateUniqueCode_shouldPassWhenCodeDoesNotExist() {
        // Given
        String code = "CASH";
        when(repository.existsByCode("CASH")).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> domainService.validateUniqueCode(code));
        verify(repository).existsByCode("CASH");
    }

    @Test
    void validateUniqueCode_shouldThrowExceptionWhenCodeExists() {
        // Given
        String code = "CASH";
        when(repository.existsByCode("CASH")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> domainService.validateUniqueCode(code))
                .isInstanceOf(DuplicatePaymentMethodCodeException.class)
                .hasMessageContaining("CASH");

        verify(repository).existsByCode("CASH");
    }

    @Test
    void validateUniqueCode_shouldNormalizeCodeBeforeChecking() {
        // Given
        String code = "  cash  ";
        when(repository.existsByCode("CASH")).thenReturn(false);

        // When
        domainService.validateUniqueCode(code);

        // Then
        verify(repository).existsByCode("CASH");
    }

    // ========== Validate Unique Code Excluding Tests ==========

    @Test
    void validateUniqueCodeExcluding_shouldPassWhenCodeDoesNotExistForOtherPaymentMethods() {
        // Given
        String code = "CASH";
        UUID uuid = UUID.randomUUID();
        when(repository.existsByCodeAndUuidNot("CASH", uuid)).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> domainService.validateUniqueCodeExcluding(code, uuid));
        verify(repository).existsByCodeAndUuidNot("CASH", uuid);
    }

    @Test
    void validateUniqueCodeExcluding_shouldThrowExceptionWhenCodeExistsForOtherPaymentMethods() {
        // Given
        String code = "CASH";
        UUID uuid = UUID.randomUUID();
        when(repository.existsByCodeAndUuidNot("CASH", uuid)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> domainService.validateUniqueCodeExcluding(code, uuid))
                .isInstanceOf(DuplicatePaymentMethodCodeException.class)
                .hasMessageContaining("CASH");

        verify(repository).existsByCodeAndUuidNot("CASH", uuid);
    }

    // ========== Can Deactivate Tests ==========

    @Test
    void canDeactivate_shouldReturnTrueWhenNoTransactions() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        // When
        boolean result = domainService.canDeactivate(paymentMethod, 0L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canDeactivate_shouldReturnFalseWhenHasTransactions() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        // When
        boolean result = domainService.canDeactivate(paymentMethod, 5L);

        // Then
        assertThat(result).isFalse();
    }

    // ========== Can Delete Tests ==========

    @Test
    void canDelete_shouldReturnTrueWhenNoTransactions() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .build();
        long transactionsCount = 0L;

        // When
        boolean result = domainService.canDelete(paymentMethod, transactionsCount);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canDelete_shouldReturnFalseWhenHasTransactions() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .build();
        long transactionsCount = 100L;

        // When
        boolean result = domainService.canDelete(paymentMethod, transactionsCount);

        // Then
        assertThat(result).isFalse();
    }

    // ========== Prepare For Creation Tests ==========

    @Test
    void prepareForCreation_shouldValidateAndNormalizeCode() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("  cash  ")
                .name("Efectivo")
                .build();

        when(repository.existsByCode("CASH")).thenReturn(false);
        doNothing().when(validator).validateAll(anyString(), anyString());

        // When
        domainService.prepareForCreation(paymentMethod);

        // Then
        assertThat(paymentMethod.getCode()).isEqualTo("CASH");
        verify(validator).validateAll("  cash  ", "Efectivo");
        verify(repository).existsByCode("CASH");
    }

    @Test
    void prepareForCreation_shouldGenerateUuidIfNull() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .uuid(null)
                .code("CASH")
                .name("Efectivo")
                .build();

        when(repository.existsByCode("CASH")).thenReturn(false);
        doNothing().when(validator).validateAll(anyString(), anyString());

        // When
        domainService.prepareForCreation(paymentMethod);

        // Then
        assertThat(paymentMethod.getUuid()).isNotNull();
    }

    @Test
    void prepareForCreation_shouldSetEnabledToTrueIfNull() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(null)
                .build();

        when(repository.existsByCode("CASH")).thenReturn(false);
        doNothing().when(validator).validateAll(anyString(), anyString());

        // When
        domainService.prepareForCreation(paymentMethod);

        // Then
        assertThat(paymentMethod.getEnabled()).isTrue();
    }

    @Test
    void prepareForCreation_shouldKeepExistingUuidAndEnabled() {
        // Given
        UUID existingUuid = UUID.randomUUID();
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .uuid(existingUuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(false)
                .build();

        when(repository.existsByCode("CASH")).thenReturn(false);
        doNothing().when(validator).validateAll(anyString(), anyString());

        // When
        domainService.prepareForCreation(paymentMethod);

        // Then
        assertThat(paymentMethod.getUuid()).isEqualTo(existingUuid);
        assertThat(paymentMethod.getEnabled()).isFalse();
    }

    @Test
    void prepareForCreation_shouldThrowExceptionWhenCodeExists() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        when(repository.existsByCode("CASH")).thenReturn(true);
        doNothing().when(validator).validateAll(anyString(), anyString());

        // When & Then
        assertThatThrownBy(() -> domainService.prepareForCreation(paymentMethod))
                .isInstanceOf(DuplicatePaymentMethodCodeException.class);

        verify(validator).validateAll("CASH", "Efectivo");
        verify(repository).existsByCode("CASH");
    }

    // ========== Prepare For Update Tests ==========

    @Test
    void prepareForUpdate_shouldValidateAndNormalizeCode() {
        // Given
        UUID existingUuid = UUID.randomUUID();
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("  cash  ")
                .name("Efectivo")
                .build();

        when(repository.existsByCodeAndUuidNot("CASH", existingUuid)).thenReturn(false);
        doNothing().when(validator).validateAll(anyString(), anyString());

        // When
        domainService.prepareForUpdate(paymentMethod, existingUuid);

        // Then
        assertThat(paymentMethod.getCode()).isEqualTo("CASH");
        verify(validator).validateAll("  cash  ", "Efectivo");
        verify(repository).existsByCodeAndUuidNot("CASH", existingUuid);
    }

    @Test
    void prepareForUpdate_shouldThrowExceptionWhenCodeExistsForOtherPaymentMethod() {
        // Given
        UUID existingUuid = UUID.randomUUID();
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        when(repository.existsByCodeAndUuidNot("CASH", existingUuid)).thenReturn(true);
        doNothing().when(validator).validateAll(anyString(), anyString());

        // When & Then
        assertThatThrownBy(() -> domainService.prepareForUpdate(paymentMethod, existingUuid))
                .isInstanceOf(DuplicatePaymentMethodCodeException.class);

        verify(validator).validateAll("CASH", "Efectivo");
        verify(repository).existsByCodeAndUuidNot("CASH", existingUuid);
    }
}
