package com.jcuadrado.erplitebackend.infrastructure.out.persistence;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {

    boolean existsByName(String name);

    @Query("SELECT r FROM UserEntity u JOIN u.roles r WHERE u.id = :userId")
    List<RoleEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(u) FROM UserEntity u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") UUID roleId);
}
