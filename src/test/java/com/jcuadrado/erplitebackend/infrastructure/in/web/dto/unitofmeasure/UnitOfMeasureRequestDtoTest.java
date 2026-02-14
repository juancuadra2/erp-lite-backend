package com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnitOfMeasureRequestDtoTest {

    @Test
    void createRequestDto_shouldConstructAndAccessFields() {
        CreateUnitOfMeasureRequestDto dto = new CreateUnitOfMeasureRequestDto();
        dto.setName("Caja");
        dto.setAbbreviation("CJ");

        assertThat(dto.getName()).isEqualTo("Caja");
        assertThat(dto.getAbbreviation()).isEqualTo("CJ");
    }

    @Test
    void updateRequestDto_shouldConstructAndAccessFields() {
        UpdateUnitOfMeasureRequestDto dto = new UpdateUnitOfMeasureRequestDto();
        dto.setName("Kilogramo");
        dto.setAbbreviation("KG");

        assertThat(dto.getName()).isEqualTo("Kilogramo");
        assertThat(dto.getAbbreviation()).isEqualTo("KG");
    }
}
