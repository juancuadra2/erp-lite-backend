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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private ManageUserUseCase manageUserUseCase;

    @Mock
    private CompareUserUseCase compareUserUseCase;

    @Mock
    private UserDtoMapper mapper;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(manageUserUseCase, compareUserUseCase, mapper);
    }

    @Test
    @DisplayName("create should return 201 with the created user response DTO")
    void create_shouldReturn201() {
        UUID roleId = UUID.randomUUID();
        UUID createdUserId = UUID.randomUUID();

        CreateUserRequestDto request = new CreateUserRequestDto(
                "john_doe", "john@example.com", "Secure@1",
                "John", "Doe", null, "12345678", List.of(roleId));

        User createdUser = User.builder()
                .id(createdUserId)
                .username("john_doe")
                .email("john@example.com")
                .active(true)
                .failedAttempts(0)
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(createdUserId)
                .username("john_doe")
                .email("john@example.com")
                .active(true)
                .build();

        when(manageUserUseCase.createUser(any(CreateUserCommand.class))).thenReturn(createdUser);
        when(mapper.toResponseDto(createdUser)).thenReturn(responseDto);

        ResponseEntity<UserResponseDto> response = controller.create(request, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(createdUserId);
        assertThat(response.getBody().username()).isEqualTo("john_doe");
        verify(manageUserUseCase).createUser(any(CreateUserCommand.class));
        verify(mapper).toResponseDto(createdUser);
    }

    @Test
    @DisplayName("getById should return 200 with the found user response DTO")
    void getById_shouldReturn200() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("alice")
                .active(true)
                .failedAttempts(0)
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(userId)
                .username("alice")
                .active(true)
                .build();

        when(compareUserUseCase.getById(userId)).thenReturn(user);
        when(mapper.toResponseDto(user)).thenReturn(responseDto);

        ResponseEntity<UserResponseDto> response = controller.getById(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(userId);
        assertThat(response.getBody().username()).isEqualTo("alice");
        verify(compareUserUseCase).getById(userId);
        verify(mapper).toResponseDto(user);
    }

    @Test
    @DisplayName("list should return 200 with a paged response containing users")
    void list_shouldReturnPagedResponse() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("alice")
                .active(true)
                .failedAttempts(0)
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(userId)
                .username("alice")
                .active(true)
                .build();

        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(compareUserUseCase.list(any())).thenReturn(page);
        when(mapper.toResponseDto(user)).thenReturn(responseDto);

        ResponseEntity<PagedResponseDto<UserResponseDto>> response =
                controller.list(0, 10, "username", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).containsExactly(responseDto);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1L);
        assertThat(response.getBody().getTotalPages()).isEqualTo(1);
        assertThat(response.getBody().getCurrentPage()).isEqualTo(0);
        assertThat(response.getBody().getPageSize()).isEqualTo(10);
        verify(compareUserUseCase).list(any());
    }

    @Test
    @DisplayName("list should return 200 with DESC sort when direction is desc")
    void list_shouldReturnPagedResponse_withDescOrder() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("alice")
                .active(true)
                .failedAttempts(0)
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(userId)
                .username("alice")
                .active(true)
                .build();

        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(compareUserUseCase.list(any())).thenReturn(page);
        when(mapper.toResponseDto(user)).thenReturn(responseDto);

        ResponseEntity<PagedResponseDto<UserResponseDto>> response =
                controller.list(0, 10, "username", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).containsExactly(responseDto);
    }

    @Test
    @DisplayName("list should return 200 with an empty paged response when no users exist")
    void list_shouldReturnEmptyPagedResponse() {
        Page<User> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(compareUserUseCase.list(any())).thenReturn(emptyPage);

        ResponseEntity<PagedResponseDto<UserResponseDto>> response =
                controller.list(0, 10, "username", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("delete should return 204 and invoke soft delete on the use case")
    void delete_shouldReturn204() {
        UUID userId = UUID.randomUUID();

        doNothing().when(manageUserUseCase).deleteUser(userId);

        ResponseEntity<Void> response = controller.delete(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(manageUserUseCase).deleteUser(userId);
    }

    @Test
    @DisplayName("update should return 200 with the updated user response DTO")
    void update_shouldReturn200() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequestDto request = new UpdateUserRequestDto(
                "new@example.com", "NewFirst", "NewLast", null, null);

        User updatedUser = User.builder()
                .id(userId)
                .username("alice")
                .email("new@example.com")
                .active(true)
                .failedAttempts(0)
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(userId)
                .username("alice")
                .email("new@example.com")
                .active(true)
                .build();

        when(manageUserUseCase.updateUser(any(UUID.class), any(UpdateUserCommand.class))).thenReturn(updatedUser);
        when(mapper.toResponseDto(updatedUser)).thenReturn(responseDto);

        ResponseEntity<UserResponseDto> response = controller.update(userId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().email()).isEqualTo("new@example.com");
        verify(manageUserUseCase).updateUser(any(UUID.class), any(UpdateUserCommand.class));
    }

    @Test
    @DisplayName("unlock should return 204 and invoke unlockUser on the use case")
    void unlock_shouldReturn204() {
        UUID userId = UUID.randomUUID();

        doNothing().when(manageUserUseCase).unlockUser(userId);

        ResponseEntity<Void> response = controller.unlock(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(manageUserUseCase).unlockUser(userId);
    }

    @Test
    @DisplayName("changePassword should return 204 and invoke changePassword on the use case")
    void changePassword_shouldReturn204() {
        UUID userId = UUID.randomUUID();
        ChangePasswordRequestDto request = new ChangePasswordRequestDto("OldPass@1", "NewPass@1");

        doNothing().when(manageUserUseCase).changePassword(any(UUID.class), any(ChangePasswordCommand.class));

        ResponseEntity<Void> response = controller.changePassword(userId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(manageUserUseCase).changePassword(any(UUID.class), any(ChangePasswordCommand.class));
    }

    @Test
    @DisplayName("assignRoles should return 204 and invoke assignRoles on the use case")
    void assignRoles_shouldReturn204() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AssignRolesRequestDto request = new AssignRolesRequestDto(List.of(roleId));

        doNothing().when(manageUserUseCase).assignRoles(userId, List.of(roleId));

        ResponseEntity<Void> response = controller.assignRoles(userId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(manageUserUseCase).assignRoles(userId, List.of(roleId));
    }
}
