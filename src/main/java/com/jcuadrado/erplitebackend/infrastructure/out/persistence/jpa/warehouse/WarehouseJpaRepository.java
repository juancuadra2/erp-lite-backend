package com.jcuadrado.erplitebackend.infrastructure.out.persistence.jpa.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.warehouse.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface WarehouseJpaRepository
        extends JpaRepository<WarehouseEntity, Long>, JpaSpecificationExecutor<WarehouseEntity> {

    Optional<WarehouseEntity> findByUuidAndDeletedAtIsNull(String uuid);

    List<WarehouseEntity> findByActiveTrueAndDeletedAtIsNull();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndUuidNot(String code, String uuid);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndUuidNot(String name, String uuid);

    boolean existsByTypeAndActiveTrueAndDeletedAtIsNull(WarehouseType type);

    boolean existsByTypeAndActiveTrueAndDeletedAtIsNullAndUuidNot(WarehouseType type, String uuid);
}
