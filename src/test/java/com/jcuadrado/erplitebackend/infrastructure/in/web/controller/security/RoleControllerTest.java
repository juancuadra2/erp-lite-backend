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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private ManageRoleUseCase manageRoleUseCase;

    @Mock
    private CompareRoleUseCase compareRoleUseCase;

    @Mock
    private RoleDtoMapper mapper;

    private RoleController controller;

    @BeforeEach
    void setUp() {
        controller = new RoleController(manageRoleUseCase, compareRoleUseCase, mapper);
    }

    @Test
    @DisplayName("create should return 201 with the created role response DTO")
    void create_shouldReturn201() {
        UUID roleId = UUID.randomUUID();
        CreateRoleRequestDto request = new CreateRoleRequestDto("MANAGER", "Manager role", List.of());
        Role createdRole = Role.builder().id(roleId).name("MANAGER").description("Manager role").active(true).build();
        RoleResponseDto responseDto = RoleResponseDto.builder().id(roleId).name("MANAGER").active(true).build();

        when(manageRoleUseCase.createRole(any(CreateRoleCommand.class))).thenReturn(createdRole);
        when(mapper.toResponseDto(createdRole)).thenReturn(responseDto);

        ResponseEntity<RoleResponseDto> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(roleId);
        assertThat(response.getBody().name()).isEqualTo("MANAGER");
        verify(manageRoleUseCase).createRole(any(CreateRoleCommand.class));
        verify(mapper).toResponseDto(createdRole);
    }

    @Test
    @DisplayName("getById should return 200 with the found role response DTO")
    void getById_shouldReturn200() {
        UUID roleId = UUID.randomUUID();
        Role role = Role.builder().id(roleId).name("ADMIN").active(true).build();
        RoleResponseDto responseDto = RoleResponseDto.builder().id(roleId).name("ADMIN").active(true).build();

        when(compareRoleUseCase.getById(roleId)).thenReturn(role);
        when(mapper.toResponseDto(role)).thenReturn(responseDto);

        ResponseEntity<RoleResponseDto> response = controller.getById(roleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(roleId);
        verify(compareRoleUseCase).getById(roleId);
    }

    @Test
    @DisplayName("listAll should return 200 with all roles as response DTOs")
    void listAll_shouldReturn200_withAllRoles() {
        Role role1 = Role.create("ADMIN", "Admin");
        Role role2 = Role.create("USER", "User");
        RoleResponseDto dto1 = RoleResponseDto.builder().id(role1.getId()).name("ADMIN").active(true).build();
        RoleResponseDto dto2 = RoleResponseDto.builder().id(role2.getId()).name("USER").active(true).build();

        when(compareRoleUseCase.listAll()).thenReturn(List.of(role1, role2));
        when(mapper.toResponseDto(role1)).thenReturn(dto1);
        when(mapper.toResponseDto(role2)).thenReturn(dto2);

        ResponseEntity<List<RoleResponseDto>> response = controller.listAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        verify(compareRoleUseCase).listAll();
    }

    @Test
    @DisplayName("update should return 200 with the updated role response DTO")
    void update_shouldReturn200() {
        UUID roleId = UUID.randomUUID();
        UpdateRoleRequestDto request = new UpdateRoleRequestDto("EDITOR", "Editor role");
        Role updatedRole = Role.builder().id(roleId).name("EDITOR").active(true).build();
        RoleResponseDto responseDto = RoleResponseDto.builder().id(roleId).name("EDITOR").active(true).build();

        when(manageRoleUseCase.updateRole(eq(roleId), any(UpdateRoleCommand.class))).thenReturn(updatedRole);
        when(mapper.toResponseDto(updatedRole)).thenReturn(responseDto);

        ResponseEntity<RoleResponseDto> response = controller.update(roleId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("EDITOR");
        verify(manageRoleUseCase).updateRole(eq(roleId), any(UpdateRoleCommand.class));
    }

    @Test
    @DisplayName("delete should return 204 after deleting the role")
    void delete_shouldReturn204() {
        UUID roleId = UUID.randomUUID();

        doNothing().when(manageRoleUseCase).deleteRole(roleId);

        ResponseEntity<Void> response = controller.delete(roleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(manageRoleUseCase).deleteRole(roleId);
    }

    @Test
    @DisplayName("assignPermissions should return 204 after assigning permissions to the role")
    void assignPermissions_shouldReturn204() {
        UUID roleId = UUID.randomUUID();
        UUID permId = UUID.randomUUID();
        AssignPermissionsRequestDto request = new AssignPermissionsRequestDto(List.of(permId));

        doNothing().when(manageRoleUseCase).assignPermissions(roleId, List.of(permId));

        ResponseEntity<Void> response = controller.assignPermissions(roleId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(manageRoleUseCase).assignPermissions(roleId, List.of(permId));
    }
}
