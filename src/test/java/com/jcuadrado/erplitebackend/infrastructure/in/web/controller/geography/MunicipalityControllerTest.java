package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.geography;

import com.jcuadrado.erplitebackend.application.port.geography.CompareMunicipalitiesUseCase;
import com.jcuadrado.erplitebackend.application.port.geography.ManageMunicipalityUseCase;
import com.jcuadrado.erplitebackend.domain.exception.geography.MunicipalityNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.geography.DuplicateMunicipalityCodeException;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.CreateMunicipalityRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.DepartmentResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.MunicipalityResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.UpdateMunicipalityRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.PagedResponseDto;
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
public class MunicipalityControllerTest {

    @Mock
    private ManageMunicipalityUseCase manageUseCase;

    @Mock
    private CompareMunicipalitiesUseCase compareUseCase;

    @Mock
    private MunicipalityDtoMapper mapper;

    @InjectMocks
    private MunicipalityController controller;

    private MunicipalityResponseDto createResponseDto(Municipality municipality) {
        DepartmentResponseDto deptDto = null;
        if (municipality.getDepartment() != null) {
            deptDto = DepartmentResponseDto.builder()
                    .id(municipality.getDepartment().getId())
                    .uuid(municipality.getDepartment().getUuid())
                    .code(municipality.getDepartment().getCode())
                    .name(municipality.getDepartment().getName())
                    .enabled(municipality.getDepartment().isEnabled())
                    .build();
        }
        return MunicipalityResponseDto.builder()
                .id(municipality.getId())
                .uuid(municipality.getUuid())
                .code(municipality.getCode())
                .name(municipality.getName())
                .department(deptDto)
                .enabled(municipality.isEnabled())
                .createdAt(municipality.getCreatedAt())
                .updatedAt(municipality.getUpdatedAt())
                .build();
    }

    private Department createSampleDepartment() {
        return Department.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05").name("Antioquia").enabled(true).build();
    }

    // ==================== create ====================

    @Test
    void create_shouldReturn201WithCreatedMunicipality() {
        Department dept = createSampleDepartment();
        CreateMunicipalityRequestDto request = CreateMunicipalityRequestDto.builder()
                .departmentId(1L).code("05001").name("Medellín").build();
        Municipality domain = Municipality.builder()
                .code("05001").name("Medellín").department(Department.builder().id(1L).build()).build();
        Municipality created = Municipality.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05001").name("Medellín")
                .department(dept).enabled(true).createdAt(LocalDateTime.now()).build();
        MunicipalityResponseDto response = createResponseDto(created);

        when(mapper.toDomain(request)).thenReturn(domain);
        when(manageUseCase.create(domain)).thenReturn(created);
        when(mapper.toResponseDto(created)).thenReturn(response);

        ResponseEntity<MunicipalityResponseDto> result = controller.create(request);

        assertEquals(201, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("05001", result.getBody().getCode());
        assertNotNull(result.getHeaders().getLocation());
    }

    @Test
    void create_shouldPropagateExceptionWhenCodeIsDuplicate() {
        CreateMunicipalityRequestDto request = CreateMunicipalityRequestDto.builder()
                .departmentId(1L).code("05001").name("Medellín").build();
        Municipality domain = Municipality.builder()
                .code("05001").name("Medellín").department(Department.builder().id(1L).build()).build();

        when(mapper.toDomain(request)).thenReturn(domain);
        when(manageUseCase.create(domain)).thenThrow(new DuplicateMunicipalityCodeException("05001"));

        assertThrows(DuplicateMunicipalityCodeException.class, () -> controller.create(request));
    }

    // ==================== getByUuid ====================

    @Test
    void getByUuid_shouldReturn200WithMunicipality() {
        UUID uuid = UUID.randomUUID();
        Department dept = createSampleDepartment();
        Municipality municipality = Municipality.builder()
                .id(1L).uuid(uuid).code("05001").name("Medellín")
                .department(dept).enabled(true).build();
        MunicipalityResponseDto response = createResponseDto(municipality);

        when(compareUseCase.getByUuid(uuid)).thenReturn(municipality);
        when(mapper.toResponseDto(municipality)).thenReturn(response);

        ResponseEntity<MunicipalityResponseDto> result = controller.getByUuid(uuid);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("05001", result.getBody().getCode());
    }

    @Test
    void getByUuid_shouldPropagateNotFoundWhenUuidDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        when(compareUseCase.getByUuid(uuid)).thenThrow(new MunicipalityNotFoundException(uuid));

        assertThrows(MunicipalityNotFoundException.class, () -> controller.getByUuid(uuid));
    }

    // ==================== list ====================

    @Test
    void list_shouldReturn200WithPagedResponse() {
        Department dept = createSampleDepartment();
        Municipality mun = Municipality.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05001").name("Medellín")
                .department(dept).enabled(true).build();
        MunicipalityResponseDto dto = createResponseDto(mun);
        Page<Municipality> page = new PageImpl<>(List.of(mun));

        when(compareUseCase.findAll(any(), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseDto(mun)).thenReturn(dto);

        ResponseEntity<PagedResponseDto<MunicipalityResponseDto>> result =
                controller.list(null, null, null, 0, 10, "id", "asc");

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
    }

    @Test
    void list_shouldPassDepartmentIdFilter() {
        Page<Municipality> emptyPage = new PageImpl<>(Collections.emptyList());
        when(compareUseCase.findAll(any(), any(Pageable.class))).thenReturn(emptyPage);

        controller.list(null, 1L, null, 0, 10, "id", "asc");

        verify(compareUseCase).findAll(any(), any(Pageable.class));
    }

    @Test
    void list_shouldPassEnabledFilter() {
        Page<Municipality> emptyPage = new PageImpl<>(Collections.emptyList());
        when(compareUseCase.findAll(any(), any(Pageable.class))).thenReturn(emptyPage);

        controller.list(true, null, null, 0, 10, "id", "asc");

        verify(compareUseCase).findAll(any(), any(Pageable.class));
    }

    @Test
    void list_shouldPassSearchFilter() {
        Page<Municipality> emptyPage = new PageImpl<>(Collections.emptyList());
        when(compareUseCase.findAll(any(), any(Pageable.class))).thenReturn(emptyPage);

        controller.list(null, null, "Medellin", 0, 10, "id", "asc");

        verify(compareUseCase).findAll(any(), any(Pageable.class));
    }

    @Test
    void list_shouldSortDescending() {
        Page<Municipality> emptyPage = new PageImpl<>(Collections.emptyList());
        when(compareUseCase.findAll(any(), any(Pageable.class))).thenReturn(emptyPage);

        controller.list(null, null, null, 0, 10, "id", "desc");

        verify(compareUseCase).findAll(any(), any(Pageable.class));
    }

    // ==================== getAllActive ====================

    @Test
    void getAllActive_shouldReturn200WithActiveList() {
        Department dept = createSampleDepartment();
        Municipality mun = Municipality.builder()
                .id(1L).uuid(UUID.randomUUID()).code("05001").name("Medellín")
                .department(dept).enabled(true).build();
        MunicipalityResponseDto dto = createResponseDto(mun);

        when(compareUseCase.getAllActive()).thenReturn(List.of(mun));
        when(mapper.toResponseDto(mun)).thenReturn(dto);

        ResponseEntity<List<MunicipalityResponseDto>> result = controller.getAllActive();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getAllActive_shouldReturnEmptyListWhenNoneActive() {
        when(compareUseCase.getAllActive()).thenReturn(Collections.emptyList());

        ResponseEntity<List<MunicipalityResponseDto>> result = controller.getAllActive();

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isEmpty());
    }

    // ==================== update ====================

    @Test
    void update_shouldReturn200WithUpdatedMunicipality() {
        UUID uuid = UUID.randomUUID();
        Department dept = createSampleDepartment();
        UpdateMunicipalityRequestDto request = UpdateMunicipalityRequestDto.builder()
                .code("05002").name("Abejorral").build();
        Municipality domain = Municipality.builder().code("05002").name("Abejorral").build();
        Municipality updated = Municipality.builder()
                .id(1L).uuid(uuid).code("05002").name("Abejorral")
                .department(dept).enabled(true).build();
        MunicipalityResponseDto response = createResponseDto(updated);

        when(mapper.toDomain(request)).thenReturn(domain);
        when(manageUseCase.update(eq(uuid), eq(domain))).thenReturn(updated);
        when(mapper.toResponseDto(updated)).thenReturn(response);

        ResponseEntity<MunicipalityResponseDto> result = controller.update(uuid, request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("05002", result.getBody().getCode());
    }

    @Test
    void update_shouldPropagateNotFoundWhenUuidDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        UpdateMunicipalityRequestDto request = UpdateMunicipalityRequestDto.builder()
                .code("05002").name("Abejorral").build();
        Municipality domain = Municipality.builder().code("05002").name("Abejorral").build();

        when(mapper.toDomain(request)).thenReturn(domain);
        when(manageUseCase.update(eq(uuid), eq(domain))).thenThrow(new MunicipalityNotFoundException(uuid));

        assertThrows(MunicipalityNotFoundException.class, () -> controller.update(uuid, request));
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
        doThrow(new MunicipalityNotFoundException(uuid)).when(manageUseCase).delete(uuid);

        assertThrows(MunicipalityNotFoundException.class, () -> controller.delete(uuid));
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
        doThrow(new MunicipalityNotFoundException(uuid)).when(manageUseCase).activate(uuid);

        assertThrows(MunicipalityNotFoundException.class, () -> controller.activate(uuid));
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
        doThrow(new MunicipalityNotFoundException(uuid)).when(manageUseCase).deactivate(uuid);

        assertThrows(MunicipalityNotFoundException.class, () -> controller.deactivate(uuid));
    }
}
