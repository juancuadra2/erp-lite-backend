package com.jcuadrado.erplitebackend.domain.port.security;

import com.jcuadrado.erplitebackend.domain.model.security.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findById(UUID id);

    List<Role> findByIds(List<UUID> ids);

    List<Role> findAll();

    List<Role> findByUserId(UUID userId);

    boolean existsByName(String name);

    long countUsersByRoleId(UUID roleId);
}
