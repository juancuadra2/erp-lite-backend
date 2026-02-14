package com.jcuadrado.erplitebackend.domain.exception.unitofmeasure;

public class DuplicateUnitOfMeasureAbbreviationException extends UnitOfMeasureException {

    public DuplicateUnitOfMeasureAbbreviationException(String abbreviation) {
        super(String.format("Ya existe una unidad de medida con la abreviatura '%s'", abbreviation));
    }
}