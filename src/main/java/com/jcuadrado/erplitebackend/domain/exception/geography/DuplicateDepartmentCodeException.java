package com.jcuadrado.erplitebackend.domain.exception.geography;

public class DuplicateDepartmentCodeException extends GeographyDomainException {

    public DuplicateDepartmentCodeException(String code) {
        super(String.format("Department with code '%s' already exists", code));
    }
}
