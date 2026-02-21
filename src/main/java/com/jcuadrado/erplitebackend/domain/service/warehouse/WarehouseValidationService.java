package com.jcuadrado.erplitebackend.domain.service.warehouse;

import com.jcuadrado.erplitebackend.domain.exception.warehouse.WarehouseInUseException;
import com.jcuadrado.erplitebackend.domain.model.warehouse.Warehouse;
import com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType;

import java.util.UUID;

public class WarehouseValidationService {

    public void validateDeletable(Warehouse warehouse) {
        if (warehouse.getType() == WarehouseType.PRINCIPAL && warehouse.isActive()) {
            throw new WarehouseInUseException(
                    "No se puede eliminar la bodega PRINCIPAL activa. Es el punto de venta activo del sistema");
        }
        // BR-03.1: stub — verificación de stock activo se implementará en 07-inventory
    }

    public void validateDeactivatable(UUID uuid) {
        // BR-03.2: stub — verificación de transferencias pendientes se implementará en 07-inventory
    }
}
