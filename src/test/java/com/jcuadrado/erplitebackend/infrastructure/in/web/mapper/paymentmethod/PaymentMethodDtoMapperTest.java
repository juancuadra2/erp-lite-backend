package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.paymentmethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jcuadrado.erplitebackend.domain.model.paymentmethod.PaymentMethod;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.CreatePaymentMethodRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.PaymentMethodResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.paymentmethod.UpdatePaymentMethodRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaymentMethodDtoMapper
 */
class PaymentMethodDtoMapperTest {

    private PaymentMethodDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PaymentMethodDtoMapper.class);
    }

    // ==================== toDomain from CreatePaymentMethodRequestDto Tests ====================

    @Test
    void toDomain_shouldMapCreateRequestDtoToDomainModel() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("CASH")
                .name("Efectivo")
                .build();

        // When
        PaymentMethod result = mapper.toDomain(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("CASH");
        assertThat(result.getName()).isEqualTo("Efectivo");
    }

    @Test
    void toDomain_shouldIgnoreSystemFieldsFromCreateRequestDto() {
        // Given
        CreatePaymentMethodRequestDto dto = CreatePaymentMethodRequestDto.builder()
                .code("CC")
                .name("Tarjeta de Crédito")
                .build();

        // When
        PaymentMethod result = mapper.toDomain(dto);

        // Then
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
    void toDomain_shouldReturnNullWhenCreateRequestDtoIsNull() {
        // When
        PaymentMethod result = mapper.toDomain((CreatePaymentMethodRequestDto) null);

        // Then
        assertThat(result).isNull();
    }

    // ==================== toDomain from UpdatePaymentMethodRequestDto Tests ====================

    @Test
    void toDomain_shouldMapUpdateRequestDtoToDomainModel() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("TRANSFER")
                .name("Transferencia Bancaria")
                .enabled(true)
                .build();

        // When
        PaymentMethod result = mapper.toDomain(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("TRANSFER");
        assertThat(result.getName()).isEqualTo("Transferencia Bancaria");
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    void toDomain_shouldMapEnabledFalseFromUpdateRequestDto() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("PSE")
                .name("PSE - Pagos Seguros en Línea")
                .enabled(false)
                .build();

        // When
        PaymentMethod result = mapper.toDomain(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    void toDomain_shouldIgnoreSystemFieldsFromUpdateRequestDto() {
        // Given
        UpdatePaymentMethodRequestDto dto = UpdatePaymentMethodRequestDto.builder()
                .code("DC")
                .name("Tarjeta Débito")
                .enabled(true)
                .build();

        // When
        PaymentMethod result = mapper.toDomain(dto);

        // Then
        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
        assertThat(result.getDeletedAt()).isNull();
    }

    @Test
    void toDomain_shouldReturnNullWhenUpdateRequestDtoIsNull() {
        // When
        PaymentMethod result = mapper.toDomain((UpdatePaymentMethodRequestDto) null);

        // Then
        assertThat(result).isNull();
    }

    // ==================== toResponseDto Tests ====================

    @Test
    void toResponseDto_shouldMapDomainModelToResponseDto() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .uuid(uuid)
                .code("CHECK")
                .name("Cheque")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        PaymentMethodResponseDto result = mapper.toResponseDto(paymentMethod);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.uuid()).isEqualTo(uuid);
        assertThat(result.code()).isEqualTo("CHECK");
        assertThat(result.name()).isEqualTo("Cheque");
        assertThat(result.enabled()).isTrue();
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void toResponseDto_shouldMapAllAuditFields() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .uuid(uuid)
                .code("CREDIT")
                .name("Crédito")
                .enabled(false)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When
        PaymentMethodResponseDto result = mapper.toResponseDto(paymentMethod);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toResponseDto_shouldHandleNullOptionalFields() {
        // Given
        UUID uuid = UUID.randomUUID();

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .uuid(uuid)
                .code("WALLET")
                .name("Billetera Digital")
                .build();

        // When
        PaymentMethodResponseDto result = mapper.toResponseDto(paymentMethod);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.uuid()).isEqualTo(uuid);
        assertThat(result.code()).isEqualTo("WALLET");
        assertThat(result.name()).isEqualTo("Billetera Digital");
        assertThat(result.enabled()).isNull();
        assertThat(result.createdAt()).isNull();
        assertThat(result.updatedAt()).isNull();
    }

    @Test
    void toResponseDto_shouldReturnNullWhenInputIsNull() {
        // When
        PaymentMethodResponseDto result = mapper.toResponseDto(null);

        // Then
        assertThat(result).isNull();
    }

    // ==================== Round-trip Mapping Tests ====================

    @Test
    void roundTripMapping_createRequestToResponseDto() {
        // Given
        CreatePaymentMethodRequestDto createDto = CreatePaymentMethodRequestDto.builder()
                .code("MOBILE")
                .name("Pago Móvil")
                .build();

        // When
        PaymentMethod domain = mapper.toDomain(createDto);
        
        // Simulate system setting UUID and enabled
        domain = PaymentMethod.builder()
                .uuid(UUID.randomUUID())
                .code(domain.getCode())
                .name(domain.getName())
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        PaymentMethodResponseDto responseDto = mapper.toResponseDto(domain);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.code()).isEqualTo(createDto.getCode());
        assertThat(responseDto.name()).isEqualTo(createDto.getName());
        assertThat(responseDto.uuid()).isNotNull();
        assertThat(responseDto.enabled()).isTrue();
    }

    @Test
    void roundTripMapping_updateRequestToResponseDto() {
        // Given
        UpdatePaymentMethodRequestDto updateDto = UpdatePaymentMethodRequestDto.builder()
                .code("GIFT_CARD")
                .name("Tarjeta de Regalo")
                .enabled(true)
                .build();

        // When
        PaymentMethod domain = mapper.toDomain(updateDto);
        
        // Simulate existing record with UUID
        domain = PaymentMethod.builder()
                .uuid(UUID.randomUUID())
                .code(domain.getCode())
                .name(domain.getName())
                .enabled(domain.getEnabled())
                .updatedAt(LocalDateTime.now())
                .build();
        
        PaymentMethodResponseDto responseDto = mapper.toResponseDto(domain);

        // Then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.code()).isEqualTo(updateDto.getCode());
        assertThat(responseDto.name()).isEqualTo(updateDto.getName());
        assertThat(responseDto.enabled()).isEqualTo(updateDto.getEnabled());
        assertThat(responseDto.uuid()).isNotNull();
    }

    @Test
    void toResponseDto_shouldIncludeAllRequiredFields() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusWeeks(2);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(3);

        PaymentMethod paymentMethod = PaymentMethod.builder()
                .id(1L)
                .uuid(uuid)
                .code("CRYPTO")
                .name("Criptomoneda")
                .enabled(true)
                .createdBy(100L)
                .updatedBy(101L)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // When
        PaymentMethodResponseDto result = mapper.toResponseDto(paymentMethod);

        // Then - ResponseDto should only include external fields
        assertThat(result).isNotNull();
        assertThat(result.uuid()).isEqualTo(uuid);
        assertThat(result.code()).isEqualTo("CRYPTO");
        assertThat(result.name()).isEqualTo("Criptomoneda");
        assertThat(result.enabled()).isTrue();
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.updatedAt()).isEqualTo(updatedAt);
        // Internal fields not exposed in DTO
    }

    // ==================== JSON Serialization Tests ====================

    @Test
    void toResponseDto_shouldExcludeDeletedFieldsFromJsonSerialization() throws Exception {
        // Given - PaymentMethod with all audit fields populated
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .id(1L)
                .uuid(uuid)
                .code("CASH")
                .name("Efectivo")
                .enabled(true)
                .createdBy(100L)
                .updatedBy(200L)
                .deletedBy(300L)  // This should be excluded
                .createdAt(now.minusDays(10))
                .updatedAt(now.minusDays(5))
                .deletedAt(now)  // This should be excluded
                .build();

        // When
        PaymentMethodResponseDto result = mapper.toResponseDto(paymentMethod);

        // Then - Serialize to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(result);

        // Verify deletedBy and deletedAt are NOT present in JSON
        assertThat(json).doesNotContain("deletedBy");
        assertThat(json).doesNotContain("deletedAt");

        // Verify other audit fields ARE present
        assertThat(json).contains("createdBy");
        assertThat(json).contains("updatedBy");
        assertThat(json).contains("createdAt");
        assertThat(json).contains("updatedAt");

        // Verify core fields are present
        assertThat(json).contains("uuid");
        assertThat(json).contains("code");
        assertThat(json).contains("name");
        assertThat(json).contains("enabled");
    }
}
