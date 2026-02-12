package com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.DepartmentEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.MunicipalityEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MunicipalityEntityMapperTest {

    private MunicipalityEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(MunicipalityEntityMapper.class);
    }

    // ==================== toEntity ====================

    @Test
    void toEntity_shouldMapAllFieldsIncludingDepartment() {
        UUID deptUuid = UUID.randomUUID();
        UUID munUuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Department department = Department.builder()
                .id(1L).uuid(deptUuid).code("05").name("Antioquia").enabled(true).build();
        Municipality municipality = Municipality.builder()
                .id(10L).uuid(munUuid).code("05001").name("Medellín")
                .department(department).enabled(true)
                .createdBy(10L).updatedBy(20L).createdAt(now).updatedAt(now)
                .build();

        MunicipalityEntity result = mapper.toEntity(municipality);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUuid()).isEqualTo(munUuid);
        assertThat(result.getCode()).isEqualTo("05001");
        assertThat(result.getName()).isEqualTo("Medellín");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(10L);
        assertThat(result.getUpdatedBy()).isEqualTo(20L);
        assertThat(result.getDepartment()).isNotNull();
        assertThat(result.getDepartment().getId()).isEqualTo(1L);
        assertThat(result.getDepartment().getCode()).isEqualTo("05");
    }

    @Test
    void toEntity_shouldIgnoreDepartmentMunicipalities() {
        Department department = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").build();
        Municipality municipality = Municipality.builder()
                .id(10L).uuid(UUID.randomUUID()).code("05001").name("Medellín")
                .department(department).enabled(true).build();

        MunicipalityEntity result = mapper.toEntity(municipality);

        assertThat(result.getDepartment().getMunicipalities()).isNull();
    }

    @Test
    void toEntity_shouldHandleNullDepartment() {
        Municipality municipality = Municipality.builder()
                .id(10L).uuid(UUID.randomUUID()).code("05001").name("Medellín")
                .department(null).enabled(true).build();

        MunicipalityEntity result = mapper.toEntity(municipality);

        assertThat(result.getDepartment()).isNull();
    }

    // ==================== toDomain ====================

    @Test
    void toDomain_shouldMapAllFieldsIncludingDepartment() {
        UUID deptUuid = UUID.randomUUID();
        UUID munUuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        DepartmentEntity deptEntity = DepartmentEntity.builder()
                .id(1L).uuid(deptUuid).code("05").name("Antioquia").enabled(true).build();
        MunicipalityEntity entity = MunicipalityEntity.builder()
                .id(10L).uuid(munUuid).code("05001").name("Medellín")
                .department(deptEntity).enabled(true)
                .createdBy(10L).updatedBy(20L).createdAt(now).updatedAt(now)
                .build();

        Municipality result = mapper.toDomain(entity);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUuid()).isEqualTo(munUuid);
        assertThat(result.getCode()).isEqualTo("05001");
        assertThat(result.getName()).isEqualTo("Medellín");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getDepartment()).isNotNull();
        assertThat(result.getDepartment().getId()).isEqualTo(1L);
        assertThat(result.getDepartment().getCode()).isEqualTo("05");
    }

    @Test
    void toDomain_shouldIgnoreDepartmentMunicipalities() {
        DepartmentEntity deptEntity = DepartmentEntity.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").build();
        MunicipalityEntity entity = MunicipalityEntity.builder()
                .id(10L).uuid(UUID.randomUUID()).code("05001").name("Medellín")
                .department(deptEntity).enabled(true).build();

        Municipality result = mapper.toDomain(entity);

        assertThat(result.getDepartment().getMunicipalities()).isNull();
    }

    // ==================== Bidirectional round-trip ====================

    @Test
    void shouldMapBidirectionallyWithoutDataLoss() {
        UUID deptUuid = UUID.randomUUID();
        UUID munUuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Department department = Department.builder()
                .id(1L).uuid(deptUuid).code("05").name("Antioquia").enabled(true).build();
        Municipality original = Municipality.builder()
                .id(10L).uuid(munUuid).code("05001").name("Medellín")
                .department(department).enabled(true)
                .createdBy(10L).updatedBy(20L).createdAt(now).updatedAt(now)
                .build();

        MunicipalityEntity entity = mapper.toEntity(original);
        Municipality roundTrip = mapper.toDomain(entity);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getUuid()).isEqualTo(original.getUuid());
        assertThat(roundTrip.getCode()).isEqualTo(original.getCode());
        assertThat(roundTrip.getName()).isEqualTo(original.getName());
        assertThat(roundTrip.getEnabled()).isEqualTo(original.getEnabled());
        assertThat(roundTrip.getCreatedBy()).isEqualTo(original.getCreatedBy());
        assertThat(roundTrip.getUpdatedBy()).isEqualTo(original.getUpdatedBy());
        assertThat(roundTrip.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(roundTrip.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
        assertThat(roundTrip.getDepartment().getId()).isEqualTo(original.getDepartment().getId());
        assertThat(roundTrip.getDepartment().getCode()).isEqualTo(original.getDepartment().getCode());
    }
}
