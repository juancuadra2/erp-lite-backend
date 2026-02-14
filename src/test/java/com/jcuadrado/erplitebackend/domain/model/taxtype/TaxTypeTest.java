package com.jcuadrado.erplitebackend.domain.model.taxtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TaxType - Domain Model Tests")
class TaxTypeTest {

    @Test
    @DisplayName("should create tax type with builder")
    void shouldCreateTaxTypeWithBuilder() {
        UUID uuid = UUID.randomUUID();
        String code = "IVA19";
        String name = "IVA 19%";
        BigDecimal percentage = new BigDecimal("19.0000");
        TaxApplicationType applicationType = TaxApplicationType.BOTH;

        TaxType taxType = TaxType.builder()
                .uuid(uuid)
                .code(code)
                .name(name)
                .percentage(percentage)
                .isIncluded(false)
                .applicationType(applicationType)
                .enabled(true)
                .build();

        assertThat(taxType).isNotNull();
        assertThat(taxType.getUuid()).isEqualTo(uuid);
        assertThat(taxType.getCode()).isEqualTo(code);
        assertThat(taxType.getName()).isEqualTo(name);
        assertThat(taxType.getPercentage()).isEqualByComparingTo(percentage);
        assertThat(taxType.getIsIncluded()).isFalse();
        assertThat(taxType.getApplicationType()).isEqualTo(applicationType);
        assertThat(taxType.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("should activate tax type")
    void shouldActivateTaxType() {
        TaxType taxType = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .enabled(false)
                .deletedBy(100L)
                .deletedAt(LocalDateTime.now())
                .build();

        taxType.activate();

        assertThat(taxType.getEnabled()).isTrue();
        assertThat(taxType.isActive()).isTrue();
        assertThat(taxType.getDeletedBy()).isNull();
        assertThat(taxType.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("should deactivate tax type")
    void shouldDeactivateTaxType() {
        Long userId = 100L;
        TaxType taxType = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .enabled(true)
                .build();

        LocalDateTime beforeDeactivation = LocalDateTime.now();

        taxType.deactivate(userId);

        assertThat(taxType.getEnabled()).isFalse();
        assertThat(taxType.isActive()).isFalse();
        assertThat(taxType.getDeletedBy()).isEqualTo(userId);
        assertThat(taxType.getDeletedAt()).isNotNull();
        assertThat(taxType.getDeletedAt()).isAfterOrEqualTo(beforeDeactivation);
    }

    @Test
    @DisplayName("isActive should return true when enabled is true")
    void isActive_shouldReturnTrueWhenEnabledIsTrue() {
        TaxType taxType = TaxType.builder()
                .enabled(true)
                .build();

        assertThat(taxType.isActive()).isTrue();
    }

    @Test
    @DisplayName("isActive should return false when enabled is false")
    void isActive_shouldReturnFalseWhenEnabledIsFalse() {
        TaxType taxType = TaxType.builder()
                .enabled(false)
                .build();

        assertThat(taxType.isActive()).isFalse();
    }

    @Test
    @DisplayName("isActive should return false when enabled is null")
    void isActive_shouldReturnFalseWhenEnabledIsNull() {
        TaxType taxType = TaxType.builder()
                .enabled(null)
                .build();

        assertThat(taxType.isActive()).isFalse();
    }

}
