package com.jcuadrado.erplitebackend.application.port.security;

import com.jcuadrado.erplitebackend.domain.model.security.Role;

import java.util.List;
import java.util.UUID;

public interface CompareRoleUseCase {

    Role getById(UUID id);

    List<Role> listAll();
}
