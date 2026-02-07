package com.jcuadrado.erplitebackend.infrastructure.out.persistence;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentJpaRepository extends
        JpaRepository<DepartmentEntity, Long>,
        JpaSpecificationExecutor<DepartmentEntity> {

    Optional<DepartmentEntity> findByUuid(UUID uuid);

    Optional<DepartmentEntity> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndUuidNot(String code, UUID uuid);

    List<DepartmentEntity> findByEnabledTrue();
}
