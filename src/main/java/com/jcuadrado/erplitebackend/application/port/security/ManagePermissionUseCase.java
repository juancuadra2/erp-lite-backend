package com.jcuadrado.erplitebackend.application.port.security;

import com.jcuadrado.erplitebackend.application.command.security.CreatePermissionCommand;
import com.jcuadrado.erplitebackend.domain.model.security.Permission;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ManagePermissionUseCase {

    Permission createPermission(CreatePermissionCommand command);

    List<Permission> listAll();

    boolean checkPermission(UUID userId, String entity, String action, Map<String, Object> context);
}
