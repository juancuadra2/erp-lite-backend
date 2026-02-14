package com.jcuadrado.erplitebackend.application.usecase.paymentmethod;

import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodConstraintException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodDomainService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ManagePaymentMethodUseCaseImpl
 */
@ExtendWith(MockitoExtension.class)
class ManagePaymentMethodUseCaseImplTest {

    @Mock
    private PaymentMethodRepository repository;

    @Mock
    private PaymentMethodDomainService domainService;

    @InjectMocks
    private ManagePaymentMethodUseCaseImpl useCase;

    @Captor
    private ArgumentCaptor<PaymentMethod> paymentMethodCaptor;

    private UUID sampleUuid;

    @BeforeEach
    void setUp() {
        sampleUuid = UUID.randomUUID();
    }

    @Test
    void create_shouldPrepareValidateAndSavePaymentMethod() {
        // Given
        PaymentMethod newPaymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        PaymentMethod savedPaymentMethod = PaymentMethod.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        doNothing().when(domainService).prepareForCreation(any(PaymentMethod.class));
        when(repository.save(any(PaymentMethod.class))).thenReturn(savedPaymentMethod);

        // When
        PaymentMethod result = useCase.create(newPaymentMethod);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("CASH");

        verify(domainService).prepareForCreation(newPaymentMethod);
        verify(repository).save(paymentMethodCaptor.capture());

        PaymentMethod captured = paymentMethodCaptor.getValue();
        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getCreatedBy()).isEqualTo(0L);
    }

    @Test
    void create_shouldSetCreatedAt() {
        // Given
        PaymentMethod newPaymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        LocalDateTime beforeCreation = LocalDateTime.now();

        doNothing().when(domainService).prepareForCreation(any(PaymentMethod.class));
        when(repository.save(any(PaymentMethod.class))).thenReturn(newPaymentMethod);

        // When
        useCase.create(newPaymentMethod);

        // Then
        verify(repository).save(paymentMethodCaptor.capture());
        PaymentMethod captured = paymentMethodCaptor.getValue();

        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
    }

    @Test
    void update_shouldFindExistingPaymentMethodAndUpdate() {
        // Given
        PaymentMethod existingPaymentMethod = PaymentMethod.builder()
                .id(1L)
                .uuid(sampleUuid)
                .code("CASH")
                .name("Old Name")
                .enabled(true)
                .build();

        PaymentMethod updateData = PaymentMethod.builder()
                .code("CASH")
                .name("New Name")
                .enabled(false)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingPaymentMethod));
        doNothing().when(domainService).prepareForUpdate(any(PaymentMethod.class), eq(sampleUuid));
        when(repository.save(any(PaymentMethod.class))).thenReturn(existingPaymentMethod);

        // When
        PaymentMethod result = useCase.update(sampleUuid, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByUuid(sampleUuid);
        verify(domainService).prepareForUpdate(existingPaymentMethod, sampleUuid);
        verify(repository).save(paymentMethodCaptor.capture());

        PaymentMethod captured = paymentMethodCaptor.getValue();
        assertThat(captured.getCode()).isEqualTo("CASH");
        assertThat(captured.getName()).isEqualTo("New Name");
        assertThat(captured.getEnabled()).isFalse();
        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getUpdatedBy()).isEqualTo(0L);
    }

    @Test
    void update_shouldThrowExceptionWhenPaymentMethodNotFound() {
        // Given
        PaymentMethod updateData = PaymentMethod.builder()
                .code("CASH")
                .name("New Name")
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.update(sampleUuid, updateData))
                .isInstanceOf(PaymentMethodNotFoundException.class);

        verify(repository).findByUuid(sampleUuid);
        verify(domainService, never()).prepareForUpdate(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldSetUpdatedAt() {
        // Given
        PaymentMethod existingPaymentMethod = PaymentMethod.builder()
                .uuid(sampleUuid)
                .code("CASH")
                .name("Efectivo")
                .build();

        PaymentMethod updateData = PaymentMethod.builder()
                .code("CASH")
                .name("Updated Name")
                .build();

        LocalDateTime beforeUpdate = LocalDateTime.now();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingPaymentMethod));
        doNothing().when(domainService).prepareForUpdate(any(), any());
        when(repository.save(any(PaymentMethod.class))).thenReturn(existingPaymentMethod);

        // When
        useCase.update(sampleUuid, updateData);

        // Then
        verify(repository).save(paymentMethodCaptor.capture());
        PaymentMethod captured = paymentMethodCaptor.getValue();

        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    void delete_shouldSoftDeletePaymentMethodWhenNoTransactions() {
        // Given
        PaymentMethod existingPaymentMethod = PaymentMethod.builder()
                .uuid(sampleUuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingPaymentMethod));
        when(repository.countTransactionsWithPaymentMethod(sampleUuid)).thenReturn(0L);
        when(domainService.canDelete( 0L)).thenReturn(true);
        when(repository.save(any(PaymentMethod.class))).thenReturn(existingPaymentMethod);

        // When
        useCase.delete(sampleUuid);

        // Then
        verify(repository).findByUuid(sampleUuid);
        verify(repository).countTransactionsWithPaymentMethod(sampleUuid);
        verify(domainService).canDelete( 0L);
        verify(repository).save(paymentMethodCaptor.capture());

        PaymentMethod captured = paymentMethodCaptor.getValue();
        assertThat(captured.getEnabled()).isFalse();
        assertThat(captured.getDeletedAt()).isNotNull();
        assertThat(captured.getDeletedBy()).isEqualTo(0L);
        assertThat(captured.getUpdatedBy()).isEqualTo(0L);
    }

    @Test
    void delete_shouldThrowExceptionWhenPaymentMethodHasTransactions() {
        // Given
        PaymentMethod existingPaymentMethod = PaymentMethod.builder()
                .uuid(sampleUuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingPaymentMethod));
        when(repository.countTransactionsWithPaymentMethod(sampleUuid)).thenReturn(100L);
        when(domainService.canDelete( 100L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> useCase.delete(sampleUuid))
                .isInstanceOf(PaymentMethodConstraintException.class)
                .hasMessageContaining("Cannot delete payment method with associated transactions");

        verify(repository).findByUuid(sampleUuid);
        verify(repository).countTransactionsWithPaymentMethod(sampleUuid);
        verify(domainService).canDelete( 100L);
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldThrowExceptionWhenPaymentMethodNotFound() {
        // Given
        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.delete(sampleUuid))
                .isInstanceOf(PaymentMethodNotFoundException.class);

        verify(repository).findByUuid(sampleUuid);
        verify(repository, never()).countTransactionsWithPaymentMethod(any());
        verify(repository, never()).save(any());
    }

    @Test
        void activate_shouldActivatePaymentMethod() {
        // Given
        PaymentMethod existingPaymentMethod = PaymentMethod.builder()
                .uuid(sampleUuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(false)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingPaymentMethod));
        when(repository.save(any(PaymentMethod.class))).thenReturn(existingPaymentMethod);

        // When
        PaymentMethod result = useCase.activate(sampleUuid);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByUuid(sampleUuid);
        verify(repository).save(paymentMethodCaptor.capture());

        PaymentMethod captured = paymentMethodCaptor.getValue();
        assertThat(captured.getEnabled()).isTrue();
        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getUpdatedBy()).isEqualTo(0L);
    }

    @Test
    void activate_shouldThrowExceptionWhenPaymentMethodNotFound() {
        // Given
        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.activate(sampleUuid))
                .isInstanceOf(PaymentMethodNotFoundException.class);

        verify(repository).findByUuid(sampleUuid);
        verify(repository, never()).save(any());
    }

    @Test
    void deactivate_shouldDeactivatePaymentMethod() {
        // Given
        PaymentMethod existingPaymentMethod = PaymentMethod.builder()
                .uuid(sampleUuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingPaymentMethod));
        when(repository.countTransactionsWithPaymentMethod(sampleUuid)).thenReturn(0L);
        when(domainService.canDeactivate(0L)).thenReturn(true);
        when(repository.save(any(PaymentMethod.class))).thenReturn(existingPaymentMethod);

        // When
        PaymentMethod result = useCase.deactivate(sampleUuid);

        // Then
        assertThat(result).isNotNull();
        verify(repository).findByUuid(sampleUuid);
        verify(repository).countTransactionsWithPaymentMethod(sampleUuid);
        verify(domainService).canDeactivate(0L);
        verify(repository).save(paymentMethodCaptor.capture());

        PaymentMethod captured = paymentMethodCaptor.getValue();
        assertThat(captured.getEnabled()).isFalse();
        assertThat(captured.getUpdatedAt()).isNotNull();
        assertThat(captured.getDeletedBy()).isEqualTo(0L);
        assertThat(captured.getUpdatedBy()).isEqualTo(0L);
    }

    @Test
    void deactivate_shouldThrowExceptionWhenPaymentMethodNotFound() {
        // Given
        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.deactivate(sampleUuid))
                .isInstanceOf(PaymentMethodNotFoundException.class);

        verify(repository).findByUuid(sampleUuid);
        verify(repository, never()).countTransactionsWithPaymentMethod(any());
        verify(domainService, never()).canDeactivate(anyLong());
        verify(repository, never()).save(any());
    }

    @Test
    void deactivate_shouldThrowExceptionWhenCannotDeactivate() {
        // Given
        PaymentMethod existingPaymentMethod = PaymentMethod.builder()
                .uuid(sampleUuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        when(repository.findByUuid(sampleUuid)).thenReturn(Optional.of(existingPaymentMethod));
        when(repository.countTransactionsWithPaymentMethod(sampleUuid)).thenReturn(10L);
        when(domainService.canDeactivate(10L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> useCase.deactivate(sampleUuid))
                .isInstanceOf(PaymentMethodConstraintException.class);

        verify(repository).findByUuid(sampleUuid);
        verify(repository).countTransactionsWithPaymentMethod(sampleUuid);
        verify(domainService).canDeactivate(10L);
        verify(repository, never()).save(any());
    }
}
