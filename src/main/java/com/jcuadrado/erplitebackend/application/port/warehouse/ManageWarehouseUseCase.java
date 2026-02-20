package com.jcuadrado.erplitebackend.application.port.warehouse;

import com.jcuadrado.erplitebackend.application.command.warehouse.CreateWarehouseCommand;
import com.jcuadrado.erplitebackend.application.command.warehouse.UpdateWarehouseCommand;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;

import java.util.UUID;

public interface ManageWarehouseUseCase {

    Warehouse create(CreateWarehouseCommand command);

    Warehouse update(UUID uuid, UpdateWarehouseCommand command);

    void delete(UUID uuid);

    Warehouse activate(UUID uuid);

    Warehouse deactivate(UUID uuid);
}
