package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.paymentmethod;

import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.paymentmethod.PaymentMethodEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaymentMethodEntityMapper
 */
class PaymentMethodEntityMapperTest {

    private PaymentMethodEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PaymentMethodEntityMapper.class);
    }

    // ==================== toEntity Tests ====================

    @Test
    void toEntity_shouldMapDomainModelToEntity() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .id(1L)
                .uuid(uuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .createdBy(100L)
                .updatedBy(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        PaymentMethodEntity result = mapper.toEntity(paymentMethod);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getCode()).isEqualTo("CASH");
        assertThat(result.getName()).isEqualTo("Efectivo");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(100L);
        assertThat(result.getUpdatedBy()).isEqualTo(101L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldMapAllAuditFields() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime deletedAt = LocalDateTime.now();

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .id(2L)
                .uuid(uuid)
                .code("CC")
                .name("Tarjeta de Crédito")
                .enabled(false)
                .createdBy(200L)
                .updatedBy(201L)
                .deletedBy(202L)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .build();

        // When
        PaymentMethodEntity result = mapper.toEntity(paymentMethod);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreatedBy()).isEqualTo(200L);
        assertThat(result.getUpdatedBy()).isEqualTo(201L);
        assertThat(result.getDeletedBy()).isEqualTo(202L);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(result.getDeletedAt()).isEqualTo(deletedAt);
    }

    @Test
    void toEntity_shouldHandleNullOptionalFields() {
        // Given
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .code("PSE")
                .name("PSE - Pagos Seguros en Línea")
                .build();

        // When
        PaymentMethodEntity result = mapper.toEntity(paymentMethod);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getEnabled()).isNull();
        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
    }

    @Test
    void toEntity_shouldReturnNullWhenInputIsNull() {
        // When
        PaymentMethodEntity result = mapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    // ==================== toDomain Tests ====================

    @Test
    void toDomain_shouldMapEntityToDomainModel() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .id(3L)
                .uuid(uuid)
                .code("TRANSFER")
                .name("Transferencia Bancaria")
                .enabled(true)
                .createdBy(300L)
                .updatedBy(301L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        PaymentMethod result = mapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getCode()).isEqualTo("TRANSFER");
        assertThat(result.getName()).isEqualTo("Transferencia Bancaria");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(300L);
        assertThat(result.getUpdatedBy()).isEqualTo(301L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldMapAllAuditFields() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(3);
        LocalDateTime deletedAt = LocalDateTime.now().minusDays(1);

        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .id(4L)
                .uuid(uuid)
                .code("DC")
                .name("Tarjeta Débito")
                .enabled(false)
                .createdBy(400L)
                .updatedBy(401L)
                .deletedBy(402L)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .deletedAt(deletedAt)
                .build();

        // When
        PaymentMethod result = mapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreatedBy()).isEqualTo(400L);
        assertThat(result.getUpdatedBy()).isEqualTo(401L);
        assertThat(result.getDeletedBy()).isEqualTo(402L);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(result.getDeletedAt()).isEqualTo(deletedAt);
    }

    @Test
    void toDomain_shouldHandleNullOptionalFields() {
        // Given
        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .code("CHECK")
                .name("Cheque")
                .build();

        // When
        PaymentMethod result = mapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getEnabled()).isNull();
        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
    }

    @Test
    void toDomain_shouldReturnNullWhenInputIsNull() {
        // When
        PaymentMethod result = mapper.toDomain(null);

        // Then
        assertThat(result).isNull();
    }

    // ==================== Bidirectional Mapping Tests ====================

    @Test
    void bidirectionalMapping_shouldPreserveAllFields() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod original = PaymentMethod.builder()
                .id(5L)
                .uuid(uuid)
                .code("CREDIT")
                .name("Crédito")
                .enabled(true)
                .createdBy(500L)
                .updatedBy(501L)
                .deletedBy(502L)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(now)
                .build();

        // When
        PaymentMethodEntity entity = mapper.toEntity(original);
        PaymentMethod result = mapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getUuid()).isEqualTo(original.getUuid());
        assertThat(result.getCode()).isEqualTo(original.getCode());
        assertThat(result.getName()).isEqualTo(original.getName());
        assertThat(result.getEnabled()).isEqualTo(original.getEnabled());
        assertThat(result.getCreatedBy()).isEqualTo(original.getCreatedBy());
        assertThat(result.getUpdatedBy()).isEqualTo(original.getUpdatedBy());
        assertThat(result.getDeletedBy()).isEqualTo(original.getDeletedBy());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
        assertThat(result.getDeletedAt()).isEqualTo(original.getDeletedAt());
    }

    @Test
    void bidirectionalMapping_shouldHandleMinimalData() {
        // Given
        PaymentMethod original = PaymentMethod.builder()
                .code("WALLET")
                .name("Billetera Digital")
                .build();

        // When
        PaymentMethodEntity entity = mapper.toEntity(original);
        PaymentMethod result = mapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(original.getCode());
        assertThat(result.getName()).isEqualTo(original.getName());
    }
}
