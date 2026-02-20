package com.jcuadrado.erplitebackend.domain.exception.warehouse;

public class WarehouseInUseException extends RuntimeException {

    public WarehouseInUseException(String message) {
        super(message);
    }
}
