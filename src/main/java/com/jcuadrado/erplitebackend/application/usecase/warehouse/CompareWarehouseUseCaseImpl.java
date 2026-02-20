package com.jcuadrado.erplitebackend.application.usecase.warehouse;

import com.jcuadrado.erplitebackend.application.port.warehouse.CompareWarehouseUseCase;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.WarehouseNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.port.warehouse.WarehouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CompareWarehouseUseCaseImpl implements CompareWarehouseUseCase {

    private final WarehouseRepository repository;

    public CompareWarehouseUseCaseImpl(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Warehouse findByUuid(UUID uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new WarehouseNotFoundException(uuid));
    }

    @Override
    public Page<Warehouse> findAll(Map<String, Object> filters, Pageable pageable) {
        return repository.findAll(filters, pageable);
    }

    @Override
    public List<Warehouse> findAllActive() {
        return repository.findAllActive();
    }
}
