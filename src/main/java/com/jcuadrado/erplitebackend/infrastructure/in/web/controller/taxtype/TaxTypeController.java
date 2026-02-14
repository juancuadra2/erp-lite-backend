package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.taxtype;

import com.jcuadrado.erplitebackend.application.port.taxtype.CompareTaxTypesUseCase;
import com.jcuadrado.erplitebackend.application.port.taxtype.ManageTaxTypeUseCase;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxApplicationType;
import com.jcuadrado.erplitebackend.domain.model.taxtype.TaxType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.CreateTaxTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.TaxTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.taxtype.UpdateTaxTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.taxtype.TaxTypeDtoMapper;
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

/**
 * TaxTypeController - REST API for managing tax types
 * 
 * Provides CRUD operations and business logic for tax types (IVA, ReteFuente, etc.).
 */
@RestController
@RequestMapping("/api/v1/tax-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tax Types", description = "API for managing tax types (IVA, ReteFuente, ReteIVA, ICA, etc.)")
public class TaxTypeController {

    private final ManageTaxTypeUseCase manageUseCase;
    private final CompareTaxTypesUseCase compareUseCase;
    private final TaxTypeDtoMapper mapper;

    @Operation(summary = "Create tax type", description = "Creates a new tax type with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tax type created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Tax type code already exists")
    })
    @PostMapping
    public ResponseEntity<TaxTypeResponseDto> create(@Valid @RequestBody CreateTaxTypeRequestDto request) {
        log.info("Creating tax type with code: {}", request.getCode());

        TaxType domain = mapper.toDomain(request);
        TaxType created = manageUseCase.create(domain);
        TaxTypeResponseDto response = mapper.toResponseDto(created);

        log.info("Tax type created successfully with UUID: {}", created.getUuid());

        URI location = URI.create("/api/v1/tax-types/" + created.getUuid());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get tax type by UUID", description = "Retrieves a tax type by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax type found"),
        @ApiResponse(responseCode = "404", description = "Tax type not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<TaxTypeResponseDto> getByUuid(
            @Parameter(description = "UUID of the tax type", required = true)
            @PathVariable UUID uuid) {
        log.debug("Retrieving tax type by UUID: {}", uuid);

        TaxType taxType = compareUseCase.getByUuid(uuid);

        log.debug("Tax type found: {}", taxType.getCode());
        return ResponseEntity.ok(mapper.toResponseDto(taxType));
    }

    @Operation(summary = "List tax types", 
               description = "Lists tax types with filtering (enabled, applicationType, name), pagination and sorting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax types retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<TaxTypeResponseDto>> list(
            @Parameter(description = "Filter by enabled status")
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Filter by application type (SALE, PURCHASE, BOTH)")
            @RequestParam(required = false) TaxApplicationType applicationType,
            @Parameter(description = "Filter by name (case-insensitive partial match)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Items per page")
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "Sort field")
            @RequestParam(name = "sort", required = false, defaultValue = "code") String sortField,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(name = "direction", required = false, defaultValue = "asc") String sortDirection) {

        log.debug("Listing tax types with filters - enabled: {}, applicationType: {}, name: {}", 
                  enabled, applicationType, name);

        Map<String, Object> filters = new HashMap<>();
        if (enabled != null) {
            filters.put("enabled", enabled);
        }
        if (applicationType != null) {
            filters.put("applicationType", applicationType);
        }
        if (name != null && !name.trim().isEmpty()) {
            filters.put("name", name.trim());
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<TaxType> domainPage = compareUseCase.findAll(filters, pageable);

        List<TaxTypeResponseDto> dtoList = domainPage.getContent().stream()
            .map(mapper::toResponseDto)
            .collect(Collectors.toList());

        PagedResponseDto<TaxTypeResponseDto> response = PagedResponseDto.<TaxTypeResponseDto>builder()
            .content(dtoList)
            .totalElements(domainPage.getTotalElements())
            .totalPages(domainPage.getTotalPages())
            .currentPage(domainPage.getNumber())
            .pageSize(domainPage.getSize())
            .build();

        log.debug("Found {} tax types", domainPage.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update tax type", description = "Updates an existing tax type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax type updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Tax type not found"),
        @ApiResponse(responseCode = "409", description = "Tax type code already exists")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<TaxTypeResponseDto> update(
            @Parameter(description = "UUID of the tax type", required = true)
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateTaxTypeRequestDto request) {
        log.info("Updating tax type: {}", uuid);

        TaxType domain = mapper.toDomain(request);
        TaxType updated = manageUseCase.update(uuid, domain);
        
        log.info("Tax type updated successfully: {}", uuid);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    @Operation(summary = "Activate tax type", description = "Activates a tax type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax type activated successfully"),
        @ApiResponse(responseCode = "404", description = "Tax type not found")
    })
    @PatchMapping("/{uuid}/activate")
    public ResponseEntity<TaxTypeResponseDto> activate(
            @Parameter(description = "UUID of the tax type", required = true)
            @PathVariable UUID uuid) {
        log.info("Activating tax type: {}", uuid);
        manageUseCase.activate(uuid);
        TaxType taxType = compareUseCase.getByUuid(uuid);
        log.info("Tax type activated successfully: {}", uuid);
        return ResponseEntity.ok(mapper.toResponseDto(taxType));
    }

    @Operation(summary = "Deactivate tax type", description = "Deactivates a tax type (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax type deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Tax type not found")
    })
    @PatchMapping("/{uuid}/deactivate")
    public ResponseEntity<TaxTypeResponseDto> deactivate(
            @Parameter(description = "UUID of the tax type", required = true)
            @PathVariable UUID uuid) {
        log.info("Deactivating tax type: {}", uuid);
        manageUseCase.deactivate(uuid);
        TaxType taxType = compareUseCase.getByUuid(uuid);
        log.info("Tax type deactivated successfully: {}", uuid);
        return ResponseEntity.ok(mapper.toResponseDto(taxType));
    }

    @Operation(summary = "Delete tax type", 
               description = "Permanently deletes a tax type (only if no products or transactions are associated)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tax type deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Tax type not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete tax type with associated products or transactions")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID of the tax type", required = true)
            @PathVariable UUID uuid) {
        log.info("Deleting tax type: {}", uuid);
        manageUseCase.delete(uuid);
        log.info("Tax type deleted successfully: {}", uuid);
        return ResponseEntity.noContent().build();
    }
}
