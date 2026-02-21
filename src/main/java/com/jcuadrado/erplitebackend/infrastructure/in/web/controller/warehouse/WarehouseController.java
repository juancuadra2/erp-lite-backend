package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.warehouse;

import com.jcuadrado.erplitebackend.application.port.warehouse.CompareWarehouseUseCase;
import com.jcuadrado.erplitebackend.application.port.warehouse.ManageWarehouseUseCase;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.CreateWarehouseRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.UpdateWarehouseRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.warehouse.WarehouseResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.warehouse.WarehouseDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController {

    private final ManageWarehouseUseCase manageUseCase;
    private final CompareWarehouseUseCase compareUseCase;
    private final WarehouseDtoMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponseDto> create(@Valid @RequestBody CreateWarehouseRequestDto request) {
        Warehouse created = manageUseCase.create(mapper.toCreateCommand(request));
        URI location = URI.create("/api/v1/warehouses/" + created.getUuid());
        return ResponseEntity.created(location).body(mapper.toResponseDto(created));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<WarehouseResponseDto> findByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(mapper.toResponseDto(compareUseCase.findByUuid(uuid)));
    }

    @GetMapping
    public ResponseEntity<PagedResponseDto<WarehouseResponseDto>> list(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID municipalityId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Map<String, Object> filters = new HashMap<>();
        if (active != null) filters.put("active", active);
        if (type != null && !type.isBlank()) filters.put("type", type.trim());
        if (municipalityId != null) filters.put("municipalityId", municipalityId);
        if (name != null && !name.isBlank()) filters.put("name", name.trim());
        if (code != null && !code.isBlank()) filters.put("code", code.trim());

        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<Warehouse> domainPage = compareUseCase.findAll(filters, pageable);
        List<WarehouseResponseDto> content = domainPage.getContent().stream()
                .map(mapper::toResponseDto)
                .toList();

        return ResponseEntity.ok(PagedResponseDto.<WarehouseResponseDto>builder()
                .content(content)
                .totalElements(domainPage.getTotalElements())
                .totalPages(domainPage.getTotalPages())
                .currentPage(domainPage.getNumber())
                .pageSize(domainPage.getSize())
                .build());
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponseDto> update(
            @PathVariable UUID uuid,
            @Valid @RequestBody UpdateWarehouseRequestDto request) {
        return ResponseEntity.ok(mapper.toResponseDto(manageUseCase.update(uuid, mapper.toUpdateCommand(request))));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        manageUseCase.delete(uuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{uuid}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponseDto> activate(@PathVariable UUID uuid) {
        return ResponseEntity.ok(mapper.toResponseDto(manageUseCase.activate(uuid)));
    }

    @PatchMapping("/{uuid}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponseDto> deactivate(@PathVariable UUID uuid) {
        return ResponseEntity.ok(mapper.toResponseDto(manageUseCase.deactivate(uuid)));
    }
}
