package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.geography;

import com.jcuadrado.erplitebackend.application.port.geography.CompareMunicipalitiesUseCase;
import com.jcuadrado.erplitebackend.application.port.geography.ManageMunicipalityUseCase;
import com.jcuadrado.erplitebackend.domain.model.geography.Municipality;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.CreateMunicipalityRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.MunicipalityResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.geography.UpdateMunicipalityRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.PagedResponseDto;
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
@RequestMapping("/api/geography/municipalities")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Municipalities", description = "API for managing Colombian municipalities")
public class MunicipalityController {

    private final ManageMunicipalityUseCase manageUseCase;
    private final CompareMunicipalitiesUseCase compareUseCase;
    private final MunicipalityDtoMapper mapper;

    @Operation(summary = "Create municipality", description = "Creates a new Colombian municipality with the provided DANE code, name and department")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Municipality created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Municipality code already exists in department")
    })
    @PostMapping
    public ResponseEntity<MunicipalityResponseDto> create(@Valid @RequestBody CreateMunicipalityRequestDto request) {
        log.info("Creating municipality with code: {}", request.getCode());
        Municipality domain = mapper.toDomain(request);
        Municipality created = manageUseCase.create(domain);
        MunicipalityResponseDto response = mapper.toResponseDto(created);
        log.info("Municipality created successfully with UUID: {}", created.getUuid());
        URI location = URI.create("/api/geography/municipalities/" + created.getUuid());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get municipality by UUID", description = "Retrieves a municipality by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Municipality found"),
        @ApiResponse(responseCode = "404", description = "Municipality not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<MunicipalityResponseDto> getByUuid(
            @Parameter(description = "UUID of the municipality", required = true)
            @PathVariable UUID uuid) {
        Municipality municipality = compareUseCase.getByUuid(uuid);
        return ResponseEntity.ok(mapper.toResponseDto(municipality));
    }

    @Operation(summary = "List municipalities", description = "Lists municipalities with filtering by department, searching, pagination and sorting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Municipalities retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<MunicipalityResponseDto>> list(
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Filter by department ID")
            @RequestParam(required = false) Long departmentId,
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
        if (departmentId != null) {
            filters.put("departmentId", departmentId);
        }
        if (search != null && !search.trim().isEmpty()) {
            filters.put("search", search.trim());
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<Municipality> domainPage = compareUseCase.findAll(filters, pageable);

        List<MunicipalityResponseDto> dtoList = domainPage.getContent().stream()
                .map(mapper::toResponseDto).collect(Collectors.toList());

        PagedResponseDto<MunicipalityResponseDto> response = PagedResponseDto.<MunicipalityResponseDto>builder()
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

    @Operation(summary = "Get all active municipalities", description = "Retrieves all active municipalities without pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active municipalities retrieved successfully")
    })
    @GetMapping("/active")
    public ResponseEntity<List<MunicipalityResponseDto>> getAllActive() {
        List<Municipality> active = compareUseCase.getAllActive();
        List<MunicipalityResponseDto> response = active.stream()
                .map(mapper::toResponseDto).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update municipality", description = "Updates an existing municipality")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Municipality updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Municipality not found"),
        @ApiResponse(responseCode = "409", description = "Municipality code already exists in department")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<MunicipalityResponseDto> update(
            @Parameter(description = "UUID of the municipality", required = true)
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateMunicipalityRequestDto request) {
        Municipality domain = mapper.toDomain(request);
        Municipality updated = manageUseCase.update(uuid, domain);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    @Operation(summary = "Delete municipality", description = "Deletes a municipality")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Municipality deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Municipality not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID of the municipality", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.delete(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate municipality", description = "Activates a municipality")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Municipality activated successfully"),
        @ApiResponse(responseCode = "404", description = "Municipality not found")
    })
    @PatchMapping("/{uuid}/activate")
    public ResponseEntity<Void> activate(
            @Parameter(description = "UUID of the municipality", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.activate(uuid);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deactivate municipality", description = "Deactivates a municipality")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Municipality deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Municipality not found")
    })
    @PatchMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "UUID of the municipality", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.deactivate(uuid);
        return ResponseEntity.ok().build();
    }
}
