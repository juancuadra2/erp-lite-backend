package com.jcuadrado.erplitebackend.domain.service.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.InvalidUnitOfMeasureDataException;

public class UnitOfMeasureValidator {

    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 50;
    private static final int ABBREVIATION_MIN_LENGTH = 1;
    private static final int ABBREVIATION_MAX_LENGTH = 10;

    private static final String NAME_PATTERN = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$";
    private static final String ABBREVIATION_PATTERN = "^[a-zA-Z0-9²³]+$";

    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidUnitOfMeasureDataException("El nombre no puede estar vacío");
        }

        String trimmed = name.trim();

        if (trimmed.length() < NAME_MIN_LENGTH || trimmed.length() > NAME_MAX_LENGTH) {
            throw new InvalidUnitOfMeasureDataException("El nombre debe tener entre 2 y 50 caracteres");
        }

        if (!trimmed.matches(NAME_PATTERN)) {
            throw new InvalidUnitOfMeasureDataException("El nombre solo puede contener letras y espacios");
        }
    }

    public void validateAbbreviation(String abbreviation) {
        if (abbreviation == null || abbreviation.isBlank()) {
            throw new InvalidUnitOfMeasureDataException("La abreviatura no puede estar vacía");
        }

        String trimmed = abbreviation.trim();

        if (trimmed.length() < ABBREVIATION_MIN_LENGTH || trimmed.length() > ABBREVIATION_MAX_LENGTH) {
            throw new InvalidUnitOfMeasureDataException("La abreviatura debe tener entre 1 y 10 caracteres");
        }

        if (!trimmed.matches(ABBREVIATION_PATTERN)) {
            throw new InvalidUnitOfMeasureDataException(
                    "La abreviatura solo puede contener letras, números y superíndices ² ³"
            );
        }
    }

    public void validateAll(String name, String abbreviation) {
        validateName(name);
        validateAbbreviation(abbreviation);
    }
}
