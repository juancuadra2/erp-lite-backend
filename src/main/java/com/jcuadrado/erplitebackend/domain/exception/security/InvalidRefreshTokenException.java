package com.jcuadrado.erplitebackend.domain.exception.security;

public class InvalidRefreshTokenException extends SecurityDomainException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
