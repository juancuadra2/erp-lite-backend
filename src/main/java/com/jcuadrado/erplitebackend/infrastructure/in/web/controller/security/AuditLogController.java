package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.application.port.security.AuditLogUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.AuditLogResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security.AuditLogDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "API for audit log consultation")
public class AuditLogController {

    private final AuditLogUseCase auditLogUseCase;
    private final AuditLogDtoMapper mapper;

    @Operation(summary = "List audit logs with filters and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<PagedResponseDto<AuditLogResponseDto>> list(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(name = "sort", defaultValue = "timestamp") String sortField,
            @RequestParam(name = "direction", defaultValue = "desc") String sortDirection) {

        AuditAction auditAction = null;
        if (action != null && !action.isBlank()) {
            auditAction = AuditAction.valueOf(action.toUpperCase());
        }

        AuditLogFilter filter = new AuditLogFilter(userId, entity, auditAction, startDate, endDate);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<AuditLog> domainPage = auditLogUseCase.getAuditLogs(filter, pageable);
        List<AuditLogResponseDto> content = domainPage.getContent().stream().map(mapper::toResponseDto).toList();

        PagedResponseDto<AuditLogResponseDto> response = PagedResponseDto.<AuditLogResponseDto>builder()
                .content(content)
                .totalElements(domainPage.getTotalElements())
                .totalPages(domainPage.getTotalPages())
                .currentPage(domainPage.getNumber())
                .pageSize(domainPage.getSize())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get audit log by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit log retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Audit log not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponseDto> getById(@PathVariable UUID id) {
        return auditLogUseCase.getById(id)
                .map(mapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
