package com.jcuadrado.erplitebackend.domain.exception.unitofmeasure;

public class DuplicateUnitOfMeasureNameException extends UnitOfMeasureException {

    public DuplicateUnitOfMeasureNameException(String name) {
        super(String.format("Ya existe una unidad de medida con el nombre '%s'", name));
    }
}