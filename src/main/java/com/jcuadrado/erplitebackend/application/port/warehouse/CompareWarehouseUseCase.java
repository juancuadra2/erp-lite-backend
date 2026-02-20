package com.jcuadrado.erplitebackend.application.port.warehouse;

import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CompareWarehouseUseCase {

    Warehouse findByUuid(UUID uuid);

    Page<Warehouse> findAll(Map<String, Object> filters, Pageable pageable);

    List<Warehouse> findAllActive();
}
