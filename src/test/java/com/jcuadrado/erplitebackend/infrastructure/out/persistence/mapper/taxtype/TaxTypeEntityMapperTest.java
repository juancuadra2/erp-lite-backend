package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype.TaxTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TaxTypeEntityMapper - Unit Tests")
class TaxTypeEntityMapperTest {

    private TaxTypeEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TaxTypeEntityMapper.class);
    }

    @Test
    @DisplayName("toEntity should map domain to entity")
    void toEntity_shouldMapDomainToEntity() {
        UUID uuid = UUID.randomUUID();
        TaxType domain = TaxType.builder()
                .id(1L)
                .uuid(uuid)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .enabled(true)
                .build();

        TaxTypeEntity result = mapper.toEntity(domain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getCode()).isEqualTo("IVA19");
        assertThat(result.getName()).isEqualTo("IVA 19%");
        assertThat(result.getPercentage()).isEqualByComparingTo(new BigDecimal("19.0000"));
        assertThat(result.getIsIncluded()).isFalse();
        assertThat(result.getApplicationType()).isEqualTo(TaxApplicationType.BOTH);
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("toEntity should handle null domain")
    void toEntity_shouldHandleNullDomain() {
        TaxTypeEntity result = mapper.toEntity(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toDomain should map entity to domain")
    void toDomain_shouldMapEntityToDomain() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TaxTypeEntity entity = TaxTypeEntity.builder()
                .id(1L)
                .uuid(uuid)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .isIncluded(false)
                .applicationType(TaxApplicationType.BOTH)
                .enabled(true)
                .createdBy(1L)
                .createdAt(now)
                .build();

        TaxType result = mapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getCode()).isEqualTo("IVA19");
        assertThat(result.getName()).isEqualTo("IVA 19%");
        assertThat(result.getPercentage()).isEqualByComparingTo(new BigDecimal("19.0000"));
        assertThat(result.getIsIncluded()).isFalse();
        assertThat(result.getApplicationType()).isEqualTo(TaxApplicationType.BOTH);
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toDomain should handle null entity")
    void toDomain_shouldHandleNullEntity() {
        TaxType result = mapper.toDomain(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toEntity should preserve all audit fields")
    void toEntity_shouldPreserveAllAuditFields() {
        LocalDateTime now = LocalDateTime.now();
        TaxType domain = TaxType.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .createdBy(1L)
                .updatedBy(2L)
                .deletedBy(3L)
                .createdAt(now)
                .updatedAt(now.plusHours(1))
                .deletedAt(now.plusHours(2))
                .build();

        TaxTypeEntity result = mapper.toEntity(domain);

        assertThat(result.getCreatedBy()).isEqualTo(1L);
        assertThat(result.getUpdatedBy()).isEqualTo(2L);
        assertThat(result.getDeletedBy()).isEqualTo(3L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now.plusHours(1));
        assertThat(result.getDeletedAt()).isEqualTo(now.plusHours(2));
    }

    @Test
    @DisplayName("toDomain should preserve BigDecimal precision")
    void toDomain_shouldPreserveBigDecimalPrecision() {
        TaxTypeEntity entity = TaxTypeEntity.builder()
                .uuid(UUID.randomUUID())
                .code("CUSTOM")
                .name("Custom Tax")
                .percentage(new BigDecimal("19.1234"))
                .build();

        TaxType result = mapper.toDomain(entity);

        assertThat(result.getPercentage()).isEqualByComparingTo(new BigDecimal("19.1234"));
        assertThat(result.getPercentage().scale()).isEqualTo(4);
    }
}
