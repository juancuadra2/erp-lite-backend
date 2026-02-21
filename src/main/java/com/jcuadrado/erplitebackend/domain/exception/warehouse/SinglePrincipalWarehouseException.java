package com.jcuadrado.erplitebackend.domain.exception.warehouse;

public class SinglePrincipalWarehouseException extends RuntimeException {

    public SinglePrincipalWarehouseException() {
        super("Ya existe una bodega PRINCIPAL activa. Solo puede haber una a la vez.");
    }
}
