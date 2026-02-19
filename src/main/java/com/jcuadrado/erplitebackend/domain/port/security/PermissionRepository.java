package com.jcuadrado.erplitebackend.domain.port.security;

import com.jcuadrado.erplitebackend.domain.model.security.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {

    Permission save(Permission permission);

    Optional<Permission> findById(UUID id);

    List<Permission> findByIds(List<UUID> ids);

    List<Permission> findByRoleId(UUID roleId);

    List<Permission> findByUserId(UUID userId);

    List<Permission> findAll();
}
