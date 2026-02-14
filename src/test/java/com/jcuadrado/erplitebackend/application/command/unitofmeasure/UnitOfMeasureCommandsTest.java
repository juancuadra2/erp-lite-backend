package com.jcuadrado.erplitebackend.application.command.unitofmeasure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnitOfMeasureCommandsTest {

    @Test
    void createCommand_shouldExposeFields() {
        CreateUnitOfMeasureCommand command = new CreateUnitOfMeasureCommand("Caja", "CJ", 1L);
        assertThat(command.name()).isEqualTo("Caja");
        assertThat(command.abbreviation()).isEqualTo("CJ");
        assertThat(command.userId()).isEqualTo(1L);
    }

    @Test
    void updateCommand_shouldExposeFields() {
        UpdateUnitOfMeasureCommand command = new UpdateUnitOfMeasureCommand("Caja Grande", "CG", 2L);
        assertThat(command.name()).isEqualTo("Caja Grande");
        assertThat(command.abbreviation()).isEqualTo("CG");
        assertThat(command.userId()).isEqualTo(2L);
    }
}
