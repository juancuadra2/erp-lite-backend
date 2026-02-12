package com.jcuadrado.erplitebackend.domain.model.geography;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Municipality domain model
 */
class MunicipalityTest {

    @Test
    void shouldCreateMunicipalityWithBuilder() {
        UUID uuid = UUID.randomUUID();
        String code = "05001";
        String name = "Medellín";
        Department department = Department.builder().id(1L).code("05").name("Antioquia").build();

        Municipality municipality = Municipality.builder()
                .uuid(uuid)
                .code(code)
                .name(name)
                .department(department)
                .enabled(true)
                .build();

        assertThat(municipality).isNotNull();
        assertThat(municipality.getUuid()).isEqualTo(uuid);
        assertThat(municipality.getCode()).isEqualTo(code);
        assertThat(municipality.getName()).isEqualTo(name);
        assertThat(municipality.getDepartment()).isEqualTo(department);
        assertThat(municipality.getEnabled()).isTrue();
    }

    @Test
    void shouldActivateMunicipality() {
        Municipality municipality = Municipality.builder()
                .code("05001")
                .name("Medellín")
                .enabled(false)
                .build();

        municipality.activate();

        assertThat(municipality.getEnabled()).isTrue();
        assertThat(municipality.isEnabled()).isTrue();
    }

    @Test
    void shouldDeactivateMunicipality() {
        Municipality municipality = Municipality.builder()
                .code("05001")
                .name("Medellín")
                .enabled(true)
                .build();

        municipality.deactivate();

        assertThat(municipality.getEnabled()).isFalse();
        assertThat(municipality.isEnabled()).isFalse();
    }

    @Test
    void isEnabled_shouldReturnTrueWhenEnabledIsTrue() {
        Municipality municipality = Municipality.builder().enabled(true).build();
        assertThat(municipality.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_shouldReturnFalseWhenEnabledIsFalse() {
        Municipality municipality = Municipality.builder().enabled(false).build();
        assertThat(municipality.isEnabled()).isFalse();
    }

    @Test
    void isEnabled_shouldReturnFalseWhenEnabledIsNull() {
        Municipality municipality = Municipality.builder().enabled(null).build();
        assertThat(municipality.isEnabled()).isFalse();
    }

    @Test
    void shouldSetAndGetAuditFields() {
        LocalDateTime now = LocalDateTime.now();
        Long userId = 1L;
        Municipality municipality = Municipality.builder()
                .createdBy(userId).createdAt(now).updatedBy(userId).updatedAt(now).build();

        assertThat(municipality.getCreatedBy()).isEqualTo(userId);
        assertThat(municipality.getCreatedAt()).isEqualTo(now);
        assertThat(municipality.getUpdatedBy()).isEqualTo(userId);
        assertThat(municipality.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetId() {
        Municipality municipality = Municipality.builder().id(42L).build();
        assertThat(municipality.getId()).isEqualTo(42L);
    }

    @Test
    void shouldHandleNullDepartment() {
        Municipality municipality = Municipality.builder().department(null).build();
        assertThat(municipality.getDepartment()).isNull();
    }
}
