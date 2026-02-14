package com.jcuadrado.erplitebackend.domain.model.paymentmethod;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaymentMethod domain model
 */
class PaymentMethodTest {

    @Test
    void shouldCreatePaymentMethodWithBuilder() {
        // Given
        UUID uuid = UUID.randomUUID();
        String code = "CASH";
        String name = "Efectivo";

        // When
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .uuid(uuid)
                .code(code)
                .name(name)
                .enabled(true)
                .build();

        // Then
        assertThat(paymentMethod).isNotNull();
        assertThat(paymentMethod.getUuid()).isEqualTo(uuid);
        assertThat(paymentMethod.getCode()).isEqualTo(code);
        assertThat(paymentMethod.getName()).isEqualTo(name);
        assertThat(paymentMethod.getEnabled()).isTrue();
    }

    @Test
    void shouldActivatePaymentMethod() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(false)
                .deletedBy(1L)
                .deletedAt(LocalDateTime.now())
                .build();

        // When
        paymentMethod.activate();

        // Then
        assertThat(paymentMethod.getEnabled()).isTrue();
        assertThat(paymentMethod.isActive()).isTrue();
        assertThat(paymentMethod.getDeletedBy()).isNull();
        assertThat(paymentMethod.getDeletedAt()).isNull();
    }

    @Test
    void shouldDeactivatePaymentMethod() {
        // Given
        Long userId = 100L;
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        LocalDateTime beforeDeactivation = LocalDateTime.now();

        // When
        paymentMethod.deactivate(userId);

        // Then
        assertThat(paymentMethod.getEnabled()).isFalse();
        assertThat(paymentMethod.isActive()).isFalse();
        assertThat(paymentMethod.getDeletedBy()).isEqualTo(userId);
        assertThat(paymentMethod.getDeletedAt()).isNotNull();
        assertThat(paymentMethod.getDeletedAt()).isAfterOrEqualTo(beforeDeactivation);
    }

    @Test
    void isActive_shouldReturnTrueWhenEnabledIsTrue() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .enabled(true)
                .build();

        // When & Then
        assertThat(paymentMethod.isActive()).isTrue();
    }

    @Test
    void isActive_shouldReturnFalseWhenEnabledIsFalse() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .enabled(false)
                .build();

        // When & Then
        assertThat(paymentMethod.isActive()).isFalse();
    }

    @Test
    void isActive_shouldReturnFalseWhenEnabledIsNull() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .enabled(null)
                .build();

        // When & Then
        assertThat(paymentMethod.isActive()).isFalse();
    }

    @Test
    void isDeleted_shouldReturnFalseWhenDeletedAtIsNull() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .build();

        // When & Then
        assertThat(paymentMethod.isDeleted()).isFalse();
    }

    @Test
    void isDeleted_shouldReturnTrueWhenDeletedAtIsNotNull() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .deletedAt(LocalDateTime.now())
                .build();

        // When & Then
        assertThat(paymentMethod.isDeleted()).isTrue();
    }

    @Test
    void normalizeCode_shouldConvertToUppercaseAndTrim() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("  cash  ")
                .name("Efectivo")
                .build();

        // When
        paymentMethod.normalizeCode();

        // Then
        assertThat(paymentMethod.getCode()).isEqualTo("CASH");
    }

    @Test
    void normalizeCode_shouldHandleNullCode() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code(null)
                .name("Efectivo")
                .build();

        // When
        paymentMethod.normalizeCode();

        // Then
        assertThat(paymentMethod.getCode()).isNull();
    }

    @Test
    void normalizeCode_shouldHandleAlreadyNormalizedCode() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        // When
        paymentMethod.normalizeCode();

        // Then
        assertThat(paymentMethod.getCode()).isEqualTo("CASH");
    }
}
