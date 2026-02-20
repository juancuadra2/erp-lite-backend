package com.jcuadrado.erplitebackend.domain.exception.warehouse;

import java.util.UUID;

public class WarehouseNotFoundException extends RuntimeException {

    public WarehouseNotFoundException(UUID uuid) {
        super("Bodega no encontrada con UUID: " + uuid);
    }
}
