package com.jcuadrado.erplitebackend.domain.exception.unitofmeasure;

public class UnitOfMeasureInUseException extends UnitOfMeasureException {

    public UnitOfMeasureInUseException(long usageCount) {
        super(String.format("No se puede desactivar esta unidad porque está en uso por %d productos", usageCount));
    }

    public UnitOfMeasureInUseException(String message) {
        super(message);
    }
}