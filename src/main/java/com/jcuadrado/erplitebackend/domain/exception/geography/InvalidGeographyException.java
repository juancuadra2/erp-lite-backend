package com.jcuadrado.erplitebackend.domain.exception.geography;

public class InvalidGeographyException extends GeographyDomainException {

    public InvalidGeographyException(String message) {
        super(message);
    }

    public InvalidGeographyException(String field, String reason) {
        super(String.format("Invalid %s: %s", field, reason));
    }
}
