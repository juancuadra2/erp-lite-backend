package com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.unitofmeasure;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnitOfMeasureEntityTest {

    @Test
    void onCreate_shouldSetDefaults() {
        UnitOfMeasureEntity entity = UnitOfMeasureEntity.builder().name("Caja").abbreviation("CJ").build();

        entity.onCreate();

        assertThat(entity.getUuid()).isNotNull();
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void onCreate_shouldKeepExistingValues() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        UnitOfMeasureEntity entity = UnitOfMeasureEntity.builder()
                .uuid(uuid)
                .enabled(false)
                .createdAt(createdAt)
                .name("Caja")
                .abbreviation("CJ")
                .build();

        entity.onCreate();

        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getEnabled()).isFalse();
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void onUpdate_shouldSetUpdatedAt() {
        UnitOfMeasureEntity entity = UnitOfMeasureEntity.builder().name("Caja").abbreviation("CJ").build();

        entity.onUpdate();

        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void gettersSettersAndBuilder_shouldWork() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        UnitOfMeasureEntity entity = UnitOfMeasureEntity.builder()
                .id(1L)
                .uuid(uuid)
                .name("Kilogramo")
                .abbreviation("KG")
                .enabled(true)
                .createdBy(1L)
                .updatedBy(2L)
                .deletedBy(3L)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(now)
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getName()).isEqualTo("Kilogramo");
        assertThat(entity.getAbbreviation()).isEqualTo("KG");
    }
}
