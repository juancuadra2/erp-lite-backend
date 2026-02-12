package com.jcuadrado.erplitebackend.infrastructure.out.persistence;

import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.geography.MunicipalityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MunicipalityJpaRepository extends
        JpaRepository<MunicipalityEntity, Long>,
        JpaSpecificationExecutor<MunicipalityEntity> {

    Optional<MunicipalityEntity> findByUuid(UUID uuid);

    Optional<MunicipalityEntity> findByCodeAndDepartmentId(String code, Long departmentId);

    boolean existsByCodeAndDepartmentId(String code, Long departmentId);

    boolean existsByCodeAndDepartmentIdAndUuidNot(String code, Long departmentId, UUID uuid);

    List<MunicipalityEntity> findByEnabledTrue();

    long countByDepartmentId(Long departmentId);
}
