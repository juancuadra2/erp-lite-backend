package com.jcuadrado.erplitebackend.domain.model.unitofmeasure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnitOfMeasureTest {

    @Test
    void create_shouldInitializeRequiredFields() {
        UnitOfMeasure unit = UnitOfMeasure.create("Caja", "cj", 10L);

        assertThat(unit.getUuid()).isNotNull();
        assertThat(unit.getName()).isEqualTo("Caja");
        assertThat(unit.getAbbreviation()).isEqualTo("CJ");
        assertThat(unit.getEnabled()).isTrue();
        assertThat(unit.getCreatedBy()).isEqualTo(10L);
        assertThat(unit.getCreatedAt()).isNotNull();
        assertThat(unit.isActive()).isTrue();
    }

    @Test
    void update_shouldChangeNameAndAbbreviationAndAudit() {
        UnitOfMeasure unit = UnitOfMeasure.create("Caja", "CJ", 10L);

        unit.update("Caja Grande", "cg", 20L);

        assertThat(unit.getName()).isEqualTo("Caja Grande");
        assertThat(unit.getAbbreviation()).isEqualTo("CG");
        assertThat(unit.getUpdatedBy()).isEqualTo(20L);
        assertThat(unit.getUpdatedAt()).isNotNull();
    }

    @Test
    void deactivateAndActivate_shouldToggleStateAndDeleteAudit() {
        UnitOfMeasure unit = UnitOfMeasure.create("Caja", "CJ", 10L);

        unit.deactivate(30L);

        assertThat(unit.getEnabled()).isFalse();
        assertThat(unit.getDeletedBy()).isEqualTo(30L);
        assertThat(unit.getDeletedAt()).isNotNull();
        assertThat(unit.getUpdatedBy()).isEqualTo(30L);

        unit.activate(40L);

        assertThat(unit.getEnabled()).isTrue();
        assertThat(unit.getDeletedBy()).isNull();
        assertThat(unit.getDeletedAt()).isNull();
        assertThat(unit.getUpdatedBy()).isEqualTo(40L);
        assertThat(unit.getUpdatedAt()).isNotNull();
    }
}