package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaymentMethodResponseDto
 */
@DisplayName("PaymentMethodResponseDto Tests")
class PaymentMethodResponseDtoTest {

    @Test
    @DisplayName("Should create DTO with builder")
    void shouldCreateDtoWithBuilder() {
        // Given
        Long id = 1L;
        UUID uuid = UUID.randomUUID();
        String code = "CASH";
        String name = "Efectivo";
        Boolean enabled = true;
        LocalDateTime now = LocalDateTime.now();

        // When
        PaymentMethodResponseDto dto = PaymentMethodResponseDto.builder()
                .id(id)
                .uuid(uuid)
                .code(code)
                .name(name)
                .enabled(enabled)
                .createdBy(1L)
                .updatedBy(2L)
                .deletedBy(null)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getCode()).isEqualTo(code);
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getEnabled()).isEqualTo(enabled);
        assertThat(dto.getCreatedBy()).isEqualTo(1L);
        assertThat(dto.getUpdatedBy()).isEqualTo(2L);
        assertThat(dto.getDeletedBy()).isNull();
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
        assertThat(dto.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("Should create DTO with no-args constructor")
    void shouldCreateDtoWithNoArgsConstructor() {
        // When
        PaymentMethodResponseDto dto = new PaymentMethodResponseDto();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isNull();
        assertThat(dto.getUuid()).isNull();
        assertThat(dto.getCode()).isNull();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEnabled()).isNull();
    }

    @Test
    @DisplayName("Should create DTO with all-args constructor")
    void shouldCreateDtoWithAllArgsConstructor() {
        // Given
        Long id = 1L;
        UUID uuid = UUID.randomUUID();
        String code = "CARD";
        String name = "Tarjeta";
        Boolean enabled = false;
        LocalDateTime now = LocalDateTime.now();

        // When
        PaymentMethodResponseDto dto = new PaymentMethodResponseDto(
                id, uuid, code, name, enabled,
                1L, 2L, 3L, now, now, now
        );

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getCode()).isEqualTo(code);
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getEnabled()).isEqualTo(enabled);
    }

    @Test
    @DisplayName("Should set and get all fields")
    void shouldSetAndGetAllFields() {
        // Given
        PaymentMethodResponseDto dto = new PaymentMethodResponseDto();
        Long id = 10L;
        UUID uuid = UUID.randomUUID();
        String code = "TRANSFER";
        String name = "Transferencia";
        Boolean enabled = true;
        LocalDateTime now = LocalDateTime.now();

        // When
        dto.setId(id);
        dto.setUuid(uuid);
        dto.setCode(code);
        dto.setName(name);
        dto.setEnabled(enabled);
        dto.setCreatedBy(1L);
        dto.setUpdatedBy(2L);
        dto.setDeletedBy(3L);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        dto.setDeletedAt(now);

        // Then
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getUuid()).isEqualTo(uuid);
        assertThat(dto.getCode()).isEqualTo(code);
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getEnabled()).isEqualTo(enabled);
        assertThat(dto.getCreatedBy()).isEqualTo(1L);
        assertThat(dto.getUpdatedBy()).isEqualTo(2L);
        assertThat(dto.getDeletedBy()).isEqualTo(3L);
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
        assertThat(dto.getDeletedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        UUID uuid = UUID.randomUUID();
        PaymentMethodResponseDto dto1 = PaymentMethodResponseDto.builder()
                .id(1L)
                .uuid(uuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        PaymentMethodResponseDto dto2 = PaymentMethodResponseDto.builder()
                .id(1L)
                .uuid(uuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        PaymentMethodResponseDto dto3 = PaymentMethodResponseDto.builder()
                .id(2L)
                .uuid(UUID.randomUUID())
                .code("CARD")
                .name("Tarjeta")
                .enabled(false)
                .build();

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        // Given
        PaymentMethodResponseDto dto = PaymentMethodResponseDto.builder()
                .id(1L)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .build();

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("CASH");
        assertThat(toString).contains("Efectivo");
        assertThat(toString).contains("true");
    }
}
