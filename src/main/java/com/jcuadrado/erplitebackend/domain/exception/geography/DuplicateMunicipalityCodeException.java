package com.jcuadrado.erplitebackend.domain.exception.geography;

public class DuplicateMunicipalityCodeException extends GeographyDomainException {

    public DuplicateMunicipalityCodeException(String code, String departmentName) {
        super(String.format("Municipality with code '%s' already exists in department '%s'", code, departmentName));
    }

    public DuplicateMunicipalityCodeException(String code) {
        super(String.format("Municipality with code '%s' already exists in this department", code));
    }
}
