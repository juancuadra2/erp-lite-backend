package com.jcuadrado.erplitebackend.domain.service.unitofmeasure;

import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.UnitOfMeasureInUseException;
import com.jcuadrado.erplitebackend.domain.model.unitofmeasure.UnitOfMeasure;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UnitOfMeasureDomainService {

    private final UnitOfMeasureValidator validator;
    private final UnitOfMeasureValidationService validationService;

    public String normalizeAbbreviation(String abbreviation) {
        return abbreviation.toUpperCase().trim();
    }

    public void prepareForCreation(UnitOfMeasure unitOfMeasure) {
        validator.validateAll(unitOfMeasure.getName(), unitOfMeasure.getAbbreviation());

        String normalizedAbbreviation = normalizeAbbreviation(unitOfMeasure.getAbbreviation());
        unitOfMeasure.setAbbreviation(normalizedAbbreviation);

        validationService.ensureNameIsUnique(unitOfMeasure.getName(), null);
        validationService.ensureAbbreviationIsUnique(normalizedAbbreviation, null);
    }

    public void prepareForUpdate(UnitOfMeasure unitOfMeasure, UUID existingUuid) {
        validator.validateAll(unitOfMeasure.getName(), unitOfMeasure.getAbbreviation());

        String normalizedAbbreviation = normalizeAbbreviation(unitOfMeasure.getAbbreviation());
        unitOfMeasure.setAbbreviation(normalizedAbbreviation);

        validationService.ensureNameIsUnique(unitOfMeasure.getName(), existingUuid);
        validationService.ensureAbbreviationIsUnique(normalizedAbbreviation, existingUuid);
    }

    public void ensureCanBeDeactivated(long usageCount) {
        if (usageCount > 0) {
            throw new UnitOfMeasureInUseException(usageCount);
        }
    }
}
