package com.jcuadrado.erplitebackend.domain.model.taxtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TaxType domain model
 */
@DisplayName("TaxType - Domain Model Tests")
class TaxTypeTest {

    @Test
    @DisplayName("should create tax type with builder")
    void shouldCreateTaxTypeWithBuilder() {
        // Given
        UUID uuid = UUID.randomUUID();
        String code = "IVA19";
        String name = "IVA 19%";
        BigDecimal percentage = new BigDecimal("19.0000");
        TaxApplicationType applicationType = TaxApplicationType.BOTH;

        // When
        TaxType taxType = TaxType.builder()
                .uuid(uuid)
                .code(code)
                .name(name)
                .percentage(percentage)
                .isIncluded(false)
                .applicationType(applicationType)
                .enabled(true)
                .build();

        // Then
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
        // Given
        TaxType taxType = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .enabled(false)
                .deletedBy(100L)
                .deletedAt(LocalDateTime.now())
                .build();

        // When
        taxType.activate();

        // Then
        assertThat(taxType.getEnabled()).isTrue();
        assertThat(taxType.isActive()).isTrue();
        assertThat(taxType.getDeletedBy()).isNull();
        assertThat(taxType.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("should deactivate tax type")
    void shouldDeactivateTaxType() {
        // Given
        Long userId = 100L;
        TaxType taxType = TaxType.builder()
                .code("IVA19")
                .name("IVA 19%")
                .enabled(true)
                .build();

        LocalDateTime beforeDeactivation = LocalDateTime.now();

        // When
        taxType.deactivate(userId);

        // Then
        assertThat(taxType.getEnabled()).isFalse();
        assertThat(taxType.isActive()).isFalse();
        assertThat(taxType.getDeletedBy()).isEqualTo(userId);
        assertThat(taxType.getDeletedAt()).isNotNull();
        assertThat(taxType.getDeletedAt()).isAfterOrEqualTo(beforeDeactivation);
    }

    @Test
    @DisplayName("isActive should return true when enabled is true")
    void isActive_shouldReturnTrueWhenEnabledIsTrue() {
        // Given
        TaxType taxType = TaxType.builder()
                .enabled(true)
                .build();

        // When & Then
        assertThat(taxType.isActive()).isTrue();
    }

    @Test
    @DisplayName("isActive should return false when enabled is false")
    void isActive_shouldReturnFalseWhenEnabledIsFalse() {
        // Given
        TaxType taxType = TaxType.builder()
                .enabled(false)
                .build();

        // When & Then
        assertThat(taxType.isActive()).isFalse();
    }

    @Test
    @DisplayName("isActive should return false when enabled is null")
    void isActive_shouldReturnFalseWhenEnabledIsNull() {
        // Given
        TaxType taxType = TaxType.builder()
                .enabled(null)
                .build();

        // When & Then
        assertThat(taxType.isActive()).isFalse();
    }

    @Test
    @DisplayName("isApplicableForSales should return true when application type is SALE")
    void isApplicableForSales_shouldReturnTrueWhenApplicationTypeIsSale() {
        // Given
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.SALE)
                .build();

        // When & Then
        assertThat(taxType.isApplicableForSales()).isTrue();
    }

    @Test
    @DisplayName("isApplicableForSales should return true when application type is BOTH")
    void isApplicableForSales_shouldReturnTrueWhenApplicationTypeIsBoth() {
        // Given
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.BOTH)
                .build();

        // When & Then
        assertThat(taxType.isApplicableForSales()).isTrue();
    }

    @Test
    @DisplayName("isApplicableForSales should return false when application type is PURCHASE")
    void isApplicableForSales_shouldReturnFalseWhenApplicationTypeIsPurchase() {
        // Given
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.PURCHASE)
                .build();

        // When & Then
        assertThat(taxType.isApplicableForSales()).isFalse();
    }

    @Test
    @DisplayName("isApplicableForPurchases should return true when application type is PURCHASE")
    void isApplicableForPurchases_shouldReturnTrueWhenApplicationTypeIsPurchase() {
        // Given
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.PURCHASE)
                .build();

        // When & Then
        assertThat(taxType.isApplicableForPurchases()).isTrue();
    }

    @Test
    @DisplayName("isApplicableForPurchases should return true when application type is BOTH")
    void isApplicableForPurchases_shouldReturnTrueWhenApplicationTypeIsBoth() {
        // Given
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.BOTH)
                .build();

        // When & Then
        assertThat(taxType.isApplicableForPurchases()).isTrue();
    }

    @Test
    @DisplayName("isApplicableForPurchases should return false when application type is SALE")
    void isApplicableForPurchases_shouldReturnFalseWhenApplicationTypeIsSale() {
        // Given
        TaxType taxType = TaxType.builder()
                .applicationType(TaxApplicationType.SALE)
                .build();

        // When & Then
        assertThat(taxType.isApplicableForPurchases()).isFalse();
    }

    @Test
    @DisplayName("isValidPercentage should return true when percentage is valid")
    void isValidPercentage_shouldReturnTrueWhenPercentageIsValid() {
        // Given
        TaxType taxType = TaxType.builder()
                .percentage(new BigDecimal("19.0000"))
                .build();

        // When & Then
        assertThat(taxType.isValidPercentage()).isTrue();
    }

    @Test
    @DisplayName("isValidPercentage should return true when percentage is zero")
    void isValidPercentage_shouldReturnTrueWhenPercentageIsZero() {
        // Given
        TaxType taxType = TaxType.builder()
                .percentage(BigDecimal.ZERO)
                .build();

        // When & Then
        assertThat(taxType.isValidPercentage()).isTrue();
    }

    @Test
    @DisplayName("isValidPercentage should return true when percentage is one hundred")
    void isValidPercentage_shouldReturnTrueWhenPercentageIsOneHundred() {
        // Given
        TaxType taxType = TaxType.builder()
                .percentage(new BigDecimal("100.0000"))
                .build();

        // When & Then
        assertThat(taxType.isValidPercentage()).isTrue();
    }

    @Test
    @DisplayName("isValidPercentage should return false when percentage is negative")
    void isValidPercentage_shouldReturnFalseWhenPercentageIsNegative() {
        // Given
        TaxType taxType = TaxType.builder()
                .percentage(new BigDecimal("-1.0000"))
                .build();

        // When & Then
        assertThat(taxType.isValidPercentage()).isFalse();
    }

    @Test
    @DisplayName("isValidPercentage should return false when percentage exceeds one hundred")
    void isValidPercentage_shouldReturnFalseWhenPercentageExceedsOneHundred() {
        // Given
        TaxType taxType = TaxType.builder()
                .percentage(new BigDecimal("100.0001"))
                .build();

        // When & Then
        assertThat(taxType.isValidPercentage()).isFalse();
    }

    @Test
    @DisplayName("isValidPercentage should return false when percentage is null")
    void isValidPercentage_shouldReturnFalseWhenPercentageIsNull() {
        // Given
        TaxType taxType = TaxType.builder()
                .percentage(null)
                .build();

        // When & Then
        assertThat(taxType.isValidPercentage()).isFalse();
    }
}
