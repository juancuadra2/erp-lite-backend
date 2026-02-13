package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.geography;

import com.jcuadrado.erplitebackend.application.port.geography.CompareDepartmentsUseCase;
import com.jcuadrado.erplitebackend.application.port.geography.CompareMunicipalitiesUseCase;
import com.jcuadrado.erplitebackend.application.port.geography.ManageDepartmentUseCase;
import com.jcuadrado.erplitebackend.domain.model.geography.Department;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.CreateDepartmentRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.DepartmentResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.MunicipalitySimplifiedDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.UpdateDepartmentRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.geography.DepartmentDtoMapper;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.geography.MunicipalityDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/geography/departments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Departments", description = "API for managing Colombian departments")
public class DepartmentController {

    private final ManageDepartmentUseCase manageUseCase;
    private final CompareDepartmentsUseCase compareUseCase;
    private final CompareMunicipalitiesUseCase compareMunicipalitiesUseCase;
    private final DepartmentDtoMapper mapper;
    private final MunicipalityDtoMapper municipalityMapper;

    @Operation(summary = "Create department", description = "Creates a new Colombian department with the provided DANE code and name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Department created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Department code already exists")
    })
    @PostMapping
    public ResponseEntity<DepartmentResponseDto> create(@Valid @RequestBody CreateDepartmentRequestDto request) {
        log.info("Creating department with code: {}", request.getCode());
        Department domain = mapper.toDomain(request);
        Department created = manageUseCase.create(domain);
        DepartmentResponseDto response = mapper.toResponseDto(created);
        log.info("Department created successfully with UUID: {}", created.getUuid());
        URI location = URI.create("/api/geography/departments/" + created.getUuid());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get department by UUID", description = "Retrieves a department by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Department found"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<DepartmentResponseDto> getByUuid(
            @Parameter(description = "UUID of the department", required = true)
            @PathVariable UUID uuid) {
        Department department = compareUseCase.getByUuid(uuid);
        return ResponseEntity.ok(mapper.toResponseDto(department));
    }

    @Operation(summary = "Get department by code", description = "Retrieves a department by its DANE code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Department found"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<DepartmentResponseDto> getByCode(
            @Parameter(description = "DANE code of the department (2 digits)", required = true)
            @PathVariable String code) {
        Department department = compareUseCase.getByCode(code);
        return ResponseEntity.ok(mapper.toResponseDto(department));
    }

    @Operation(summary = "List departments", description = "Lists departments with filtering, searching, pagination and sorting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Departments retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<DepartmentResponseDto>> list(
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Global search in code and name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Items per page")
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "Sort field")
            @RequestParam(name = "sort", required = false, defaultValue = "id") String sortField,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(name = "direction", required = false, defaultValue = "asc") String sortDirection) {

        Map<String, Object> filters = new HashMap<>();
        if (enabled != null) {
            filters.put("enabled", enabled);
        }
        if (search != null && !search.trim().isEmpty()) {
            filters.put("search", search.trim());
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<Department> domainPage = compareUseCase.findAll(filters, pageable);

        List<DepartmentResponseDto> dtoList = domainPage.getContent().stream()
                .map(mapper::toResponseDto).collect(Collectors.toList());

        PagedResponseDto<DepartmentResponseDto> response = PagedResponseDto.<DepartmentResponseDto>builder()
                .content(dtoList)
                .totalElements(domainPage.getTotalElements())
                .totalPages(domainPage.getTotalPages())
                .currentPage(domainPage.getNumber())
                .pageSize(domainPage.getSize())
                .first(domainPage.isFirst())
                .last(domainPage.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all active departments", description = "Retrieves all active departments without pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active departments retrieved successfully")
    })
    @GetMapping("/active")
    public ResponseEntity<List<DepartmentResponseDto>> getAllActive() {
        List<Department> active = compareUseCase.getAllActive();
        List<DepartmentResponseDto> response = active.stream()
                .map(mapper::toResponseDto).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update department", description = "Updates an existing department")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Department updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Department code already exists")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<DepartmentResponseDto> update(
            @Parameter(description = "UUID of the department", required = true)
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateDepartmentRequestDto request) {
        Department domain = mapper.toDomain(request);
        Department updated = manageUseCase.update(uuid, domain);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    @Operation(summary = "Delete department", description = "Deletes a department if it has no municipalities")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Department has associated municipalities")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID of the department", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.delete(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate department", description = "Activates a department")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Department activated successfully"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @PatchMapping("/{uuid}/activate")
    public ResponseEntity<Void> activate(
            @Parameter(description = "UUID of the department", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.activate(uuid);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deactivate department", description = "Deactivates a department")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Department deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @PatchMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "UUID of the department", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.deactivate(uuid);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all municipalities by department", description = "Retrieves all active municipalities for a department without pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Municipalities retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/{uuid}/municipalities")
    public ResponseEntity<List<MunicipalitySimplifiedDto>> getAllMunicipalitiesByDepartment(
            @Parameter(description = "UUID of the department", required = true)
            @PathVariable UUID uuid) {
        List<Municipality> municipalities = compareMunicipalitiesUseCase.getAllByDepartment(uuid);
        List<MunicipalitySimplifiedDto> response = municipalityMapper.toSimplifiedDtoList(municipalities);
        return ResponseEntity.ok(response);
    }
}
