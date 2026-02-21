package com.jcuadrado.erplitebackend.domain.exception.security;

public class DuplicateUsernameException extends SecurityDomainException {

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
