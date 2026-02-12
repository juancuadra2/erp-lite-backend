package com.jcuadrado.erplitebackend.domain.exception.geography;

import java.util.UUID;

public class DepartmentNotFoundException extends GeographyDomainException {

    public DepartmentNotFoundException(UUID uuid) {
        super(String.format("Department not found with UUID: %s", uuid));
    }

    public DepartmentNotFoundException(String code) {
        super(String.format("Department not found with code: %s", code));
    }

    public DepartmentNotFoundException(Long id) {
        super(String.format("Department not found with ID: %d", id));
    }
}
