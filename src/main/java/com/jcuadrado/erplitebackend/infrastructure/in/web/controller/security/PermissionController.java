package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.security;

import com.jcuadrado.erplitebackend.application.command.security.CreatePermissionCommand;
import com.jcuadrado.erplitebackend.application.port.security.ManagePermissionUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.CreatePermissionRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.PermissionResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security.PermissionDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permissions", description = "API for permission management")
public class PermissionController {

    private final ManagePermissionUseCase managePermissionUseCase;
    private final PermissionDtoMapper mapper;

    @Operation(summary = "Create a new permission")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Permission created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<PermissionResponseDto> create(@Valid @RequestBody CreatePermissionRequestDto request) {
        CreatePermissionCommand command = new CreatePermissionCommand(
                request.entity(), request.action().toUpperCase(), request.condition(), request.description());
        Permission created = managePermissionUseCase.createPermission(command);
        URI location = URI.create("/api/v1/permissions/" + created.getId());
        return ResponseEntity.created(location).body(mapper.toResponseDto(created));
    }

    @Operation(summary = "List all permissions")
    @GetMapping
    public ResponseEntity<List<PermissionResponseDto>> listAll() {
        List<PermissionResponseDto> permissions = managePermissionUseCase.listAll().stream()
                .map(mapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(permissions);
    }
}
