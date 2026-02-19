package com.jcuadrado.erplitebackend.domain.exception.unitofmeasure;

import java.util.UUID;

public class UnitOfMeasureNotFoundException extends UnitOfMeasureException {

    public UnitOfMeasureNotFoundException(String message) {
        super(message);
    }

    public UnitOfMeasureNotFoundException(UUID uuid) {
        super("Unit of measure not found with UUID: " + uuid);
    }
}