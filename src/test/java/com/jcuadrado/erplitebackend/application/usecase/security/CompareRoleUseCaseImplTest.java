package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.domain.exception.security.RoleNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompareRoleUseCaseImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private CompareRoleUseCaseImpl useCase;

    @Test
    @DisplayName("getById should return the role when found")
    void getById_shouldReturnRole_whenFound() {
        UUID roleId = UUID.randomUUID();
        Role role = Role.create("ADMIN", "Administrator role");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        Role result = useCase.getById(roleId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ADMIN");
        verify(roleRepository).findById(roleId);
    }

    @Test
    @DisplayName("getById should throw RoleNotFoundException when role is not found")
    void getById_shouldThrowRoleNotFoundException_whenNotFound() {
        UUID roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(roleId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining(roleId.toString());

        verify(roleRepository).findById(roleId);
    }

    @Test
    @DisplayName("listAll should return all roles from the repository")
    void listAll_shouldReturnAllRoles() {
        Role role1 = Role.create("ADMIN", "Administrator");
        Role role2 = Role.create("USER", "Standard user");

        when(roleRepository.findAll()).thenReturn(List.of(role1, role2));

        List<Role> result = useCase.listAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(role1, role2);
        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("listAll should return an empty list when no roles exist")
    void listAll_shouldReturnEmptyList_whenNoRolesExist() {
        when(roleRepository.findAll()).thenReturn(List.of());

        List<Role> result = useCase.listAll();

        assertThat(result).isEmpty();
        verify(roleRepository).findAll();
    }
}
