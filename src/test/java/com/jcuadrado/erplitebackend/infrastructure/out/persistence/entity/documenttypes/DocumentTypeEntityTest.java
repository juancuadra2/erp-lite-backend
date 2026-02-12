package com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.documenttypes;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTypeEntityTest {

    @Test
    void onCreate_shouldSetDefaultValues() {
        // Given
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .code("CC")
                .name("Cedula de Ciudadania")
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getUuid()).isNotNull();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getActive()).isTrue();
    }

    @Test
    void onCreate_shouldNotOverrideExistingUuid() {
        // Given
        UUID existingUuid = UUID.randomUUID();
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .uuid(existingUuid)
                .code("CC")
                .name("Cedula de Ciudadania")
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
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .code("CC")
                .name("Cedula de Ciudadania")
                .createdAt(existingCreatedAt)
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getCreatedAt()).isEqualTo(existingCreatedAt);
    }

    @Test
    void onCreate_shouldNotOverrideExistingActive() {
        // Given
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .code("CC")
                .name("Cedula de Ciudadania")
                .active(false)
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getActive()).isFalse();
    }

    @Test
    void onUpdate_shouldSetUpdatedAt() {
        // Given
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .code("CC")
                .name("Cedula de Ciudadania")
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

        // When
        DocumentTypeEntity entity = DocumentTypeEntity.builder()
                .id(1L)
                .uuid(uuid)
                .code("CC")
                .name("Cedula de Ciudadania")
                .description("Documento de identidad")
                .active(true)
                .createdBy(100L)
                .updatedBy(200L)
                .deletedBy(300L)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(now)
                .build();

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getCode()).isEqualTo("CC");
        assertThat(entity.getName()).isEqualTo("Cedula de Ciudadania");
        assertThat(entity.getDescription()).isEqualTo("Documento de identidad");
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo(100L);
        assertThat(entity.getUpdatedBy()).isEqualTo(200L);
        assertThat(entity.getDeletedBy()).isEqualTo(300L);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeletedAt()).isEqualTo(now);
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        // Given
        DocumentTypeEntity entity = new DocumentTypeEntity();
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        entity.setId(1L);
        entity.setUuid(uuid);
        entity.setCode("CC");
        entity.setName("Cedula de Ciudadania");
        entity.setDescription("Documento de identidad");
        entity.setActive(true);
        entity.setCreatedBy(100L);
        entity.setUpdatedBy(200L);
        entity.setDeletedBy(300L);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedAt(now);

        // Then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getCode()).isEqualTo("CC");
        assertThat(entity.getName()).isEqualTo("Cedula de Ciudadania");
        assertThat(entity.getDescription()).isEqualTo("Documento de identidad");
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo(100L);
        assertThat(entity.getUpdatedBy()).isEqualTo(200L);
        assertThat(entity.getDeletedBy()).isEqualTo(300L);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeletedAt()).isEqualTo(now);
    }
}
