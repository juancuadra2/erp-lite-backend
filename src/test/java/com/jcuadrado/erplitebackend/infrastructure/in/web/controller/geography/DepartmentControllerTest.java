package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.geography;

import com.jcuadrado.erplitebackend.application.port.geography.CompareDepartmentsUseCase;
import com.jcuadrado.erplitebackend.application.port.geography.CompareMunicipalitiesUseCase;
import com.jcuadrado.erplitebackend.application.port.geography.ManageDepartmentUseCase;
import com.jcuadrado.erplitebackend.domain.exception.geography.DepartmentNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.geography.DuplicateDepartmentCodeException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.CreateDepartmentRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.DepartmentResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.MunicipalitySimplifiedDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.UpdateDepartmentRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.geography.DepartmentDtoMapper;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.geography.MunicipalityDtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DepartmentControllerTest {

    @Mock
    private ManageDepartmentUseCase manageUseCase;

    @Mock
    private CompareDepartmentsUseCase compareUseCase;

    @Mock
    private CompareMunicipalitiesUseCase compareMunicipalitiesUseCase;

    @Mock
    private DepartmentDtoMapper mapper;

    @Mock
    private MunicipalityDtoMapper municipalityMapper;

    @InjectMocks
    private DepartmentController controller;

    private DepartmentResponseDto createResponseDto(Department department) {
        return DepartmentResponseDto.builder()
                .id(department.getId())
                .uuid(department.getUuid())
                .code(department.getCode())
                .name(department.getName())
                .enabled(department.isEnabled())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    // ==================== create ====================

    @Test
    void create_shouldReturn201WithCreatedDepartment() {
        CreateDepartmentRequestDto request = CreateDepartmentRequestDto.builder()
                .code("05").name("Antioquia").build();
        Department domain = Department.builder().code("05").name("Antioquia").build();
        Department created = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true)
                .createdAt(LocalDateTime.now()).build();
        DepartmentResponseDto response = createResponseDto(created);

        when(mapper.toDomain(request)).thenReturn(domain);
        when(manageUseCase.create(domain)).thenReturn(created);
        when(mapper.toResponseDto(created)).thenReturn(response);

        ResponseEntity<DepartmentResponseDto> result = controller.create(request);

        assertEquals(201, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("05", result.getBody().getCode());
        assertNotNull(result.getHeaders().getLocation());
    }

    @Test
    void create_shouldPropagateExceptionWhenCodeIsDuplicate() {
        CreateDepartmentRequestDto request = CreateDepartmentRequestDto.builder()
                .code("05").name("Antioquia").build();
        Department domain = Department.builder().code("05").name("Antioquia").build();

        when(mapper.toDomain(request)).thenReturn(domain);
        when(manageUseCase.create(domain)).thenThrow(new DuplicateDepartmentCodeException("05"));

        assertThrows(DuplicateDepartmentCodeException.class, () -> controller.create(request));
    }

    // ==================== getByUuid ====================

    @Test
    void getByUuid_shouldReturn200WithDepartment() {
        UUID uuid = UUID.randomUUID();
        Department department = Department.builder()
                .id(1L).uuid(uuid).code("05").name("Antioquia").enabled(true).build();
        DepartmentResponseDto response = createResponseDto(department);

        when(compareUseCase.getByUuid(uuid)).thenReturn(department);
        when(mapper.toResponseDto(department)).thenReturn(response);

        ResponseEntity<DepartmentResponseDto> result = controller.getByUuid(uuid);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("05", result.getBody().getCode());
    }

    @Test
    void getByUuid_shouldPropagateNotFoundWhenUuidDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        when(compareUseCase.getByUuid(uuid)).thenThrow(new DepartmentNotFoundException(uuid));

        assertThrows(DepartmentNotFoundException.class, () -> controller.getByUuid(uuid));
    }

    // ==================== getByCode ====================

    @Test
    void getByCode_shouldReturn200WithDepartment() {
        Department department = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true).build();
        DepartmentResponseDto response = createResponseDto(department);

        when(compareUseCase.getByCode("05")).thenReturn(department);
        when(mapper.toResponseDto(department)).thenReturn(response);

        ResponseEntity<DepartmentResponseDto> result = controller.getByCode("05");

        assertEquals(200, result.getStatusCode().value());
        assertEquals("Antioquia", result.getBody().getName());
    }

    @Test
    void getByCode_shouldPropagateNotFoundWhenCodeDoesNotExist() {
        when(compareUseCase.getByCode("XX")).thenThrow(new DepartmentNotFoundException("XX"));

        assertThrows(DepartmentNotFoundException.class, () -> controller.getByCode("XX"));
    }

    // ==================== list ====================

    @Test
    void list_shouldReturn200WithPagedResponse() {
        Department dept = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true).build();
        DepartmentResponseDto dto = createResponseDto(dept);
        Page<Department> page = new PageImpl<>(List.of(dept));

        when(compareUseCase.findAll(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseDto(dept)).thenReturn(dto);

        ResponseEntity<PagedResponseDto<DepartmentResponseDto>> result =
                controller.list(null, null, 0, 10, "id", "asc");

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        assertEquals(1, result.getBody().getTotalElements());
    }

    @Test
    void list_shouldPassFiltersToUseCase() {
        Page<Department> emptyPage = new PageImpl<>(Collections.emptyList());
        when(compareUseCase.findAll(any(), any(Pageable.class))).thenReturn(emptyPage);

        controller.list(true, "Anti", 0, 10, "name", "desc");

        verify(compareUseCase).findAll(any(), any(Pageable.class));
    }

    @Test
    void list_shouldIgnoreEmptySearch() {
        Page<Department> emptyPage = new PageImpl<>(Collections.emptyList());
        when(compareUseCase.findAll(any(), any(Pageable.class))).thenReturn(emptyPage);

        controller.list(null, "   ", 0, 10, "id", "asc");

        verify(compareUseCase).findAll(any(), any(Pageable.class));
    }

    // ==================== getAllActive ====================

    @Test
    void getAllActive_shouldReturn200WithActiveList() {
        Department dept1 = Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true).build();
        Department dept2 = Department.builder()
                .id(2L).uuid(UUID.randomUUID()).code("08").name("Atlántico").enabled(true).build();
        DepartmentResponseDto dto1 = createResponseDto(dept1);
        DepartmentResponseDto dto2 = createResponseDto(dept2);

        when(compareUseCase.getAllActive()).thenReturn(List.of(dept1, dept2));
        when(mapper.toResponseDto(dept1)).thenReturn(dto1);
        when(mapper.toResponseDto(dept2)).thenReturn(dto2);

        ResponseEntity<List<DepartmentResponseDto>> result = controller.getAllActive();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void getAllActive_shouldReturnEmptyListWhenNoneActive() {
        when(compareUseCase.getAllActive()).thenReturn(Collections.emptyList());

        ResponseEntity<List<DepartmentResponseDto>> result = controller.getAllActive();

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isEmpty());
    }

    // ==================== update ====================

    @Test
    void update_shouldReturn200WithUpdatedDepartment() {
        UUID uuid = UUID.randomUUID();
        UpdateDepartmentRequestDto request = UpdateDepartmentRequestDto.builder()
                .code("08").name("Atlántico").build();
        Department domain = Department.builder().code("08").name("Atlántico").build();
        Department updated = Department.builder()
                .id(1L).uuid(uuid).code("08").name("Atlántico").enabled(true).build();
        DepartmentResponseDto response = createResponseDto(updated);

        when(mapper.toDomain(request)).thenReturn(domain);
        when(manageUseCase.update(eq(uuid), eq(domain))).thenReturn(updated);
        when(mapper.toResponseDto(updated)).thenReturn(response);

        ResponseEntity<DepartmentResponseDto> result = controller.update(uuid, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("08", result.getBody().getCode());
    }

    @Test
    void update_shouldPropagateNotFoundWhenUuidDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        UpdateDepartmentRequestDto request = UpdateDepartmentRequestDto.builder()
                .code("08").name("Atlántico").build();
        Department domain = Department.builder().code("08").name("Atlántico").build();

        when(mapper.toDomain(request)).thenReturn(domain);
        when(manageUseCase.update(eq(uuid), eq(domain))).thenThrow(new DepartmentNotFoundException(uuid));

        assertThrows(DepartmentNotFoundException.class, () -> controller.update(uuid, request));
    }

    // ==================== delete ====================

    @Test
    void delete_shouldReturn204() {
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).delete(uuid);

        ResponseEntity<Void> result = controller.delete(uuid);

        assertEquals(204, result.getStatusCode().value());
        verify(manageUseCase).delete(uuid);
    }

    @Test
    void delete_shouldPropagateNotFoundWhenUuidDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        doThrow(new DepartmentNotFoundException(uuid)).when(manageUseCase).delete(uuid);

        assertThrows(DepartmentNotFoundException.class, () -> controller.delete(uuid));
    }

    // ==================== activate ====================

    @Test
    void activate_shouldReturn200() {
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).activate(uuid);

        ResponseEntity<Void> result = controller.activate(uuid);

        assertEquals(200, result.getStatusCode().value());
        verify(manageUseCase).activate(uuid);
    }

    @Test
    void activate_shouldPropagateNotFoundWhenUuidDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        doThrow(new DepartmentNotFoundException(uuid)).when(manageUseCase).activate(uuid);

        assertThrows(DepartmentNotFoundException.class, () -> controller.activate(uuid));
    }

    // ==================== deactivate ====================

    @Test
    void deactivate_shouldReturn200() {
        UUID uuid = UUID.randomUUID();
        doNothing().when(manageUseCase).deactivate(uuid);

        ResponseEntity<Void> result = controller.deactivate(uuid);

        assertEquals(200, result.getStatusCode().value());
        verify(manageUseCase).deactivate(uuid);
    }

    @Test
    void deactivate_shouldPropagateNotFoundWhenUuidDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        doThrow(new DepartmentNotFoundException(uuid)).when(manageUseCase).deactivate(uuid);

        assertThrows(DepartmentNotFoundException.class, () -> controller.deactivate(uuid));
    }

    @Test
    void getAllMunicipalitiesByDepartment_shouldReturn200WithMunicipalities() {
        UUID departmentUuid = UUID.randomUUID();
        UUID munUuid1 = UUID.randomUUID();
        UUID munUuid2 = UUID.randomUUID();
        Department department = Department.builder().id(1L).code("05").name("Antioquia").build();

        Municipality mun1 = Municipality.builder()
                .id(1L).uuid(munUuid1).code("05001").name("Medell\u00edn")
                .department(department).enabled(true).build();
        Municipality mun2 = Municipality.builder()
                .id(2L).uuid(munUuid2).code("05002").name("Abejorral")
                .department(department).enabled(true).build();

        MunicipalitySimplifiedDto dto1 = MunicipalitySimplifiedDto.builder()
                .uuid(munUuid1).code("05001").name("Medell\u00edn").build();
        MunicipalitySimplifiedDto dto2 = MunicipalitySimplifiedDto.builder()
                .uuid(munUuid2).code("05002").name("Abejorral").build();

        when(compareMunicipalitiesUseCase.getAllByDepartment(departmentUuid))
                .thenReturn(List.of(mun1, mun2));
        when(municipalityMapper.toSimplifiedDtoList(List.of(mun1, mun2)))
                .thenReturn(List.of(dto1, dto2));

        ResponseEntity<List<MunicipalitySimplifiedDto>> result =
                controller.getAllMunicipalitiesByDepartment(departmentUuid);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertEquals("05001", result.getBody().get(0).getCode());
        assertEquals("Medell\u00edn", result.getBody().get(0).getName());
    }

    @Test
    void getAllMunicipalitiesByDepartment_shouldReturnEmptyListWhenNoMunicipalities() {
        UUID departmentUuid = UUID.randomUUID();
        when(compareMunicipalitiesUseCase.getAllByDepartment(departmentUuid))
                .thenReturn(Collections.emptyList());
        when(municipalityMapper.toSimplifiedDtoList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<MunicipalitySimplifiedDto>> result =
                controller.getAllMunicipalitiesByDepartment(departmentUuid);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void getAllMunicipalitiesByDepartment_shouldPropagateNotFoundWhenDepartmentDoesNotExist() {
        UUID departmentUuid = UUID.randomUUID();
        when(compareMunicipalitiesUseCase.getAllByDepartment(departmentUuid))
                .thenThrow(new DepartmentNotFoundException(departmentUuid));

        assertThrows(DepartmentNotFoundException.class,
                () -> controller.getAllMunicipalitiesByDepartment(departmentUuid));
    }
}
