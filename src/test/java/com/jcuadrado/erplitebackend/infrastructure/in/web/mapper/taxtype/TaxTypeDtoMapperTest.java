package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.CreateTaxTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.TaxTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.UpdateTaxTypeRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TaxTypeDtoMapper - Unit Tests")
class TaxTypeDtoMapperTest {

    private TaxTypeDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TaxTypeDtoMapper.class);
    }

    @Test
    @DisplayName("toDomain should map CreateRequestDto to domain model")
    void toDomain_shouldMapCreateRequestDtoToDomainModel() {
        CreateTaxTypeRequestDto dto = CreateTaxTypeRequestDto.builder()
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .build();

        TaxType result = mapper.toDomain(dto);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("IVA19");
        assertThat(result.getName()).isEqualTo("IVA 19%");
        assertThat(result.getPercentage()).isEqualByComparingTo(new BigDecimal("19.0000"));
        assertThat(result.getIsIncluded()).isFalse();
        assertThat(result.getApplicationType()).isEqualTo(TaxApplicationType.BOTH);
    }

    @Test
    @DisplayName("toDomain should ignore system fields from CreateRequestDto")
    void toDomain_shouldIgnoreSystemFieldsFromCreateRequestDto() {
        CreateTaxTypeRequestDto dto = CreateTaxTypeRequestDto.builder()
                .code("IVA5")
                .name("IVA 5%")
                .percentage(new BigDecimal("5.0000"))
                .build();

        TaxType result = mapper.toDomain(dto);

        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getEnabled()).isNull();
        assertThat(result.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("toDomain should return null when CreateRequestDto is null")
    void toDomain_shouldReturnNullWhenCreateRequestDtoIsNull() {
        TaxType result = mapper.toDomain((CreateTaxTypeRequestDto) null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toDomain should map UpdateRequestDto to domain model")
    void toDomain_shouldMapUpdateRequestDtoToDomainModel() {
        UpdateTaxTypeRequestDto dto = UpdateTaxTypeRequestDto.builder()
                .name("IVA 19% Actualizado")
                .percentage(new BigDecimal("19.5000"))
                .isIncluded(true)
                .build();

        TaxType result = mapper.toDomain(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("IVA 19% Actualizado");
        assertThat(result.getPercentage()).isEqualByComparingTo(new BigDecimal("19.5000"));
        assertThat(result.getIsIncluded()).isTrue();
    }

    @Test
    @DisplayName("toDomain should handle partial UpdateRequestDto")
    void toDomain_shouldHandlePartialUpdateRequestDto() {
        UpdateTaxTypeRequestDto dto = UpdateTaxTypeRequestDto.builder()
                .name("Updated Name Only")
                .build();

        TaxType result = mapper.toDomain(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name Only");
        assertThat(result.getCode()).isNull();
        assertThat(result.getPercentage()).isNull();
    }

    @Test
    @DisplayName("toResponseDto should map domain to ResponseDto")
    void toResponseDto_shouldMapDomainToResponseDto() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TaxType domain = TaxType.builder()
                .id(1L)
                .uuid(uuid)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .enabled(true)
                .createdAt(now)
                .build();

        TaxTypeResponseDto result = mapper.toResponseDto(domain);

        assertThat(result).isNotNull();
        assertThat(result.uuid()).isEqualTo(uuid);
        assertThat(result.code()).isEqualTo("IVA19");
        assertThat(result.name()).isEqualTo("IVA 19%");
        assertThat(result.percentage()).isEqualByComparingTo(new BigDecimal("19.0000"));
        assertThat(result.isIncluded()).isFalse();
        assertThat(result.applicationType()).isEqualTo(TaxApplicationType.BOTH);
        assertThat(result.enabled()).isTrue();
        assertThat(result.createdAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toResponseDto should not include deleted fields when null")
    void toResponseDto_shouldNotIncludeDeletedFieldsWhenNull() {
        TaxType domain = TaxType.builder()
                .uuid(UUID.randomUUID())
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .enabled(true)
                .deletedBy(null)
                .deletedAt(null)
                .build();

        TaxTypeResponseDto result = mapper.toResponseDto(domain);

        assertThat(result).isNotNull();
        assertThat(result.deletedBy()).isNull();
        assertThat(result.deletedAt()).isNull();
    }

    @Test
    @DisplayName("toResponseDto should return null when domain is null")
    void toResponseDto_shouldReturnNullWhenDomainIsNull() {
        TaxTypeResponseDto result = mapper.toResponseDto(null);
        assertThat(result).isNull();
    }
}
