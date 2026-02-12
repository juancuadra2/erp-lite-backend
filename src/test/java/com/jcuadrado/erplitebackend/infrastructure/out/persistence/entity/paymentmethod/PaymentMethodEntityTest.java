package com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaymentMethodEntity lifecycle callbacks
 */
class PaymentMethodEntityTest {

    @Test
    void onCreate_shouldSetDefaultValues() {
        // Given
        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getUuid()).isNotNull();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getEnabled()).isTrue();
    }

    @Test
    void onCreate_shouldNotOverrideExistingUuid() {
        // Given
        UUID existingUuid = UUID.randomUUID();
        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .uuid(existingUuid)
                .code("CC")
                .name("Tarjeta de Crédito")
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getUuid()).isEqualTo(existingUuid);
    }

    @Test
    void onCreate_shouldNotOverrideExistingCreatedAt() {
        // Given
        LocalDateTime existingCreatedAt = LocalDateTime.now().minusDays(1);
        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .code("TRANSFER")
                .name("Transferencia Bancaria")
                .createdAt(existingCreatedAt)
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getCreatedAt()).isEqualTo(existingCreatedAt);
    }

    @Test
    void onCreate_shouldNotOverrideExistingEnabled() {
        // Given
        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .code("PSE")
                .name("PSE - Pagos Seguros en Línea")
                .enabled(false)
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getEnabled()).isFalse();
    }

    @Test
    void onUpdate_shouldSetUpdatedAt() {
        // Given
        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .code("DC")
                .name("Tarjeta Débito")
                .build();

        // When
        entity.onUpdate();

        // Then
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void builder_shouldCreateEntityWithAllFields() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .id(1L)
                .uuid(uuid)
                .code("CHECK")
                .name("Cheque")
                .enabled(true)
                .createdBy(100L)
                .updatedBy(200L)
                .deletedBy(300L)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(now)
                .build();

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getCode()).isEqualTo("CHECK");
        assertThat(entity.getName()).isEqualTo("Cheque");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo(100L);
        assertThat(entity.getUpdatedBy()).isEqualTo(200L);
        assertThat(entity.getDeletedBy()).isEqualTo(300L);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeletedAt()).isEqualTo(now);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        // Given
        PaymentMethodEntity entity = new PaymentMethodEntity();
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        entity.setId(1L);
        entity.setUuid(uuid);
        entity.setCode("CREDIT");
        entity.setName("Crédito");
        entity.setEnabled(true);
        entity.setCreatedBy(100L);
        entity.setUpdatedBy(200L);
        entity.setDeletedBy(300L);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedAt(now);

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getCode()).isEqualTo("CREDIT");
        assertThat(entity.getName()).isEqualTo("Crédito");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo(100L);
        assertThat(entity.getUpdatedBy()).isEqualTo(200L);
        assertThat(entity.getDeletedBy()).isEqualTo(300L);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeletedAt()).isEqualTo(now);
    }
}
