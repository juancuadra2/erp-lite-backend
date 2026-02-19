package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.security;

import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.RoleJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security.RoleEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository jpaRepository;
    private final RoleEntityMapper mapper;

    @Override
    public Role save(Role role) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(role)));
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Role> findByIds(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Role> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public long countUsersByRoleId(UUID roleId) {
        return jpaRepository.countUsersByRoleId(roleId);
    }
}
