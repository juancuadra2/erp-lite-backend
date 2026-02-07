package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.DepartmentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentEntityMapperTest {

    private DepartmentEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DepartmentEntityMapper.class);
    }

    // ==================== toEntity ====================

    @Test
    void toEntity_shouldMapAllFields() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Department department = Department.builder()
                .id(1L).uuid(uuid).code("05").name("Antioquia").enabled(true)
                .createdBy(10L).updatedBy(20L).createdAt(now).updatedAt(now)
                .build();

        DepartmentEntity result = mapper.toEntity(department);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getCode()).isEqualTo("05");
        assertThat(result.getName()).isEqualTo("Antioquia");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(10L);
        assertThat(result.getUpdatedBy()).isEqualTo(20L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_shouldIgnoreMunicipalities() {
        Department department = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true)
                .build();

        DepartmentEntity result = mapper.toEntity(department);

        assertThat(result.getMunicipalities()).isNull();
    }

    // ==================== toDomain ====================

    @Test
    void toDomain_shouldMapAllFields() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DepartmentEntity entity = DepartmentEntity.builder()
                .id(1L).uuid(uuid).code("05").name("Antioquia").enabled(true)
                .createdBy(10L).updatedBy(20L).createdAt(now).updatedAt(now)
                .build();

        Department result = mapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUuid()).isEqualTo(uuid);
        assertThat(result.getCode()).isEqualTo("05");
        assertThat(result.getName()).isEqualTo("Antioquia");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(10L);
        assertThat(result.getUpdatedBy()).isEqualTo(20L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldIgnoreMunicipalities() {
        DepartmentEntity entity = DepartmentEntity.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true)
                .build();

        Department result = mapper.toDomain(entity);

        assertThat(result.getMunicipalities()).isNull();
    }

    // ==================== Bidirectional round-trip ====================

    @Test
    void shouldMapBidirectionallyWithoutDataLoss() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Department original = Department.builder()
                .id(1L).uuid(uuid).code("05").name("Antioquia").enabled(true)
                .createdBy(10L).updatedBy(20L).createdAt(now).updatedAt(now)
                .build();

        DepartmentEntity entity = mapper.toEntity(original);
        Department roundTrip = mapper.toDomain(entity);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getUuid()).isEqualTo(original.getUuid());
        assertThat(roundTrip.getCode()).isEqualTo(original.getCode());
        assertThat(roundTrip.getName()).isEqualTo(original.getName());
        assertThat(roundTrip.getEnabled()).isEqualTo(original.getEnabled());
        assertThat(roundTrip.getCreatedBy()).isEqualTo(original.getCreatedBy());
        assertThat(roundTrip.getUpdatedBy()).isEqualTo(original.getUpdatedBy());
        assertThat(roundTrip.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(roundTrip.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
    }

    @Test
    void shouldHandleDisabledDepartment() {
        Department department = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(false)
                .build();

        DepartmentEntity entity = mapper.toEntity(department);
        assertThat(entity.getEnabled()).isFalse();

        Department domain = mapper.toDomain(entity);
        assertThat(domain.getEnabled()).isFalse();
    }
}
