package com.jcuadrado.erplitebackend.domain.exception.security;

public class UserNotFoundException extends SecurityDomainException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
