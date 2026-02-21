package com.jcuadrado.erplitebackend.domain.exception.security;

public class SecurityDomainException extends RuntimeException {

    public SecurityDomainException(String message) {
        super(message);
    }

    public SecurityDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
