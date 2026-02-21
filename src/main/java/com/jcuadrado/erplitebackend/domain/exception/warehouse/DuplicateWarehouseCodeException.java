package com.jcuadrado.erplitebackend.domain.exception.warehouse;

public class DuplicateWarehouseCodeException extends RuntimeException {

    public DuplicateWarehouseCodeException(String code) {
        super("Ya existe una bodega con el código " + code);
    }
}
