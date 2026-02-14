package com.jcuadrado.erplitebackend.domain.model.unitofmeasure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

class UnitOfMeasureTest {

    @Test
    void create_shouldInitializeRequiredFields() {
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .uuid(UUID.randomUUID())
                .name("Caja")
                .abbreviation("CJ")
                .enabled(true)
                .createdBy(10L)
                .createdAt(java.time.LocalDateTime.now())
                .build();

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
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .name("Caja")
                .abbreviation("CJ")
                .createdBy(10L)
                .build();

        unit.setName("Caja Grande");
        unit.setAbbreviation("CG");
        unit.setUpdatedBy(20L);
        unit.setUpdatedAt(java.time.LocalDateTime.now());

        assertThat(unit.getName()).isEqualTo("Caja Grande");
        assertThat(unit.getAbbreviation()).isEqualTo("CG");
        assertThat(unit.getUpdatedBy()).isEqualTo(20L);
        assertThat(unit.getUpdatedAt()).isNotNull();
    }

    @Test
    void deactivateAndActivate_shouldToggleStateAndDeleteAudit() {
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .name("Caja")
                .abbreviation("CJ")
                .enabled(true)
                .build();

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