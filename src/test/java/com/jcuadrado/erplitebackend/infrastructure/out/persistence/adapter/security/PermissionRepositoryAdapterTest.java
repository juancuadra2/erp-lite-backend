package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.security;

import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.PermissionAction;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.PermissionJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.PermissionEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security.PermissionEntityMapper;
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
class PermissionRepositoryAdapterTest {

    @Mock
    private PermissionJpaRepository jpaRepository;

    @Mock
    private PermissionEntityMapper mapper;

    private PermissionRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PermissionRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("save should map to entity, delegate to JPA, and map back to domain")
    void save_shouldMapAndDelegate() {
        Permission permission = Permission.create("Invoice", PermissionAction.READ, null, "Read invoices");
        PermissionEntity entity = new PermissionEntity();
        PermissionEntity savedEntity = new PermissionEntity();
        Permission savedPermission = Permission.create("Invoice", PermissionAction.READ, null, "Read invoices");

        when(mapper.toEntity(permission)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedPermission);

        Permission result = adapter.save(permission);

        assertThat(result).isNotNull();
        assertThat(result.getEntity()).isEqualTo("Invoice");
        verify(mapper).toEntity(permission);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findById should return permission when found")
    void findById_shouldReturnPermission_whenFound() {
        UUID permId = UUID.randomUUID();
        PermissionEntity entity = new PermissionEntity();
        Permission permission = Permission.create("Invoice", PermissionAction.READ, null, "Read");

        when(jpaRepository.findById(permId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(permission);

        Optional<Permission> result = adapter.findById(permId);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findById should return empty when permission is not found")
    void findById_shouldReturnEmpty_whenNotFound() {
        UUID permId = UUID.randomUUID();

        when(jpaRepository.findById(permId)).thenReturn(Optional.empty());

        Optional<Permission> result = adapter.findById(permId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIds should return permissions mapped from entities")
    void findByIds_shouldReturnPermissions() {
        UUID id1 = UUID.randomUUID();
        PermissionEntity e1 = new PermissionEntity();
        Permission p1 = Permission.create("Invoice", PermissionAction.READ, null, "Read");

        when(jpaRepository.findAllById(List.of(id1))).thenReturn(List.of(e1));
        when(mapper.toDomain(e1)).thenReturn(p1);

        List<Permission> result = adapter.findByIds(List.of(id1));

        assertThat(result).hasSize(1);
        verify(jpaRepository).findAllById(List.of(id1));
    }

    @Test
    @DisplayName("findByUserId should return permissions for the user")
    void findByUserId_shouldReturnPermissions() {
        UUID userId = UUID.randomUUID();
        PermissionEntity entity = new PermissionEntity();
        Permission permission = Permission.create("Invoice", PermissionAction.READ, null, "Read");

        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(permission);

        List<Permission> result = adapter.findByUserId(userId);

        assertThat(result).hasSize(1);
        verify(jpaRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("findByRoleId should return permissions associated with the role")
    void findByRoleId_shouldReturnPermissions() {
        UUID roleId = UUID.randomUUID();
        PermissionEntity entity = new PermissionEntity();
        Permission permission = Permission.create("Invoice", PermissionAction.READ, null, "Read");

        when(jpaRepository.findByRoleId(roleId)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(permission);

        List<Permission> result = adapter.findByRoleId(roleId);

        assertThat(result).hasSize(1);
        verify(jpaRepository).findByRoleId(roleId);
    }

    @Test
    @DisplayName("findAll should return all permissions")
    void findAll_shouldReturnAllPermissions() {
        PermissionEntity entity = new PermissionEntity();
        Permission permission = Permission.create("Invoice", PermissionAction.READ, null, "Read");

        when(jpaRepository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(permission);

        List<Permission> result = adapter.findAll();

        assertThat(result).hasSize(1);
        verify(jpaRepository).findAll();
    }
}
