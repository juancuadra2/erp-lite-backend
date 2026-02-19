package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.CreateRoleCommand;
import com.jcuadrado.erplitebackend.application.command.security.UpdateRoleCommand;
import com.jcuadrado.erplitebackend.application.port.security.ManageRoleUseCase;
import com.jcuadrado.erplitebackend.domain.exception.security.RoleInUseException;
import com.jcuadrado.erplitebackend.domain.exception.security.RoleNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.security.SecurityDomainException;
import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.domain.port.security.PermissionRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ManageRoleUseCaseImpl implements ManageRoleUseCase {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public Role createRole(CreateRoleCommand command) {
        log.info("Creando rol: {}", command.name());

        if (roleRepository.existsByName(command.name())) {
            throw new SecurityDomainException("Ya existe un rol con el nombre: " + command.name());
        }

        Role role = Role.create(command.name(), command.description());
        Role saved = roleRepository.save(role);

        if (command.permissionIds() != null && !command.permissionIds().isEmpty()) {
            assignPermissions(saved.getId(), command.permissionIds());
        }

        auditLogRepository.save(AuditLog.create(
                null, null, "Role", saved.getId(),
                AuditAction.ROLE_CREATED, null, null));

        log.info("Rol creado con id: {}", saved.getId());
        return saved;
    }

    @Override
    public Role updateRole(UUID id, UpdateRoleCommand command) {
        log.info("Actualizando rol: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado: " + id));

        if (!role.getName().equals(command.name()) && roleRepository.existsByName(command.name())) {
            throw new SecurityDomainException("Ya existe un rol con el nombre: " + command.name());
        }

        role.update(command.name(), command.description());
        Role saved = roleRepository.save(role);

        auditLogRepository.save(AuditLog.create(
                null, null, "Role", id,
                AuditAction.ROLE_UPDATED, null, null));

        log.info("Rol actualizado: {}", id);
        return saved;
    }

    @Override
    public void deleteRole(UUID id) {
        log.info("Eliminando rol: {}", id);

        roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado: " + id));

        long usersCount = roleRepository.countUsersByRoleId(id);
        if (usersCount > 0) {
            throw new RoleInUseException(
                    "No se puede eliminar el rol porque tiene " + usersCount + " usuarios asignados");
        }

        auditLogRepository.save(AuditLog.create(
                null, null, "Role", id,
                AuditAction.ROLE_DELETED, null, null));

        log.info("Rol eliminado: {}", id);
    }

    @Override
    public void assignPermissions(UUID roleId, List<UUID> permissionIds) {
        log.info("Asignando {} permisos al rol: {}", permissionIds.size(), roleId);

        roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado: " + roleId));

        List<Permission> permissions = permissionRepository.findByIds(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new SecurityDomainException("Uno o más permisos no encontrados");
        }

        log.info("Permisos asignados al rol: {}", roleId);
    }
}
