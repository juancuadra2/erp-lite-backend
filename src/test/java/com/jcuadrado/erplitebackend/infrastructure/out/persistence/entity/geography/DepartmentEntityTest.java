package com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentEntityTest {

    @Test
    void onCreate_shouldSetDefaultValues() {
        // Given
        DepartmentEntity entity = DepartmentEntity.builder()
                .code("05")
                .name("Antioquia")
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
        DepartmentEntity entity = DepartmentEntity.builder()
                .uuid(existingUuid)
                .code("05")
                .name("Antioquia")
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
        DepartmentEntity entity = DepartmentEntity.builder()
                .code("05")
                .name("Antioquia")
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
        DepartmentEntity entity = DepartmentEntity.builder()
                .code("05")
                .name("Antioquia")
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
        DepartmentEntity entity = DepartmentEntity.builder()
                .code("05")
                .name("Antioquia")
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
        List<MunicipalityEntity> municipalities = new ArrayList<>();

        // When
        DepartmentEntity entity = DepartmentEntity.builder()
                .id(1L)
                .uuid(uuid)
                .code("05")
                .name("Antioquia")
                .enabled(true)
                .municipalities(municipalities)
                .createdBy(100L)
                .updatedBy(200L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getCode()).isEqualTo("05");
        assertThat(entity.getName()).isEqualTo("Antioquia");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getMunicipalities()).isEqualTo(municipalities);
        assertThat(entity.getCreatedBy()).isEqualTo(100L);
        assertThat(entity.getUpdatedBy()).isEqualTo(200L);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        // Given
        DepartmentEntity entity = new DepartmentEntity();
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<MunicipalityEntity> municipalities = new ArrayList<>();

        // When
        entity.setId(1L);
        entity.setUuid(uuid);
        entity.setCode("05");
        entity.setName("Antioquia");
        entity.setEnabled(true);
        entity.setMunicipalities(municipalities);
        entity.setCreatedBy(100L);
        entity.setUpdatedBy(200L);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getCode()).isEqualTo("05");
        assertThat(entity.getName()).isEqualTo("Antioquia");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getMunicipalities()).isEqualTo(municipalities);
        assertThat(entity.getCreatedBy()).isEqualTo(100L);
        assertThat(entity.getUpdatedBy()).isEqualTo(200L);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }
}
