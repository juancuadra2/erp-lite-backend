package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.security;

import com.jcuadrado.erplitebackend.application.command.security.CreateRoleCommand;
import com.jcuadrado.erplitebackend.application.command.security.UpdateRoleCommand;
import com.jcuadrado.erplitebackend.application.port.security.CompareRoleUseCase;
import com.jcuadrado.erplitebackend.application.port.security.ManageRoleUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.AssignPermissionsRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.CreateRoleRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.RoleResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.UpdateRoleRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security.RoleDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Roles", description = "API for role management")
public class RoleController {

    private final ManageRoleUseCase manageRoleUseCase;
    private final CompareRoleUseCase compareRoleUseCase;
    private final RoleDtoMapper mapper;

    @Operation(summary = "Create a new role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Role name already exists")
    })
    @PostMapping
    public ResponseEntity<RoleResponseDto> create(@Valid @RequestBody CreateRoleRequestDto request) {
        CreateRoleCommand command = new CreateRoleCommand(
                request.name(), request.description(), request.permissionIds());
        Role created = manageRoleUseCase.createRole(command);
        URI location = URI.create("/api/v1/roles/" + created.getId());
        return ResponseEntity.created(location).body(mapper.toResponseDto(created));
    }

    @Operation(summary = "Get role by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponseDto(compareRoleUseCase.getById(id)));
    }

    @Operation(summary = "List all roles")
    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> listAll() {
        List<RoleResponseDto> roles = compareRoleUseCase.listAll().stream()
                .map(mapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Update role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequestDto request) {
        UpdateRoleCommand command = new UpdateRoleCommand(request.name(), request.description());
        Role updated = manageRoleUseCase.updateRole(id, command);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    @Operation(summary = "Delete role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "409", description = "Role has assigned users")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        manageRoleUseCase.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assign permissions to role")
    @PostMapping("/{id}/permissions")
    public ResponseEntity<Void> assignPermissions(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPermissionsRequestDto request) {
        manageRoleUseCase.assignPermissions(id, request.permissionIds());
        return ResponseEntity.noContent().build();
    }
}
