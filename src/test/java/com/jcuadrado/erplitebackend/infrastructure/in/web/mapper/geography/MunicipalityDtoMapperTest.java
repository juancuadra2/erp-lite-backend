package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.CreateMunicipalityRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.MunicipalityResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.MunicipalitySimplifiedDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.UpdateMunicipalityRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MunicipalityDtoMapperTest {

    private MunicipalityDtoMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        mapper = Mappers.getMapper(MunicipalityDtoMapper.class);
        // MapStruct's generated impl uses DepartmentDtoMapper; Mappers.getMapper() doesn't inject it.
        DepartmentDtoMapper departmentDtoMapper = Mappers.getMapper(DepartmentDtoMapper.class);
        Field field = mapper.getClass().getDeclaredField("departmentDtoMapper");
        field.setAccessible(true);
        field.set(mapper, departmentDtoMapper);
    }

    // ==================== toDomain(CreateMunicipalityRequestDto) ====================

    @Test
    void toDomain_fromCreate_shouldMapCodeNameAndDepartment() {
        CreateMunicipalityRequestDto dto = CreateMunicipalityRequestDto.builder()
                .departmentId(1L).code("05001").name("Medellín").build();

        Municipality result = mapper.toDomain(dto);

        assertThat(result.getCode()).isEqualTo("05001");
        assertThat(result.getName()).isEqualTo("Medellín");
        assertThat(result.getDepartment()).isNotNull();
        assertThat(result.getDepartment().getId()).isEqualTo(1L);
    }

    @Test
    void toDomain_fromCreate_shouldIgnoreSystemFields() {
        CreateMunicipalityRequestDto dto = CreateMunicipalityRequestDto.builder()
                .departmentId(1L).code("05001").name("Medellín").build();

        Municipality result = mapper.toDomain(dto);

        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getEnabled()).isNull();
        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
    }

    @Test
    void toDomain_fromCreate_shouldHandleNullDepartmentId() {
        CreateMunicipalityRequestDto dto = CreateMunicipalityRequestDto.builder()
                .departmentId(null).code("05001").name("Medellín").build();

        Municipality result = mapper.toDomain(dto);

        assertThat(result.getDepartment()).isNull();
    }

    // ==================== toDomain(UpdateMunicipalityRequestDto) ====================

    @Test
    void toDomain_fromUpdate_shouldMapCodeAndName() {
        UpdateMunicipalityRequestDto dto = UpdateMunicipalityRequestDto.builder()
                .code("05002").name("Abejorral").build();

        Municipality result = mapper.toDomain(dto);

        assertThat(result.getCode()).isEqualTo("05002");
        assertThat(result.getName()).isEqualTo("Abejorral");
    }

    @Test
    void toDomain_fromUpdate_shouldIgnoreSystemFieldsAndDepartment() {
        UpdateMunicipalityRequestDto dto = UpdateMunicipalityRequestDto.builder()
                .code("05002").name("Abejorral").build();

        Municipality result = mapper.toDomain(dto);

        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getEnabled()).isNull();
        assertThat(result.getDepartment()).isNull();
    }

    // ==================== toResponseDto ====================

    @Test
    void toResponseDto_shouldMapAllFieldsIncludingDepartment() {
        UUID deptUuid = UUID.randomUUID();
        UUID munUuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Department department = Department.builder()
                .id(1L).uuid(deptUuid).code("05").name("Antioquia").enabled(true)
                .createdAt(now).updatedAt(now).build();
        Municipality municipality = Municipality.builder()
                .id(10L).uuid(munUuid).code("05001").name("Medellín")
                .department(department).enabled(true)
                .createdBy(10L).updatedBy(20L).createdAt(now).updatedAt(now)
                .build();

        MunicipalityResponseDto result = mapper.toResponseDto(municipality);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUuid()).isEqualTo(munUuid);
        assertThat(result.getCode()).isEqualTo("05001");
        assertThat(result.getName()).isEqualTo("Medellín");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getCreatedBy()).isEqualTo(10L);
        assertThat(result.getUpdatedBy()).isEqualTo(20L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
        assertThat(result.getDepartment()).isNotNull();
        assertThat(result.getDepartment().getCode()).isEqualTo("05");
        assertThat(result.getDepartment().getName()).isEqualTo("Antioquia");
    }

    @Test
    void toResponseDto_shouldHandleNullDepartment() {
        Municipality municipality = Municipality.builder()
                .id(10L).uuid(UUID.randomUUID()).code("05001").name("Medellín")
                .department(null).enabled(true).build();

        MunicipalityResponseDto result = mapper.toResponseDto(municipality);

        assertThat(result.getDepartment()).isNull();
    }

    // ==================== departmentFromId ====================

    @Test
    void departmentFromId_shouldReturnDepartmentWithIdSet() {
        Department result = mapper.departmentFromId(42L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(42L);
    }

    @Test
    void departmentFromId_shouldReturnNullWhenIdIsNull() {
        Department result = mapper.departmentFromId(null);

        assertThat(result).isNull();
    }

    @Test
    void toSimplifiedDto_shouldMapOnlyBasicFields() {
        UUID munUuid = UUID.randomUUID();
        Department department = Department.builder()
                .id(1L).code("05").name("Antioquia").enabled(true).build();
        Municipality municipality = Municipality.builder()
                .id(10L).uuid(munUuid).code("05001").name("Medellín")
                .department(department).enabled(true).build();

        MunicipalitySimplifiedDto result = mapper.toSimplifiedDto(municipality);

        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(munUuid);
        assertThat(result.getCode()).isEqualTo("05001");
        assertThat(result.getName()).isEqualTo("Medellín");
    }

    @Test
    void toSimplifiedDtoList_shouldMapAllMunicipalities() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Department department = Department.builder()
                .id(1L).code("05").name("Antioquia").enabled(true).build();

        Municipality municipality1 = Municipality.builder()
                .id(1L).uuid(uuid1).code("05001").name("Medellín")
                .department(department).enabled(true).build();
        Municipality municipality2 = Municipality.builder()
                .id(2L).uuid(uuid2).code("05002").name("Abejorral")
                .department(department).enabled(true).build();

        List<MunicipalitySimplifiedDto> result = mapper.toSimplifiedDtoList(List.of(municipality1, municipality2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUuid()).isEqualTo(uuid1);
        assertThat(result.get(0).getCode()).isEqualTo("05001");
        assertThat(result.get(0).getName()).isEqualTo("Medellín");
        assertThat(result.get(1).getUuid()).isEqualTo(uuid2);
        assertThat(result.get(1).getCode()).isEqualTo("05002");
        assertThat(result.get(1).getName()).isEqualTo("Abejorral");
    }

    @Test
    void toSimplifiedDtoList_shouldReturnEmptyListWhenInputIsEmpty() {
        List<MunicipalitySimplifiedDto> result = mapper.toSimplifiedDtoList(List.of());

        assertThat(result).isEmpty();
    }
}
