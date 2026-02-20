package com.jcuadrado.erplitebackend.application.usecase.warehouse;

import com.jcuadrado.erplitebackend.application.command.warehouse.CreateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.command.warehouse.UpdateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.port.warehouse.ManageWarehouseUseCase;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.WarehouseNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.port.warehouse.WarehouseRepository;
import com.jcuadrado.erplitebackend.domain.service.warehouse.WarehouseDomainService;
import com.jcuadrado.erplitebackend.domain.service.warehouse.WarehouseValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
public class ManageWarehouseUseCaseImpl implements ManageWarehouseUseCase {

    private final WarehouseRepository repository;
    private final WarehouseDomainService domainService;
    private final WarehouseValidationService validationService;

    public ManageWarehouseUseCaseImpl(WarehouseRepository repository,
                                      WarehouseDomainService domainService,
                                      WarehouseValidationService validationService) {
        this.repository = repository;
        this.domainService = domainService;
        this.validationService = validationService;
    }

    @Override
    @Transactional
    public Warehouse create(CreateWarehouseCommand command) {
        Warehouse warehouse = domainService.prepareForCreate(command);
        return repository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse update(UUID uuid, UpdateWarehouseCommand command) {
        Warehouse existing = repository.findByUuid(uuid)
                .orElseThrow(() -> new WarehouseNotFoundException(uuid));
        domainService.applyUpdate(existing, command);
        return repository.save(existing);
    }

    @Override
    @Transactional
    public void delete(UUID uuid) {
        Warehouse warehouse = repository.findByUuid(uuid)
                .orElseThrow(() -> new WarehouseNotFoundException(uuid));
        validationService.validateDeletable(warehouse);
        warehouse.softDelete();
        repository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse activate(UUID uuid) {
        Warehouse warehouse = repository.findByUuid(uuid)
                .orElseThrow(() -> new WarehouseNotFoundException(uuid));
        domainService.validateForActivation(warehouse);
        warehouse.activate();
        return repository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse deactivate(UUID uuid) {
        Warehouse warehouse = repository.findByUuid(uuid)
                .orElseThrow(() -> new WarehouseNotFoundException(uuid));
        validationService.validateDeactivatable(uuid);
        warehouse.deactivate();
        return repository.save(warehouse);
    }
}
