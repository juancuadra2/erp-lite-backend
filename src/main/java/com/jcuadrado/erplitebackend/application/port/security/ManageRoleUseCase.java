package com.jcuadrado.erplitebackend.application.port.security;

import com.jcuadrado.erplitebackend.application.command.security.CreateRoleCommand;
import com.jcuadrado.erplitebackend.application.command.security.UpdateRoleCommand;
import com.jcuadrado.erplitebackend.domain.model.security.Role;

import java.util.List;
import java.util.UUID;

public interface ManageRoleUseCase {

    Role createRole(CreateRoleCommand command);

    Role updateRole(UUID id, UpdateRoleCommand command);

    void deleteRole(UUID id);

    void assignPermissions(UUID roleId, List<UUID> permissionIds);
}
