package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.security;

import com.jcuadrado.erplitebackend.application.command.security.ChangePasswordCommand;
import com.jcuadrado.erplitebackend.application.command.security.CreateUserCommand;
import com.jcuadrado.erplitebackend.application.command.security.UpdateUserCommand;
import com.jcuadrado.erplitebackend.application.port.security.CompareUserUseCase;
import com.jcuadrado.erplitebackend.application.port.security.ManageUserUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.AssignRolesRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.ChangePasswordRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.CreateUserRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.UpdateUserRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.UserResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security.UserDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "API for user management")
public class UserController {

    private final ManageUserUseCase manageUserUseCase;
    private final CompareUserUseCase compareUserUseCase;
    private final UserDtoMapper mapper;

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> create(
            @Valid @RequestBody CreateUserRequestDto request,
            Authentication authentication) {
        CreateUserCommand command = new CreateUserCommand(
                request.username(), request.email(), request.password(),
                request.firstName(), request.lastName(),
                request.documentTypeId(), request.documentNumber(),
                request.roleIds(), null);
        User created = manageUserUseCase.createUser(command);
        URI location = URI.create("/api/v1/users/" + created.getId());
        return ResponseEntity.created(location).body(mapper.toResponseDto(created));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponseDto(compareUserUseCase.getById(id)));
    }

    @Operation(summary = "List users with pagination")
    @GetMapping
    public ResponseEntity<PagedResponseDto<UserResponseDto>> list(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(name = "sort", defaultValue = "username") String sortField,
            @RequestParam(name = "direction", defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<User> domainPage = compareUserUseCase.list(pageable);
        List<UserResponseDto> content = domainPage.getContent().stream().map(mapper::toResponseDto).toList();

        PagedResponseDto<UserResponseDto> response = PagedResponseDto.<UserResponseDto>builder()
                .content(content)
                .totalElements(domainPage.getTotalElements())
                .totalPages(domainPage.getTotalPages())
                .currentPage(domainPage.getNumber())
                .pageSize(domainPage.getSize())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequestDto request) {
        UpdateUserCommand command = new UpdateUserCommand(
                request.email(), request.firstName(), request.lastName(),
                request.documentTypeId(), request.documentNumber(), null);
        User updated = manageUserUseCase.updateUser(id, command);
        return ResponseEntity.ok(mapper.toResponseDto(updated));
    }

    @Operation(summary = "Delete user (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        manageUserUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unlock user account")
    @PutMapping("/{id}/unlock")
    public ResponseEntity<Void> unlock(@PathVariable UUID id) {
        manageUserUseCase.unlockUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change user password")
    @PutMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        ChangePasswordCommand command = new ChangePasswordCommand(
                request.currentPassword(), request.newPassword(), null);
        manageUserUseCase.changePassword(id, command);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assign roles to user")
    @PostMapping("/{id}/roles")
    public ResponseEntity<Void> assignRoles(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRolesRequestDto request) {
        manageUserUseCase.assignRoles(id, request.roleIds());
        return ResponseEntity.noContent().build();
    }
}
