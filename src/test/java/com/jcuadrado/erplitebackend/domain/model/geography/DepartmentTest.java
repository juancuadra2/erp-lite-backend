package com.jcuadrado.erplitebackend.domain.model.geography;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Department domain model
 */
class DepartmentTest {

    @Test
    void shouldCreateDepartmentWithBuilder() {
        UUID uuid = UUID.randomUUID();
        String code = "05";
        String name = "Antioquia";

        Department department = Department.builder()
                .uuid(uuid)
                .code(code)
                .name(name)
                .enabled(true)
                .build();

        assertThat(department).isNotNull();
        assertThat(department.getUuid()).isEqualTo(uuid);
        assertThat(department.getCode()).isEqualTo(code);
        assertThat(department.getName()).isEqualTo(name);
        assertThat(department.getEnabled()).isTrue();
    }

    @Test
    void shouldActivateDepartment() {
        Department department = Department.builder()
                .code("05")
                .name("Antioquia")
                .enabled(false)
                .build();

        department.activate();

        assertThat(department.getEnabled()).isTrue();
        assertThat(department.isEnabled()).isTrue();
    }

    @Test
    void shouldDeactivateDepartment() {
        Department department = Department.builder()
                .code("05")
                .name("Antioquia")
                .enabled(true)
                .build();

        department.deactivate();

        assertThat(department.getEnabled()).isFalse();
        assertThat(department.isEnabled()).isFalse();
    }

    @Test
    void isEnabled_shouldReturnTrueWhenEnabledIsTrue() {
        Department department = Department.builder().enabled(true).build();
        assertThat(department.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_shouldReturnFalseWhenEnabledIsFalse() {
        Department department = Department.builder().enabled(false).build();
        assertThat(department.isEnabled()).isFalse();
    }

    @Test
    void isEnabled_shouldReturnFalseWhenEnabledIsNull() {
        Department department = Department.builder().enabled(null).build();
        assertThat(department.isEnabled()).isFalse();
    }

    @Test
    void canBeDeleted_shouldReturnTrueWhenMunicipalitiesIsNull() {
        Department department = Department.builder().municipalities(null).build();
        assertThat(department.canBeDeleted()).isTrue();
    }

    @Test
    void canBeDeleted_shouldReturnTrueWhenMunicipalitiesIsEmpty() {
        Department department = Department.builder().municipalities(Collections.emptyList()).build();
        assertThat(department.canBeDeleted()).isTrue();
    }

    @Test
    void canBeDeleted_shouldReturnFalseWhenMunicipalitiesExist() {
        List<Municipality> municipalities = new ArrayList<>();
        municipalities.add(Municipality.builder().code("05001").name("Medellín").build());
        Department department = Department.builder().municipalities(municipalities).build();
        assertThat(department.canBeDeleted()).isFalse();
    }

    @Test
    void shouldSetAndGetAuditFields() {
        LocalDateTime now = LocalDateTime.now();
        Long userId = 1L;
        Department department = Department.builder()
                .createdBy(userId).createdAt(now).updatedBy(userId).updatedAt(now).build();

        assertThat(department.getCreatedBy()).isEqualTo(userId);
        assertThat(department.getCreatedAt()).isEqualTo(now);
        assertThat(department.getUpdatedBy()).isEqualTo(userId);
        assertThat(department.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetId() {
        Department department = Department.builder().id(42L).build();
        assertThat(department.getId()).isEqualTo(42L);
    }
}
