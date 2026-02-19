package com.jcuadrado.erplitebackend.domain.service.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.DuplicateUnitOfMeasureAbbreviationException;
import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.DuplicateUnitOfMeasureNameException;
import com.jcuadrado.erplitebackend.domain.port.unitofmeasure.UnitOfMeasureRepository;

import java.util.UUID;

public class UnitOfMeasureValidationService {

    private final UnitOfMeasureRepository repository;

    public UnitOfMeasureValidationService(UnitOfMeasureRepository repository) {
        this.repository = repository;
    }

    public void ensureNameIsUnique(String name, UUID excludeUuid) {
        boolean exists = excludeUuid == null
                ? repository.existsByNameIgnoreCase(name)
                : repository.existsByNameIgnoreCaseAndUuidNot(name, excludeUuid);

        if (exists) {
            throw new DuplicateUnitOfMeasureNameException(name);
        }
    }

    public void ensureAbbreviationIsUnique(String abbreviation, UUID excludeUuid) {
        boolean exists = excludeUuid == null
                ? repository.existsByAbbreviationIgnoreCase(abbreviation)
                : repository.existsByAbbreviationIgnoreCaseAndUuidNot(abbreviation, excludeUuid);

        if (exists) {
            throw new DuplicateUnitOfMeasureAbbreviationException(abbreviation);
        }
    }
}
