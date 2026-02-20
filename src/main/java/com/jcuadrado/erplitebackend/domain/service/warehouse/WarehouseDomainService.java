package com.jcuadrado.erplitebackend.domain.service.warehouse;

import com.jcuadrado.erplitebackend.application.command.warehouse.CreateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.command.warehouse.UpdateWarehouseCommand;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.DuplicateWarehouseCodeException;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.DuplicateWarehouseNameException;
import com.jcuadrado.erplitebackend.domain.exception.warehouse.SinglePrincipalWarehouseException;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;
import com.jcuadrado.erplitebackend.domain.port.warehouse.WarehouseRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class WarehouseDomainService {

    private final WarehouseValidator validator;
    private final WarehouseRepository repository;

    public WarehouseDomainService(WarehouseValidator validator, WarehouseRepository repository) {
        this.validator = validator;
        this.repository = repository;
    }

    public Warehouse prepareForCreate(CreateWarehouseCommand command) {
        String normalizedCode = command.code().toUpperCase().trim();

        validator.validateAll(normalizedCode, command.name(), command.type(),
                command.email(), command.phone());

        if (repository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new DuplicateWarehouseCodeException(normalizedCode);
        }
        if (repository.existsByNameIgnoreCase(command.name())) {
            throw new DuplicateWarehouseNameException(command.name());
        }
        if (command.type() == WarehouseType.PRINCIPAL
                && repository.existsActivePrincipalWarehouse()) {
            throw new SinglePrincipalWarehouseException();
        }

        return Warehouse.builder()
                .uuid(UUID.randomUUID())
                .code(normalizedCode)
                .name(command.name())
                .description(command.description())
                .type(command.type())
                .address(command.address())
                .municipalityId(command.municipalityId())
                .responsible(command.responsible())
                .email(command.email())
                .phone(command.phone())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void applyUpdate(Warehouse existing, UpdateWarehouseCommand command) {
        validator.validateAll(existing.getCode(), command.name(), command.type(),
                command.email(), command.phone());

        if (repository.existsByNameIgnoreCaseAndUuidNot(command.name(), existing.getUuid())) {
            throw new DuplicateWarehouseNameException(command.name());
        }
        if (command.type() == WarehouseType.PRINCIPAL
                && repository.existsActivePrincipalWarehouseAndUuidNot(existing.getUuid())) {
            throw new SinglePrincipalWarehouseException();
        }

        existing.setName(command.name());
        existing.setDescription(command.description());
        existing.setType(command.type());
        existing.setAddress(command.address());
        existing.setMunicipalityId(command.municipalityId());
        existing.setResponsible(command.responsible());
        existing.setEmail(command.email());
        existing.setPhone(command.phone());
        existing.setUpdatedAt(LocalDateTime.now());
    }

    public void validateForActivation(Warehouse warehouse) {
        if (warehouse.getType() == WarehouseType.PRINCIPAL
                && repository.existsActivePrincipalWarehouseAndUuidNot(warehouse.getUuid())) {
            throw new SinglePrincipalWarehouseException();
        }
    }
}
