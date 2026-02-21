package com.jcuadrado.erplitebackend.domain.exception.security;

public class InvalidPasswordException extends SecurityDomainException {

    public InvalidPasswordException(String message) {
        super(message);
    }
}
