package com.jcuadrado.erplitebackend.domain.exception.security;

public class InvalidCredentialsException extends SecurityDomainException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
