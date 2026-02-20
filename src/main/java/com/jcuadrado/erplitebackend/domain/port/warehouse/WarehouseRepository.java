package com.jcuadrado.erplitebackend.domain.port.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findByUuid(UUID uuid);

    Page<Warehouse> findAll(Map<String, Object> filters, Pageable pageable);

    List<Warehouse> findAllActive();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndUuidNot(String code, UUID excludeUuid);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndUuidNot(String name, UUID excludeUuid);

    boolean existsActivePrincipalWarehouse();

    boolean existsActivePrincipalWarehouseAndUuidNot(UUID excludeUuid);
}
