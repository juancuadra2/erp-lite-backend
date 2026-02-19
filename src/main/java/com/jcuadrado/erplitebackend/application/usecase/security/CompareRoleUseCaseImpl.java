package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.port.security.CompareRoleUseCase;
import com.jcuadrado.erplitebackend.domain.exception.security.RoleNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CompareRoleUseCaseImpl implements CompareRoleUseCase {

    private final RoleRepository roleRepository;

    @Override
    public Role getById(UUID id) {
        log.debug("Buscando rol por id: {}", id);
        return roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado: " + id));
    }

    @Override
    public List<Role> listAll() {
        log.debug("Listando todos los roles");
        return roleRepository.findAll();
    }
}
