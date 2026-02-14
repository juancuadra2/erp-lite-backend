package com.jcuadrado.erplitebackend.domain.model.taxtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TaxApplicationType enum
 */
@DisplayName("TaxApplicationType - Enum Tests")
class TaxApplicationTypeTest {

    @Test
    @DisplayName("should have SALE value")
    void shouldHaveSaleValue() {
        // When
        TaxApplicationType type = TaxApplicationType.SALE;

        // Then
        assertThat(type).isNotNull();
        assertThat(type.name()).isEqualTo("SALE");
    }

    @Test
    @DisplayName("should have PURCHASE value")
    void shouldHavePurchaseValue() {
        // When
        TaxApplicationType type = TaxApplicationType.PURCHASE;

        // Then
        assertThat(type).isNotNull();
        assertThat(type.name()).isEqualTo("PURCHASE");
    }

    @Test
    @DisplayName("should have BOTH value")
    void shouldHaveBothValue() {
        // When
        TaxApplicationType type = TaxApplicationType.BOTH;

        // Then
        assertThat(type).isNotNull();
        assertThat(type.name()).isEqualTo("BOTH");
    }

    @Test
    @DisplayName("should have exactly three values")
    void shouldHaveExactlyThreeValues() {
        // When
        TaxApplicationType[] values = TaxApplicationType.values();

        // Then
        assertThat(values).hasSize(3);
        assertThat(values).containsExactlyInAnyOrder(
                TaxApplicationType.SALE,
                TaxApplicationType.PURCHASE,
                TaxApplicationType.BOTH
        );
    }

    @Test
    @DisplayName("should support valueOf")
    void shouldSupportValueOf() {
        // When
        TaxApplicationType sale = TaxApplicationType.valueOf("SALE");
        TaxApplicationType purchase = TaxApplicationType.valueOf("PURCHASE");
        TaxApplicationType both = TaxApplicationType.valueOf("BOTH");

        // Then
        assertThat(sale).isEqualTo(TaxApplicationType.SALE);
        assertThat(purchase).isEqualTo(TaxApplicationType.PURCHASE);
        assertThat(both).isEqualTo(TaxApplicationType.BOTH);
    }
}
