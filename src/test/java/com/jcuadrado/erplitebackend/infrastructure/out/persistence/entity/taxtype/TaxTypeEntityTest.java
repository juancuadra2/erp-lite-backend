package com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.taxtype;

import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TaxTypeEntity - Unit Tests")
class TaxTypeEntityTest {

    @Test
    @DisplayName("onCreate should set defaults when nullable fields are null")
    void onCreate_shouldSetDefaultsWhenNullFields() {
        TaxTypeEntity entity = TaxTypeEntity.builder()
                .id(1L)
                .code("IVA19")
                .name("IVA 19%")
                .percentage(new BigDecimal("19.0000"))
                .applicationType(TaxApplicationType.BOTH)
                .build();

        entity.onCreate();

        assertThat(entity.getUuid()).isNotNull();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getIsIncluded()).isFalse();
    }

    @Test
    @DisplayName("onCreate should preserve existing values")
    void onCreate_shouldPreserveExistingValues() {
        UUID existingUuid = UUID.randomUUID();
        LocalDateTime existingCreatedAt = LocalDateTime.now().minusDays(1);

        TaxTypeEntity entity = TaxTypeEntity.builder()
                .uuid(existingUuid)
                .code("RET")
                .name("ReteFuente")
                .percentage(new BigDecimal("2.5000"))
                .isIncluded(true)
                .applicationType(TaxApplicationType.SALE)
                .enabled(false)
                .createdAt(existingCreatedAt)
                .build();

        entity.onCreate();

        assertThat(entity.getUuid()).isEqualTo(existingUuid);
        assertThat(entity.getCreatedAt()).isEqualTo(existingCreatedAt);
        assertThat(entity.getEnabled()).isFalse();
        assertThat(entity.getIsIncluded()).isTrue();
    }

    @Test
    @DisplayName("onUpdate should set updatedAt")
    void onUpdate_shouldSetUpdatedAt() {
        TaxTypeEntity entity = new TaxTypeEntity();

        entity.onUpdate();

        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("lombok accessors and constructors should work")
    void lombokAccessorsAndConstructors_shouldWork() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusHours(2);
        LocalDateTime updatedAt = LocalDateTime.now().minusHours(1);
        LocalDateTime deletedAt = LocalDateTime.now();

        TaxTypeEntity allArgs = new TaxTypeEntity(
                10L,
                uuid,
                "IVA19",
                "IVA 19%",
                new BigDecimal("19.0000"),
                true,
                TaxApplicationType.BOTH,
                true,
                1L,
                2L,
                3L,
                createdAt,
                updatedAt,
                deletedAt
        );

        assertThat(allArgs.getId()).isEqualTo(10L);
        assertThat(allArgs.getUuid()).isEqualTo(uuid);
        assertThat(allArgs.getCode()).isEqualTo("IVA19");
        assertThat(allArgs.getName()).isEqualTo("IVA 19%");
        assertThat(allArgs.getPercentage()).isEqualByComparingTo("19.0000");
        assertThat(allArgs.getIsIncluded()).isTrue();
        assertThat(allArgs.getApplicationType()).isEqualTo(TaxApplicationType.BOTH);
        assertThat(allArgs.getEnabled()).isTrue();
        assertThat(allArgs.getCreatedBy()).isEqualTo(1L);
        assertThat(allArgs.getUpdatedBy()).isEqualTo(2L);
        assertThat(allArgs.getDeletedBy()).isEqualTo(3L);
        assertThat(allArgs.getCreatedAt()).isEqualTo(createdAt);
        assertThat(allArgs.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(allArgs.getDeletedAt()).isEqualTo(deletedAt);

        TaxTypeEntity noArgs = new TaxTypeEntity();
        noArgs.setId(20L);
        noArgs.setUuid(uuid);
        noArgs.setCode("ICA");
        noArgs.setName("ICA Bogotá");
        noArgs.setPercentage(new BigDecimal("9.6600"));
        noArgs.setIsIncluded(false);
        noArgs.setApplicationType(TaxApplicationType.PURCHASE);
        noArgs.setEnabled(false);
        noArgs.setCreatedBy(11L);
        noArgs.setUpdatedBy(12L);
        noArgs.setDeletedBy(13L);
        noArgs.setCreatedAt(createdAt);
        noArgs.setUpdatedAt(updatedAt);
        noArgs.setDeletedAt(deletedAt);

        assertThat(noArgs.getId()).isEqualTo(20L);
        assertThat(noArgs.getUuid()).isEqualTo(uuid);
        assertThat(noArgs.getCode()).isEqualTo("ICA");
        assertThat(noArgs.getName()).isEqualTo("ICA Bogotá");
        assertThat(noArgs.getPercentage()).isEqualByComparingTo("9.6600");
        assertThat(noArgs.getIsIncluded()).isFalse();
        assertThat(noArgs.getApplicationType()).isEqualTo(TaxApplicationType.PURCHASE);
        assertThat(noArgs.getEnabled()).isFalse();
        assertThat(noArgs.getCreatedBy()).isEqualTo(11L);
        assertThat(noArgs.getUpdatedBy()).isEqualTo(12L);
        assertThat(noArgs.getDeletedBy()).isEqualTo(13L);
        assertThat(noArgs.getCreatedAt()).isEqualTo(createdAt);
        assertThat(noArgs.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(noArgs.getDeletedAt()).isEqualTo(deletedAt);
    }
}
