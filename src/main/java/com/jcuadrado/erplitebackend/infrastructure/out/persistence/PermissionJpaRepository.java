package com.jcuadrado.erplitebackend.infrastructure.out.persistence;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, UUID> {

    @Query("SELECT p FROM RoleEntity r JOIN r.permissions p WHERE r.id = :roleId")
    List<PermissionEntity> findByRoleId(@Param("roleId") UUID roleId);

    @Query("SELECT DISTINCT p FROM UserEntity u JOIN u.roles r JOIN r.permissions p WHERE u.id = :userId")
    List<PermissionEntity> findByUserId(@Param("userId") UUID userId);
}
