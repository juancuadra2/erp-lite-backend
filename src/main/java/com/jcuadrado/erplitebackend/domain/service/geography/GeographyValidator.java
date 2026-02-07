package com.jcuadrado.erplitebackend.domain.service.geography;

import com.jcuadrado.erplitebackend.domain.exception.geography.InvalidGeographyException;

/**
 * Validator for geography domain rules.
 * Validates department and municipality codes and names.
 */
public class GeographyValidator {

    private static final String DEPARTMENT_CODE_PATTERN = "^\\d{2}$";
    private static final String MUNICIPALITY_CODE_PATTERN = "^\\d{5}$";
    private static final int NAME_MIN_LENGTH = 1;
    private static final int NAME_MAX_LENGTH = 100;

    public void validateDepartmentCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidGeographyException("code", "Department code is required");
        }
        String trimmedCode = code.trim();
        if (!trimmedCode.matches(DEPARTMENT_CODE_PATTERN)) {
            throw new InvalidGeographyException("code",
                    "Department code must be exactly 2 digits");
        }
    }

    public void validateMunicipalityCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidGeographyException("code", "Municipality code is required");
        }
        String trimmedCode = code.trim();
        if (!trimmedCode.matches(MUNICIPALITY_CODE_PATTERN)) {
            throw new InvalidGeographyException("code",
                    "Municipality code must be exactly 5 digits");
        }
    }

    public void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidGeographyException("name", "Name is required");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < NAME_MIN_LENGTH) {
            throw new InvalidGeographyException("name", "Name cannot be empty");
        }
        if (trimmedName.length() > NAME_MAX_LENGTH) {
            throw new InvalidGeographyException("name",
                    String.format("Name must not exceed %d characters", NAME_MAX_LENGTH));
        }
    }

    public void validateDepartment(String code, String name) {
        validateDepartmentCode(code);
        validateName(name);
    }

    public void validateMunicipality(String code, String name) {
        validateMunicipalityCode(code);
        validateName(name);
    }
}
