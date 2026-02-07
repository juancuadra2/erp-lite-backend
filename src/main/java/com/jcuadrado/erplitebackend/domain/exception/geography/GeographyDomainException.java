package com.jcuadrado.erplitebackend.domain.exception.geography;

public class GeographyDomainException extends RuntimeException {

    public GeographyDomainException(String message) {
        super(message);
    }

    public GeographyDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
