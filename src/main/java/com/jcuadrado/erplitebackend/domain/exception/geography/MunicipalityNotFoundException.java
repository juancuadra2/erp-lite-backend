package com.jcuadrado.erplitebackend.domain.exception.geography;

import java.util.UUID;

public class MunicipalityNotFoundException extends GeographyDomainException {

    public MunicipalityNotFoundException(UUID uuid) {
        super(String.format("Municipality not found with UUID: %s", uuid));
    }

    public MunicipalityNotFoundException(String code) {
        super(String.format("Municipality not found with code: %s", code));
    }

    public MunicipalityNotFoundException(Long id) {
        super(String.format("Municipality not found with ID: %d", id));
    }
}
