package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.documenttypes;

import com.jcuadrado.erplitebackend.application.port.documenttypes.CompareDocumentTypesUseCase;
import com.jcuadrado.erplitebackend.application.port.documenttypes.ManageDocumentTypeUseCase;
import com.jcuadrado.erplitebackend.domain.model.documenttypes.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.CreateDocumentTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.DocumentTypeResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.documenttypes.UpdateDocumentTypeRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.documenttypes.DocumentTypeDtoMapper;
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
@RequestMapping("/api/v1/document-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Types", description = "API for managing document types (NIT, CC, CE, Passport, etc.)")
public class DocumentTypeController {

    private final ManageDocumentTypeUseCase manageUseCase;
    private final CompareDocumentTypesUseCase compareUseCase;
    private final DocumentTypeDtoMapper mapper;

    @Operation(summary = "Create document type", description = "Creates a new document type with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Document type created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Document type code already exists")
    })
    @PostMapping
    public ResponseEntity<DocumentTypeResponseDto> create(@Valid @RequestBody CreateDocumentTypeRequestDto request) {
        log.info("Creating document type with code: {}", request.getCode());

        DocumentType domain = mapper.toDomain(request);
        DocumentType created = manageUseCase.create(domain);
        DocumentTypeResponseDto response = mapper.toResponseDto(created);

        log.info("Document type created successfully with UUID: {}", created.getUuid());

        URI location = URI.create("/api/document-types/" + created.getUuid());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get document type by UUID", description = "Retrieves a document type by its UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document type found"),
        @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<DocumentTypeResponseDto> getByUuid(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid) {
        log.debug("Retrieving document type by UUID: {}", uuid);

        DocumentType documentType = compareUseCase.getByUuid(uuid);

        log.debug("Document type found: {}", documentType.getCode());
        return ResponseEntity.ok(mapper.toResponseDto(documentType));
    }

    @Operation(summary = "Get document type by code", description = "Retrieves a document type by its code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document type found"),
        @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<DocumentTypeResponseDto> getByCode(
            @Parameter(description = "Code of the document type", required = true)
            @PathVariable String code) {
        DocumentType documentType = compareUseCase.getByCode(code);
        return ResponseEntity.ok(mapper.toResponseDto(documentType));
    }

    @Operation(summary = "List document types", description = "Lists document types with filtering, searching, pagination and sorting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document types retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<DocumentTypeResponseDto>> list(
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Global search in code, name, and description")
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

        Page<DocumentType> domainPage = compareUseCase.findAll(filters, pageable);

        List<DocumentTypeResponseDto> dtoList = domainPage.getContent().stream()
            .map(mapper::toResponseDto)
            .collect(Collectors.toList());

        PagedResponseDto<DocumentTypeResponseDto> response = PagedResponseDto.<DocumentTypeResponseDto>builder()
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

    @Operation(summary = "Get all active document types", description = "Retrieves all active document types without pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active document types retrieved successfully")
    })
    @GetMapping("/active")
    public ResponseEntity<List<DocumentTypeResponseDto>> getAllActive() {
        List<DocumentType> activeTypes = compareUseCase.getAllActive();
        List<DocumentTypeResponseDto> response = activeTypes.stream()
            .map(mapper::toResponseDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update document type", description = "Updates an existing document type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document type updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Document type not found"),
        @ApiResponse(responseCode = "409", description = "Document type code already exists")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<DocumentTypeResponseDto> update(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateDocumentTypeRequestDto request) {
        DocumentType domain = mapper.toDomain(request);
        DocumentType updated = manageUseCase.update(uuid, domain);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    @Operation(summary = "Delete document type", description = "Soft deletes a document type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Document type deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.delete(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate document type", description = "Activates a document type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document type activated successfully"),
        @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @PatchMapping("/{uuid}/activate")
    public ResponseEntity<Void> activate(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.activate(uuid);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deactivate document type", description = "Deactivates a document type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document type deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @PatchMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid) {
        manageUseCase.deactivate(uuid);
        return ResponseEntity.ok().build();
    }
}

