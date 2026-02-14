package com.jcuadrado.erplitebackend.domain.model.taxtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TaxApplicationType - Enum Tests")
class TaxApplicationTypeTest {

    @Test
    @DisplayName("should have SALE value")
    void shouldHaveSaleValue() {
        TaxApplicationType type = TaxApplicationType.SALE;

        assertThat(type).isNotNull();
        assertThat(type.name()).isEqualTo("SALE");
    }

    @Test
    @DisplayName("should have PURCHASE value")
    void shouldHavePurchaseValue() {
        TaxApplicationType type = TaxApplicationType.PURCHASE;

        assertThat(type).isNotNull();
        assertThat(type.name()).isEqualTo("PURCHASE");
    }

    @Test
    @DisplayName("should have BOTH value")
    void shouldHaveBothValue() {
        TaxApplicationType type = TaxApplicationType.BOTH;

        assertThat(type).isNotNull();
        assertThat(type.name()).isEqualTo("BOTH");
    }

    @Test
    @DisplayName("should have exactly three values")
    void shouldHaveExactlyThreeValues() {
        TaxApplicationType[] values = TaxApplicationType.values();

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
        TaxApplicationType sale = TaxApplicationType.valueOf("SALE");
        TaxApplicationType purchase = TaxApplicationType.valueOf("PURCHASE");
        TaxApplicationType both = TaxApplicationType.valueOf("BOTH");

        assertThat(sale).isEqualTo(TaxApplicationType.SALE);
        assertThat(purchase).isEqualTo(TaxApplicationType.PURCHASE);
        assertThat(both).isEqualTo(TaxApplicationType.BOTH);
    }
}
