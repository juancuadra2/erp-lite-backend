package com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.rest;

import com.jcuadrado.erplitebackend.application.port.in.documenttype.*;
import com.jcuadrado.erplitebackend.domain.documenttype.model.DocumentType;
import com.jcuadrado.erplitebackend.infrastructure.in.api.common.dto.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.constant.DocumentTypeApiConstants;
import com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.dto.*;
import com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.mapper.DocumentTypeDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for document type operations.
 */
@RestController
@RequestMapping(DocumentTypeApiConstants.BASE_PATH)
@RequiredArgsConstructor
@Tag(name = DocumentTypeApiConstants.API_TAG, description = DocumentTypeApiConstants.API_DESCRIPTION)
public class DocumentTypeController {

    private final CreateDocumentTypeUseCase createUseCase;
    private final GetDocumentTypeUseCase getUseCase;
    private final UpdateDocumentTypeUseCase updateUseCase;
    private final ListDocumentTypesUseCase listUseCase;
    private final DeactivateDocumentTypeUseCase deactivateUseCase;
    private final DocumentTypeDtoMapper mapper;

    /**
     * Create a new document type.
     */
    @Operation(summary = DocumentTypeApiConstants.DESC_CREATE,
            description = "Creates a new document type with the provided information. Code must be unique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document type created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Document type code already exists")
    })
    @PostMapping
    public ResponseEntity<DocumentTypeResponseDto> create(@Valid @RequestBody CreateDocumentTypeRequestDto request) {
        DocumentType domain = mapper.toDomain(request);
        DocumentType created = createUseCase.create(domain);
        DocumentTypeResponseDto response = mapper.toResponseDto(created);

        // Include Location header as per specification
        URI location = URI.create("/api/document-types/" + created.getUuid());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Get document type by UUID.
     */
    @Operation(summary = DocumentTypeApiConstants.DESC_GET_BY_UUID, description = "Retrieves a document type by its UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document type found"),
            @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<DocumentTypeResponseDto> getByUuid(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid) {
        DocumentType documentType = getUseCase.getByUuid(uuid);
        return ResponseEntity.ok(mapper.toResponseDto(documentType));
    }

    /**
     * Update an existing document type.
     */
    @Operation(summary = DocumentTypeApiConstants.DESC_UPDATE,
            description = "Updates an existing document type. Only modifiable fields can be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document type updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<DocumentTypeResponseDto> update(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateDocumentTypeRequestDto request) {
        DocumentType domain = mapper.toDomain(request);
        DocumentType updated = updateUseCase.update(uuid, domain);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    /**
     * Delete a document type (soft delete).
     */
    @Operation(summary = DocumentTypeApiConstants.DESC_DELETE,
            description = "Soft deletes a document type. The record remains in database with deletedAt timestamp.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid) {
        deactivateUseCase.deactivate(uuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * List document types with advanced filters and pagination.
     *
     * @param enabled   filter by active status (optional)
     * @param search    global search in code, name, description (optional)
     * @param page      page number (0-based, default: 0)
     * @param limit     items per page (default: 10, max: 100)
     * @param sortField field to sort by (default: id)
     * @param sortOrder sort order ASC or DESC (default: ASC)
     * @param filters   additional dynamic filters as query params (optional)
     * @return paged response with document types
     */
    @Operation(summary = DocumentTypeApiConstants.DESC_LIST, description = "Lists document types with advanced filtering, searching, pagination and sorting capabilities.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Document types retrieved successfully")})
    @GetMapping
    public ResponseEntity<PagedResponseDto<DocumentTypeResponseDto>> list(
            @Parameter(description = "Filter by active status (true/false)") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Global search in code, name, and description") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Items per page (max: 100)") @RequestParam(required = false, defaultValue = "10") Integer limit,
            @Parameter(description = "Field to sort by") @RequestParam(name = "sort.field", required = false, defaultValue = "id") String sortField,
            @Parameter(description = "Sort order (ASC/DESC)") @RequestParam(name = "sort.order", required = false, defaultValue = "ASC") String sortOrder,
            @Parameter(description = "Additional dynamic filters") @RequestParam(required = false) Map<String, Object> filters) {

        // Validate limit does not exceed maximum
        if (limit > 100) {
            throw new IllegalArgumentException("Limit must not exceed 100");
        }

        Sort.Direction direction = Sort.Direction.fromString(sortOrder);
        Pageable pageable = PageRequest.of(page, limit, Sort.by(direction, sortField));

        Map<String, Object> cleanFilters = new HashMap<>();
        if (filters != null) {
            filters.forEach((key, value) -> {
                if (!key.equals("enabled") && !key.equals("search") && !key.equals("page")
                        && !key.equals("limit") && !key.startsWith("sort.")) {
                    cleanFilters.put(key, value);
                }
            });
        }

        Page<DocumentType> documentTypes = listUseCase.listWithFilters(enabled, search, cleanFilters, pageable);

        Page<DocumentTypeResponseDto> dtoPage = documentTypes.map(mapper::toResponseDto);

        PagedResponseDto<DocumentTypeResponseDto> response = PagedResponseDto.<DocumentTypeResponseDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .currentPage(dtoPage.getNumber())
                .pageSize(dtoPage.getSize())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a document type.
     */
    @Operation(summary = DocumentTypeApiConstants.DESC_DEACTIVATE,
            description = "Deactivates a document type by setting active flag to false.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document type deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @PatchMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid) {
        deactivateUseCase.deactivate(uuid);
        return ResponseEntity.ok().build();
    }

    /**
     * Activate a document type.
     */
    @Operation(summary = DocumentTypeApiConstants.DESC_ACTIVATE,
            description = "Activates a document type by setting active flag to true.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document type activated successfully"),
            @ApiResponse(responseCode = "404", description = "Document type not found")
    })
    @PatchMapping("/{uuid}/activate")
    public ResponseEntity<Void> activate(
            @Parameter(description = "UUID of the document type", required = true)
            @PathVariable UUID uuid) {
        deactivateUseCase.activate(uuid);
        return ResponseEntity.ok().build();
    }
}
