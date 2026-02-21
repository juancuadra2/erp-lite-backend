package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.CreateRoleCommand;
import com.jcuadrado.erplitebackend.application.command.security.UpdateRoleCommand;
import com.jcuadrado.erplitebackend.domain.exception.security.RoleInUseException;
import com.jcuadrado.erplitebackend.domain.exception.security.RoleNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.security.SecurityDomainException;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.PermissionAction;
import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.domain.port.security.PermissionRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageRoleUseCaseImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    private ManageRoleUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManageRoleUseCaseImpl(roleRepository, permissionRepository, auditLogRepository);
    }

    @Test
    @DisplayName("createRole should create and save a new role when name does not exist")
    void createRole_shouldCreateAndSave_whenNameIsUnique() {
        CreateRoleCommand command = new CreateRoleCommand("MANAGER", "Manager role", List.of());
        Role savedRole = Role.create("MANAGER", "Manager role");

        when(roleRepository.existsByName("MANAGER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = useCase.createRole(command);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("MANAGER");
        verify(roleRepository).save(any(Role.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("createRole should skip permission assignment when permissionIds is null")
    void createRole_shouldSkipPermissions_whenPermissionIdsIsNull() {
        CreateRoleCommand command = new CreateRoleCommand("VIEWER", "Viewer role", null);
        Role savedRole = Role.create("VIEWER", "Viewer role");

        when(roleRepository.existsByName("VIEWER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = useCase.createRole(command);

        assertThat(result).isNotNull();
        verify(permissionRepository, never()).findByIds(any());
    }

    @Test
    @DisplayName("createRole should throw SecurityDomainException when role name already exists")
    void createRole_shouldThrow_whenNameAlreadyExists() {
        CreateRoleCommand command = new CreateRoleCommand("ADMIN", "desc", List.of());

        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> useCase.createRole(command))
                .isInstanceOf(SecurityDomainException.class)
                .hasMessageContaining("ADMIN");

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("createRole should assign permissions when permissionIds are provided")
    void createRole_shouldAssignPermissions_whenPermissionIdsProvided() {
        UUID permissionId = UUID.randomUUID();
        CreateRoleCommand command = new CreateRoleCommand("EDITOR", "Editor role", List.of(permissionId));
        Role savedRole = Role.create("EDITOR", "Editor role");
        Permission permission = Permission.create("Document", PermissionAction.READ, null, "Read docs");

        when(roleRepository.existsByName("EDITOR")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);
        when(roleRepository.findById(savedRole.getId())).thenReturn(Optional.of(savedRole));
        when(permissionRepository.findByIds(List.of(permissionId))).thenReturn(List.of(permission));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = useCase.createRole(command);

        assertThat(result).isNotNull();
        verify(permissionRepository).findByIds(List.of(permissionId));
    }

    @Test
    @DisplayName("updateRole should update the role when found and name is unique")
    void updateRole_shouldUpdate_whenFoundAndNameUnique() {
        UUID roleId = UUID.randomUUID();
        Role existing = Role.create("USER", "User role");
        UpdateRoleCommand command = new UpdateRoleCommand("EDITOR", "Editor role");
        Role updatedRole = Role.create("EDITOR", "Editor role");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByName("EDITOR")).thenReturn(false);
        when(roleRepository.save(existing)).thenReturn(updatedRole);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = useCase.updateRole(roleId, command);

        assertThat(result).isNotNull();
        verify(roleRepository).save(existing);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("updateRole should throw RoleNotFoundException when role does not exist")
    void updateRole_shouldThrow_whenRoleNotFound() {
        UUID roleId = UUID.randomUUID();
        UpdateRoleCommand command = new UpdateRoleCommand("NEW_NAME", "desc");

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.updateRole(roleId, command))
                .isInstanceOf(RoleNotFoundException.class);

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRole should throw SecurityDomainException when new name already exists for another role")
    void updateRole_shouldThrow_whenNewNameExistsForAnotherRole() {
        UUID roleId = UUID.randomUUID();
        Role existing = Role.create("USER", "User role");
        UpdateRoleCommand command = new UpdateRoleCommand("ADMIN", "Admin role");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> useCase.updateRole(roleId, command))
                .isInstanceOf(SecurityDomainException.class)
                .hasMessageContaining("ADMIN");

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRole should not check name uniqueness when the name is unchanged")
    void updateRole_shouldNotCheckNameUniqueness_whenNameUnchanged() {
        UUID roleId = UUID.randomUUID();
        Role existing = Role.create("USER", "User role");
        UpdateRoleCommand command = new UpdateRoleCommand("USER", "Updated description");
        Role savedRole = Role.create("USER", "Updated description");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existing));
        when(roleRepository.save(existing)).thenReturn(savedRole);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Role result = useCase.updateRole(roleId, command);

        assertThat(result).isNotNull();
        verify(roleRepository).save(existing);
    }

    @Test
    @DisplayName("deleteRole should complete without error when role has no assigned users")
    void deleteRole_shouldComplete_whenRoleHasNoUsers() {
        UUID roleId = UUID.randomUUID();
        Role role = Role.create("TEMP", "Temporary role");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.countUsersByRoleId(roleId)).thenReturn(0L);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.deleteRole(roleId);

        verify(roleRepository).countUsersByRoleId(roleId);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("deleteRole should throw RoleNotFoundException when role does not exist")
    void deleteRole_shouldThrow_whenRoleNotFound() {
        UUID roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deleteRole(roleId))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    @DisplayName("deleteRole should throw RoleInUseException when role has assigned users")
    void deleteRole_shouldThrow_whenRoleHasAssignedUsers() {
        UUID roleId = UUID.randomUUID();
        Role role = Role.create("ADMIN", "Admin role");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.countUsersByRoleId(roleId)).thenReturn(3L);

        assertThatThrownBy(() -> useCase.deleteRole(roleId))
                .isInstanceOf(RoleInUseException.class);
    }

    @Test
    @DisplayName("assignPermissions should throw RoleNotFoundException when role does not exist")
    void assignPermissions_shouldThrow_whenRoleNotFound() {
        UUID roleId = UUID.randomUUID();
        List<UUID> permissionIds = List.of(UUID.randomUUID());

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.assignPermissions(roleId, permissionIds))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    @DisplayName("assignPermissions should complete when role and permissions exist")
    void assignPermissions_shouldComplete_whenRoleAndPermissionsExist() {
        UUID roleId = UUID.randomUUID();
        UUID permId1 = UUID.randomUUID();
        UUID permId2 = UUID.randomUUID();
        List<UUID> permissionIds = List.of(permId1, permId2);

        Role role = Role.create("EDITOR", "Editor");
        Permission p1 = Permission.create("Document", PermissionAction.READ, null, "Read docs");
        Permission p2 = Permission.create("Document", PermissionAction.CREATE, null, "Create docs");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findByIds(permissionIds)).thenReturn(List.of(p1, p2));

        useCase.assignPermissions(roleId, permissionIds);

        verify(permissionRepository).findByIds(permissionIds);
    }

    @Test
    @DisplayName("assignPermissions should throw SecurityDomainException when some permissions are not found")
    void assignPermissions_shouldThrow_whenSomePermissionsNotFound() {
        UUID roleId = UUID.randomUUID();
        UUID permId1 = UUID.randomUUID();
        UUID permId2 = UUID.randomUUID();
        List<UUID> permissionIds = List.of(permId1, permId2);

        Role role = Role.create("EDITOR", "Editor");
        Permission p1 = Permission.create("Document", PermissionAction.READ, null, "Read docs");

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findByIds(permissionIds)).thenReturn(List.of(p1));

        assertThatThrownBy(() -> useCase.assignPermissions(roleId, permissionIds))
                .isInstanceOf(SecurityDomainException.class)
                .hasMessageContaining("permisos");
    }
}
