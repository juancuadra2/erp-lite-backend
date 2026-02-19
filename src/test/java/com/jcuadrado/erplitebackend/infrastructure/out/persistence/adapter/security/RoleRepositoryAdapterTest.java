package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.security;

import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.RoleJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.RoleEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security.RoleEntityMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryAdapterTest {

    @Mock
    private RoleJpaRepository jpaRepository;

    @Mock
    private RoleEntityMapper mapper;

    private RoleRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RoleRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("save should map to entity, delegate to JPA, and map back to domain")
    void save_shouldMapAndDelegate() {
        Role role = Role.create("ADMIN", "Administrator");
        RoleEntity entity = new RoleEntity();
        RoleEntity savedEntity = new RoleEntity();
        Role savedRole = Role.create("ADMIN", "Administrator");

        when(mapper.toEntity(role)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedRole);

        Role result = adapter.save(role);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ADMIN");
        verify(mapper).toEntity(role);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findById should return role when found")
    void findById_shouldReturnRole_whenFound() {
        UUID roleId = UUID.randomUUID();
        RoleEntity entity = new RoleEntity();
        Role role = Role.create("ADMIN", "Administrator");

        when(jpaRepository.findById(roleId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(role);

        Optional<Role> result = adapter.findById(roleId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("findById should return empty when role is not found")
    void findById_shouldReturnEmpty_whenNotFound() {
        UUID roleId = UUID.randomUUID();

        when(jpaRepository.findById(roleId)).thenReturn(Optional.empty());

        Optional<Role> result = adapter.findById(roleId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIds should return roles mapped from entities")
    void findByIds_shouldReturnRoles() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        RoleEntity e1 = new RoleEntity();
        RoleEntity e2 = new RoleEntity();
        Role r1 = Role.create("ADMIN", "Admin");
        Role r2 = Role.create("USER", "User");

        when(jpaRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(e1, e2));
        when(mapper.toDomain(e1)).thenReturn(r1);
        when(mapper.toDomain(e2)).thenReturn(r2);

        List<Role> result = adapter.findByIds(List.of(id1, id2));

        assertThat(result).hasSize(2);
        verify(jpaRepository).findAllById(List.of(id1, id2));
    }

    @Test
    @DisplayName("findAll should return all roles")
    void findAll_shouldReturnAllRoles() {
        RoleEntity entity = new RoleEntity();
        Role role = Role.create("ADMIN", "Administrator");

        when(jpaRepository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(role);

        List<Role> result = adapter.findAll();

        assertThat(result).hasSize(1);
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("findByUserId should return roles assigned to the user")
    void findByUserId_shouldReturnRoles() {
        UUID userId = UUID.randomUUID();
        RoleEntity entity = new RoleEntity();
        Role role = Role.create("USER", "User");

        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(role);

        List<Role> result = adapter.findByUserId(userId);

        assertThat(result).hasSize(1);
        verify(jpaRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("existsByName should delegate to JPA repository")
    void existsByName_shouldDelegate() {
        when(jpaRepository.existsByName("ADMIN")).thenReturn(true);

        assertThat(adapter.existsByName("ADMIN")).isTrue();
        verify(jpaRepository).existsByName("ADMIN");
    }

    @Test
    @DisplayName("countUsersByRoleId should delegate to JPA repository")
    void countUsersByRoleId_shouldDelegate() {
        UUID roleId = UUID.randomUUID();

        when(jpaRepository.countUsersByRoleId(roleId)).thenReturn(5L);

        assertThat(adapter.countUsersByRoleId(roleId)).isEqualTo(5L);
        verify(jpaRepository).countUsersByRoleId(roleId);
    }
}
