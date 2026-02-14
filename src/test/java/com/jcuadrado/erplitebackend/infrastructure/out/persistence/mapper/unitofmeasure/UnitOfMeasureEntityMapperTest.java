package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.unitofmeasure.UnitOfMeasureEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnitOfMeasureEntityMapperTest {

    private final UnitOfMeasureEntityMapper mapper = Mappers.getMapper(UnitOfMeasureEntityMapper.class);

    @Test
    void toEntity_shouldMapAllFields() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UnitOfMeasure model = UnitOfMeasure.builder()
                .id(1L)
                .uuid(uuid)
                .name("Kilogramo")
                .abbreviation("KG")
                .enabled(true)
                .createdBy(10L)
                .updatedBy(11L)
                .deletedBy(12L)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(now)
                .build();

        UnitOfMeasureEntity entity = mapper.toEntity(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUuid()).isEqualTo(uuid);
        assertThat(entity.getName()).isEqualTo("Kilogramo");
        assertThat(entity.getAbbreviation()).isEqualTo("KG");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getCreatedBy()).isEqualTo(10L);
        assertThat(entity.getUpdatedBy()).isEqualTo(11L);
        assertThat(entity.getDeletedBy()).isEqualTo(12L);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getDeletedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldMapAllFields() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UnitOfMeasureEntity entity = UnitOfMeasureEntity.builder()
                .id(2L)
                .uuid(uuid)
                .name("Metro")
                .abbreviation("M")
                .enabled(false)
                .createdBy(20L)
                .updatedBy(21L)
                .deletedBy(22L)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(now)
                .build();

        UnitOfMeasure model = mapper.toDomain(entity);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(2L);
        assertThat(model.getUuid()).isEqualTo(uuid);
        assertThat(model.getName()).isEqualTo("Metro");
        assertThat(model.getAbbreviation()).isEqualTo("M");
        assertThat(model.getEnabled()).isFalse();
        assertThat(model.getCreatedBy()).isEqualTo(20L);
        assertThat(model.getUpdatedBy()).isEqualTo(21L);
        assertThat(model.getDeletedBy()).isEqualTo(22L);
        assertThat(model.getCreatedAt()).isEqualTo(now);
        assertThat(model.getUpdatedAt()).isEqualTo(now);
        assertThat(model.getDeletedAt()).isEqualTo(now);
    }

    @Test
    void mapper_shouldReturnNullWhenInputIsNull() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
    }
}
