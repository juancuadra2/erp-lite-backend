package com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MunicipalityEntityTest {

    @Test
    void onCreate_shouldSetDefaultValues() {
        // Given
        MunicipalityEntity entity = MunicipalityEntity.builder()
                .code("001")
                .name("Medellin")
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getUuid()).isNotNull();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getEnabled()).isTrue();
    }

    @Test
    void onCreate_shouldNotOverrideExistingUuid() {
        // Given
        UUID existingUuid = UUID.randomUUID();
        MunicipalityEntity entity = MunicipalityEntity.builder()
                .uuid(existingUuid)
                .code("001")
                .name("Medellin")
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getUuid()).isEqualTo(existingUuid);
    }

    @Test
    void onCreate_shouldNotOverrideExistingCreatedAt() {
        // Given
        LocalDateTime existingCreatedAt = LocalDateTime.now().minusDays(1);
        MunicipalityEntity entity = MunicipalityEntity.builder()
                .code("001")
                .name("Medellin")
                .createdAt(existingCreatedAt)
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getCreatedAt()).isEqualTo(existingCreatedAt);
    }

    @Test
    void onCreate_shouldNotOverrideExistingEnabled() {
        // Given
        MunicipalityEntity entity = MunicipalityEntity.builder()
                .code("001")
                .name("Medellin")
                .enabled(false)
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getEnabled()).isFalse();
    }

    @Test
    void onUpdate_shouldSetUpdatedAt() {
        // Given
        MunicipalityEntity entity = MunicipalityEntity.builder()
                .code("001")
                .name("Medellin")
                .build();

        // When
        entity.onUpdate();

        // Then
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void builder_shouldCreateEntityWithAllFields() {
        // Given
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DepartmentEntity department = DepartmentEntity.builder()
                .id(1L)
                .code("05")
                .name("Antioquia")
                .build();

        // When
        MunicipalityEntity entity = MunicipalityEntity.builder()
                .id(1L)
                .uuid(uuid)
                .code("001")
                .name("Medellin")
                .department(department)
                .enabled(true)
                .createdBy(100L)
                .updatedBy(200L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getCode()).isEqualTo("001");
        assertThat(entity.getName()).isEqualTo("Medellin");
        assertThat(entity.getDepartment()).isEqualTo(department);
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo(100L);
        assertThat(entity.getUpdatedBy()).isEqualTo(200L);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        // Given
        MunicipalityEntity entity = new MunicipalityEntity();
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DepartmentEntity department = DepartmentEntity.builder()
                .id(1L)
                .code("05")
                .name("Antioquia")
                .build();

        // When
        entity.setId(1L);
        entity.setUuid(uuid);
        entity.setCode("001");
        entity.setName("Medellin");
        entity.setDepartment(department);
        entity.setEnabled(true);
        entity.setCreatedBy(100L);
        entity.setUpdatedBy(200L);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getCode()).isEqualTo("001");
        assertThat(entity.getName()).isEqualTo("Medellin");
        assertThat(entity.getDepartment()).isEqualTo(department);
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo(100L);
        assertThat(entity.getUpdatedBy()).isEqualTo(200L);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }
}
