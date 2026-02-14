package com.jcuadrado.erplitebackend.domain.exception.unitofmeasure;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnitOfMeasureExceptionsTest {

    @Test
    void baseException_shouldKeepMessage() {
        UnitOfMeasureException ex = new UnitOfMeasureException("msg");
        assertThat(ex.getMessage()).isEqualTo("msg");
    }

    @Test
    void notFoundException_shouldBuildMessageFromUuid() {
        UUID uuid = UUID.randomUUID();
        UnitOfMeasureNotFoundException ex = new UnitOfMeasureNotFoundException(uuid);
        assertThat(ex.getMessage()).contains(uuid.toString());
    }

    @Test
    void notFoundException_shouldUseCustomMessage() {
        UnitOfMeasureNotFoundException ex = new UnitOfMeasureNotFoundException("not found");
        assertThat(ex.getMessage()).isEqualTo("not found");
    }

    @Test
    void duplicateNameException_shouldContainName() {
        DuplicateUnitOfMeasureNameException ex = new DuplicateUnitOfMeasureNameException("Caja");
        assertThat(ex.getMessage()).contains("Caja");
    }

    @Test
    void duplicateAbbreviationException_shouldContainAbbreviation() {
        DuplicateUnitOfMeasureAbbreviationException ex = new DuplicateUnitOfMeasureAbbreviationException("KG");
        assertThat(ex.getMessage()).contains("KG");
    }

    @Test
    void inUseException_shouldContainUsageCount() {
        UnitOfMeasureInUseException ex = new UnitOfMeasureInUseException(5L);
        assertThat(ex.getMessage()).contains("5");
    }

    @Test
    void inUseException_shouldAcceptCustomMessage() {
        UnitOfMeasureInUseException ex = new UnitOfMeasureInUseException("in use");
        assertThat(ex.getMessage()).isEqualTo("in use");
    }

    @Test
    void invalidDataException_shouldKeepMessage() {
        InvalidUnitOfMeasureDataException ex = new InvalidUnitOfMeasureDataException("bad");
        assertThat(ex.getMessage()).isEqualTo("bad");
    }
}
