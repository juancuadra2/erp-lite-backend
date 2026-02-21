package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.security;

import com.jcuadrado.erplitebackend.application.command.security.CreatePermissionCommand;
import com.jcuadrado.erplitebackend.application.port.security.ManagePermissionUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.PermissionAction;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.CreatePermissionRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.PermissionResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security.PermissionDtoMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionControllerTest {

    @Mock
    private ManagePermissionUseCase managePermissionUseCase;

    @Mock
    private PermissionDtoMapper mapper;

    private PermissionController controller;

    @BeforeEach
    void setUp() {
        controller = new PermissionController(managePermissionUseCase, mapper);
    }

    @Test
    @DisplayName("create should return 201 with the created permission response DTO")
    void create_shouldReturn201() {
        UUID permId = UUID.randomUUID();
        CreatePermissionRequestDto request = new CreatePermissionRequestDto("Invoice", "READ", null, "Read invoices");
        Permission created = Permission.create("Invoice", PermissionAction.READ, null, "Read invoices");
        PermissionResponseDto responseDto = PermissionResponseDto.builder()
                .id(permId).entity("Invoice").action("READ").build();

        when(managePermissionUseCase.createPermission(any(CreatePermissionCommand.class))).thenReturn(created);
        when(mapper.toResponseDto(created)).thenReturn(responseDto);

        ResponseEntity<PermissionResponseDto> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().entity()).isEqualTo("Invoice");
        assertThat(response.getBody().action()).isEqualTo("READ");
        verify(managePermissionUseCase).createPermission(any(CreatePermissionCommand.class));
        verify(mapper).toResponseDto(created);
    }

    @Test
    @DisplayName("listAll should return 200 with all permissions as response DTOs")
    void listAll_shouldReturn200_withAllPermissions() {
        Permission p1 = Permission.create("Invoice", PermissionAction.READ, null, "Read");
        Permission p2 = Permission.create("Invoice", PermissionAction.CREATE, null, "Create");
        PermissionResponseDto dto1 = PermissionResponseDto.builder().id(p1.getId()).entity("Invoice").action("READ").build();
        PermissionResponseDto dto2 = PermissionResponseDto.builder().id(p2.getId()).entity("Invoice").action("CREATE").build();

        when(managePermissionUseCase.listAll()).thenReturn(List.of(p1, p2));
        when(mapper.toResponseDto(p1)).thenReturn(dto1);
        when(mapper.toResponseDto(p2)).thenReturn(dto2);

        ResponseEntity<List<PermissionResponseDto>> response = controller.listAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        verify(managePermissionUseCase).listAll();
    }
}
