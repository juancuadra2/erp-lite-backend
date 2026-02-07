package com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.geography;

import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.CreateDepartmentRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.DepartmentResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.UpdateDepartmentRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DepartmentDtoMapperTest {

    private DepartmentDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DepartmentDtoMapper.class);
    }

    // ==================== toDomain(CreateDepartmentRequestDto) ====================

    @Test
    void toDomain_fromCreate_shouldMapCodeAndName() {
        CreateDepartmentRequestDto dto = CreateDepartmentRequestDto.builder()
                .code("05").name("Antioquia").build();

        Department result = mapper.toDomain(dto);

        assertThat(result.getCode()).isEqualTo("05");
        assertThat(result.getName()).isEqualTo("Antioquia");
    }

    @Test
    void toDomain_fromCreate_shouldIgnoreSystemFields() {
        CreateDepartmentRequestDto dto = CreateDepartmentRequestDto.builder()
                .code("05").name("Antioquia").build();

        Department result = mapper.toDomain(dto);

        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getEnabled()).isNull();
        assertThat(result.getMunicipalities()).isNull();
        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    // ==================== toDomain(UpdateDepartmentRequestDto) ====================

    @Test
    void toDomain_fromUpdate_shouldMapCodeAndName() {
        UpdateDepartmentRequestDto dto = UpdateDepartmentRequestDto.builder()
                .code("08").name("Atlántico").build();

        Department result = mapper.toDomain(dto);

        assertThat(result.getCode()).isEqualTo("08");
        assertThat(result.getName()).isEqualTo("Atlántico");
    }

    @Test
    void toDomain_fromUpdate_shouldIgnoreSystemFields() {
        UpdateDepartmentRequestDto dto = UpdateDepartmentRequestDto.builder()
                .code("08").name("Atlántico").build();

        Department result = mapper.toDomain(dto);

        assertThat(result.getId()).isNull();
        assertThat(result.getUuid()).isNull();
        assertThat(result.getEnabled()).isNull();
        assertThat(result.getMunicipalities()).isNull();
    }

    // ==================== toResponseDto ====================

    @Test
    void toResponseDto_shouldMapAllFields() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Department department = Department.builder()
                .id(1L).uuid(uuid).code("05").name("Antioquia").enabled(true)
                .createdBy(10L).updatedBy(20L).createdAt(now).updatedAt(now)
                .build();

        DepartmentResponseDto result = mapper.toResponseDto(department);

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
    void toResponseDto_shouldHandleDisabledDepartment() {
        Department department = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(false)
                .build();

        DepartmentResponseDto result = mapper.toResponseDto(department);

        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    void toResponseDto_shouldHandleNullAuditFields() {
        Department department = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true)
                .build();

        DepartmentResponseDto result = mapper.toResponseDto(department);

        assertThat(result.getCreatedBy()).isNull();
        assertThat(result.getUpdatedBy()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }
}
