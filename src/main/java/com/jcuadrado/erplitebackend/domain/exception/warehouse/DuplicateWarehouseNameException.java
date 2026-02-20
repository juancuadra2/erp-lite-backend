package com.jcuadrado.erplitebackend.domain.exception.warehouse;

public class DuplicateWarehouseNameException extends RuntimeException {

    public DuplicateWarehouseNameException(String name) {
        super("Ya existe una bodega con el nombre " + name);
    }
}
