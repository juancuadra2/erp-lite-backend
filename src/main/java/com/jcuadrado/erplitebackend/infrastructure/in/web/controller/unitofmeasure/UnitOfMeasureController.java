package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.unitofmeasure;

import com.jcuadrado.erplitebackend.application.port.unitofmeasure.CompareUnitsOfMeasureUseCase;
import com.jcuadrado.erplitebackend.application.port.unitofmeasure.ManageUnitOfMeasureUseCase;
import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.CreateUnitOfMeasureRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.UnitOfMeasureResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.unitofmeasure.UpdateUnitOfMeasureRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.unitofmeasure.UnitOfMeasureDtoMapper;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/units-of-measure")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Units of Measure", description = "API for managing units of measure")
public class UnitOfMeasureController {

    private final ManageUnitOfMeasureUseCase manageUseCase;
    private final CompareUnitsOfMeasureUseCase compareUseCase;
    private final UnitOfMeasureDtoMapper mapper;

    @Operation(summary = "Create unit of measure")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Unit of measure created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Duplicate name or abbreviation")
    })
    @PostMapping
    public ResponseEntity<UnitOfMeasureResponseDto> create(@Valid @RequestBody CreateUnitOfMeasureRequestDto request) {
        UnitOfMeasure created = manageUseCase.create(mapper.toDomain(request));
        URI location = URI.create("/api/v1/units-of-measure/" + created.getUuid());
        return ResponseEntity.created(location).body(mapper.toResponseDto(created));
    }

    @Operation(summary = "Get unit of measure by UUID")
    @GetMapping("/{uuid}")
    public ResponseEntity<UnitOfMeasureResponseDto> getByUuid(
            @Parameter(description = "UUID of the unit of measure", required = true)
            @PathVariable UUID uuid) {
        return ResponseEntity.ok(mapper.toResponseDto(compareUseCase.getByUuid(uuid)));
    }

    @Operation(summary = "List units of measure with filters")
    @GetMapping
    public ResponseEntity<PagedResponseDto<UnitOfMeasureResponseDto>> list(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String abbreviation,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(name = "sort", required = false, defaultValue = "name") String sortField,
            @RequestParam(name = "direction", required = false, defaultValue = "asc") String sortDirection) {

        Map<String, Object> filters = new HashMap<>();

        Boolean effectiveEnabled = enabled != null ? enabled : true;
        filters.put("enabled", effectiveEnabled);

        if (name != null && !name.isBlank()) {
            filters.put("name", name.trim());
        } else if (abbreviation != null && !abbreviation.isBlank()) {
            filters.put("abbreviation", abbreviation.trim());
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<UnitOfMeasure> domainPage = compareUseCase.findAll(filters, pageable);
        List<UnitOfMeasureResponseDto> content = domainPage.getContent().stream().map(mapper::toResponseDto).toList();

        PagedResponseDto<UnitOfMeasureResponseDto> response = PagedResponseDto.<UnitOfMeasureResponseDto>builder()
                .content(content)
                .totalElements(domainPage.getTotalElements())
                .totalPages(domainPage.getTotalPages())
                .currentPage(domainPage.getNumber())
                .pageSize(domainPage.getSize())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search units of measure by name or abbreviation")
    @GetMapping("/search")
    public ResponseEntity<List<UnitOfMeasureResponseDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String abbreviation,
            @RequestParam(required = false) Boolean enabled) {

        Boolean effectiveEnabled = enabled != null ? enabled : true;

        List<UnitOfMeasure> results;
        if (name != null && !name.isBlank()) {
            results = compareUseCase.searchByName(name.trim(), effectiveEnabled);
        } else if (abbreviation != null && !abbreviation.isBlank()) {
            results = compareUseCase.searchByAbbreviation(abbreviation.trim(), effectiveEnabled);
        } else {
            results = compareUseCase.getAllActive();
        }

        return ResponseEntity.ok(results.stream().map(mapper::toResponseDto).toList());
    }

    @Operation(summary = "Update unit of measure")
    @PutMapping("/{uuid}")
    public ResponseEntity<UnitOfMeasureResponseDto> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateUnitOfMeasureRequestDto request) {
        UnitOfMeasure updated = manageUseCase.update(uuid, mapper.toDomain(request));
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    @Operation(summary = "Deactivate unit of measure")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        manageUseCase.delete(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate unit of measure")
    @PatchMapping("/{uuid}/activate")
    public ResponseEntity<UnitOfMeasureResponseDto> activate(@PathVariable UUID uuid) {
        UnitOfMeasure activated = manageUseCase.activate(uuid);
        return ResponseEntity.ok(mapper.toResponseDto(activated));
    }

    @Operation(summary = "Deactivate unit of measure")
    @PatchMapping("/{uuid}/deactivate")
    public ResponseEntity<UnitOfMeasureResponseDto> deactivate(@PathVariable UUID uuid) {
        UnitOfMeasure deactivated = manageUseCase.deactivate(uuid);
        return ResponseEntity.ok(mapper.toResponseDto(deactivated));
    }
}
