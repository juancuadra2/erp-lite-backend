package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.port.security.UserPermissionsUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.domain.port.security.PermissionRepository;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;

import java.util.List;

public class UserPermissionsUseCaseImpl implements UserPermissionsUseCase {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public UserPermissionsUseCaseImpl(UserRepository userRepository,
                                      PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<String> getPermissionStrings(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .map(permissionRepository::findByUserId)
                .orElse(List.of())
                .stream()
                .map(p -> p.getEntity() + ":" + p.getAction().name())
                .toList();
    }
}
